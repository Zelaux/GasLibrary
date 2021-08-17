package gas.tools.gasBlockConverter;

import arc.files.*;
import arc.struct.*;
import arc.struct.ObjectMap.*;
import arc.util.*;
import com.github.javaparser.*;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.expr.UnaryExpr.*;
import com.github.javaparser.ast.nodeTypes.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.*;
import com.github.javaparser.ast.visitor.*;
import com.github.javaparser.resolution.types.*;
import root.gasBlockConverter.visitors.*;

import static root.gasBlockConverter.CreatingGasBlocks.javaParser;

public class GasBlocksConverter{
    static Seq<ImportDeclaration> packagesToImport = new Seq<>();
    static Fi blockDir, root, coreDir;
    private static boolean  moreLogs = false;
    private final ObjectMap<String, ClassEntry> classMap;
    private final ObjectMap<String, CompilationUnit> existsClasses;//= new ObjectMap<>();
    private final NameTransform transform;

    public GasBlocksConverter(Seq<String> transformNames,
                              Seq<String> transformFields,
                              Seq<String> mindustryAnnotations,
                              ObjectMap<String, ClassEntry> classMap,
                              ObjectMap<String, CompilationUnit> existsClasses){
//        this.transformNames = transformNames;
//        this.transformFields = transformFields;
//        this.mindustryAnnotations = mindustryAnnotations;
        this.classMap = classMap;
        this.existsClasses = existsClasses;
        transform = new NameTransform(transformNames, transformFields, mindustryAnnotations);
    }

    void run(){
        Fi sourcesFi = new Fi("compDownloader").child("sources.zip");
        coreDir = Fi.get("core/src");
        Fi dir = coreDir.child("gas/world");
        blockDir = dir.child("blocks");
        blockDir.mkdirs();
        ZipFi sourceZip = new ZipFi(sourcesFi);
        root = sourceZip.list()[0];
        Fi worldRoot = root.child("core").child("src").child("mindustry").child("world");
        Fi blocksRoot = worldRoot.child("blocks");
        for(Entry<String, ClassEntry> entry : classMap){
            ClassEntry classEntry = entry.value;
            handleBlock(entry.key, classEntry.fi, classEntry.compilationUnit);

        }
        /*blocksRoot.walk(fi -> {
            if(fi.nameWithoutExtension().equals("LiquidBlock")){
                handleBlockFile(fi);
            }
        });*/
    }

    private void handleBlock(String name, Fi file, CompilationUnit compilationUnit){
        String nameAsString = compilationUnit.getPackageDeclaration().get().getNameAsString();
        if(!transform.names.contains(name) || !nameAsString.contains("world.blocks") && !nameAsString.contains("world.draw")) return;
        compilationUnit.getClassByName(file.name());
//            compilationUnit.getImports();
        CompilationUnit gasBlock = compilationUnit.clone();
        NodeList<ImportDeclaration> imports = gasBlock.getImports();
        String aPackage = gasBlock.getPackageDeclaration().get().getNameAsString().replace("mindustry.", "gas.");
        Log.info("[@]", name);
        gasBlock.setPackageDeclaration(aPackage);


//        Log.info("imports");
        packagesToImport.clear();

        for(ImportDeclaration anImport : imports.toArray(new ImportDeclaration[0])){
            imports.add(new ImportDeclaration(anImport.getNameAsString().replace("mindustry", "gas"), anImport.isStatic(), anImport.isAsterisk()));
        }
        String gasBlockName = transform.type(file.nameWithoutExtension()) + "." + file.extension();
//        Log.info("types");

        gasBlock.accept(new CustomModifierVisitor(){
            MethodDeclaration currentMethod;
            boolean liquidMethod;
            boolean classField = false;

            @Override
            public Visitable visit(VariableDeclarator n, Void arg){
                Type clone = n.getType().clone();
                Visitable visit = super.visit(n, arg);
                if(classField) n.setType(clone);
                return visit;
            }


            @Override
            public Visitable visit(FieldDeclaration n, Void arg){
                classField = true;
                Visitable visit = super.visit(n, arg);
                classField = false;
                return visit;
            }

            @Override
            public Visitable visit(MethodDeclaration method, Void arg){
                for(AnnotationExpr annotation : method.getAnnotations()){
                    if(annotation.getNameAsString().equals("Override")){

                        liquidMethod = method.getNameAsString().toLowerCase().contains("liquid");
                        if(method.getBody().isPresent()){
                            BlockStmt body = method.getBody().get();
                            NodeList<Statement> statements = body.getStatements();
                            currentMethod = method;
                            for(Parameter parameter : method.getParameters()){
                                Type type = parameter.getType();
                                if(!type.isClassOrInterfaceType()) continue;
                                ClassOrInterfaceType classType = type.asClassOrInterfaceType();
                                if(transform.names.contains(classType.getNameWithScope())){
                                    ClassOrInterfaceType varType = new ClassOrInterfaceType().setName(transform.type(classType.getNameWithScope()));
                                    NameExpr varName = new NameExpr("gas" + Strings.capitalize(parameter.getNameAsString()));
                                    IfStmt ifStmt = new IfStmt();

                                    ifStmt.setCondition(new UnaryExpr(new InstanceOfExpr(varName, varType), Operator.LOGICAL_COMPLEMENT));

                                    statements.add(0, new ExpressionStmt(
                                    new VariableDeclarationExpr(new VariableDeclarator(varType, varName.getName(), new CastExpr(varType, parameter.getNameAsExpression())))
                                    ));
                                    if(!method.getType().isVoidType()){
                                        ReturnStmt thenStmt = new ReturnStmt();
                                        ifStmt.setThenStmt(thenStmt);
                                        if(method.getType().isPrimitiveType()){
                                            PrimitiveType primitiveType = method.getType().asPrimitiveType();
                                            String descriptor = primitiveType.toDescriptor();
                                            boolean isNumeric= Structs.contains("BSIJFD".split(""),descriptor),isBoolean=descriptor.equals("Z");
                                            try{
                                                ResolvedReferenceType resolve = primitiveType.toBoxedType().resolve();
                                                if (resolve.isUnboxable()){

                                                    isBoolean = resolve.toUnboxedType().get().isBoolean();
                                                    isNumeric = resolve.toUnboxedType().get().isNumeric();
                                                }


                                            } catch(Exception e){
                                                Log.err("Cannot resolve: @ in @",primitiveType,method.getName());
                                                if (moreLogs){
                                                    Log.info("primitiveType: @,dec: @,method: @",primitiveType,descriptor,method);
                                                    Log.err(e);
                                                }
                                            }
                                            if(isBoolean){
                                                thenStmt.setExpression(new BooleanLiteralExpr(false));
                                            }else
                                            if(isNumeric){
                                                thenStmt.setExpression(new IntegerLiteralExpr("0"));
                                            }else{
                                                thenStmt.addOrphanComment(new BlockComment("i don't now what i need return"));
                                                thenStmt.setExpression(new NullLiteralExpr());
                                            }
                                        }else{
                                            thenStmt.setExpression(new NullLiteralExpr());
                                        }
                                    }
                                    statements.add(0, ifStmt);
                                }
                            }
                            body.accept(this, arg);
                            currentMethod = null;
                        }
                        liquidMethod = false;
                        return method;
                    }
                }
                Visitable visit = super.visit(method, arg);
                return visit;
            }

            @Override
            public Visitable visit(NameExpr nameExpr, Void arg){
                if(currentMethod != null){
                    for(Parameter parameter : currentMethod.getParameters()){
                        if(parameter.getName().equals(nameExpr.getName())){
                            Type type = parameter.getType();
                            if(!type.isClassOrInterfaceType()) continue;
                            ClassOrInterfaceType classType = type.asClassOrInterfaceType();
                            if(transform.names.contains(classType.getNameWithScope())){
                                nameExpr.setName("gas" + Strings.capitalize(nameExpr.getNameAsString()));
                            }
                            return nameExpr;
                        }
                    }
                }
                return super.visit(nameExpr, arg);
            }

            @Override
            public Visitable visit(SimpleName simpleName, Void arg){
                String type = transform.type(simpleName.getIdentifier());
                simpleName.setIdentifier(type);
                return super.visit(simpleName, arg);
            }
        }, null);
//        javaParser.parseBlock("import static gas.gen.*;")
        for(TypeDeclaration<?> type : gasBlock.getTypes()){

          /*  eachNode(type, node -> {
                if(node instanceof SimpleName){
                    SimpleName simpleName = (SimpleName)node;
                    simpleName.setIdentifier(transform.type(simpleName.getIdentifier()));
                }
            });*/
            handleBodyDeclaration(type);
        }
        boolean liquid = false;
        if(gasBlock.getPackageDeclaration().get().getNameAsString().contains("gas.world.blocks.liquid")){
            liquid = true;
            fromLiquidBlock(gasBlockName, file, gasBlock.clone());
        }
        if(!liquid || gasBlockName.contains("Liquid")) saveCompilationUnit(gasBlock, aPackage, gasBlockName);
//        blockDir.child(gasBlockName).writeString(gasBlock.toString());
    }

    private void saveCompilationUnit(CompilationUnit aClass, String aPackage, String className){
        coreDir.child(aPackage.replace(".", "/") + "/" + (className.contains(".") ? className : className + ".java")).writeString(aClass.toString());
    }

    private void fromLiquidBlock(String name, Fi file, CompilationUnit compilationUnit){
        String gasBlock = name.replace("Liquid", "Gas");
        if (!gasBlock.contains("GasGasBlock")){
            gasBlock=gasBlock.replace("GasGas","Gas");
        }
        PackageDeclaration packageDeclaration = compilationUnit.getPackageDeclaration().get();
        packageDeclaration.setName(packageDeclaration.getNameAsString().replace(".liquid", ".gas"));
        compilationUnit.accept(new CustomModifierVisitor(){
            boolean field = false;

            @Override
            public Visitable visit(ClassOrInterfaceDeclaration aClass, Void arg){
                Visitable visit = super.visit(aClass, arg);
                String name = aClass.getNameAsString();
               /* if (name.contains("GasGas") && !name.contains("GasGasBlock")){
                    aClass.setName(name.replace("GasGas","Gas"));
                }*/
                return visit;
            }

            @Override
            public Visitable visit(SimpleName simpleName, Void arg){
                simpleName.setIdentifier(simpleName.getIdentifier()
                .replace("Liquids", "Gasses")
                .replace("Liquid", "Gas")
                .replace("liquids", "gasses")
                .replace("liquid", "gas")
                .replace("GasGasBlock", "GAS_GAS_BLOCK")
                .replace("GasGas", "Gas")
                .replace("GAS_GAS_BLOCK", "GasGasBlcok")
                );
                return super.visit(simpleName, arg);
            }


            @Override
            public Visitable visit(FieldAccessExpr fieldAccess, Void arg){

                Visitable visit = super.visit(fieldAccess, arg);
                if (fieldAccess.getScope().toString().equals("Blocks")){
                    SimpleName simpleName = fieldAccess.getName();
                    String identifier = simpleName.getIdentifier();
                    if (identifier.equals("gasJunction") || identifier.equals("liquidJunction")){
//                        String expression = ;
                        ParseResult<Expression> parse = javaParser.parseExpression("Vars.content.blocks().find(b->b instanceof GasJunction)");
                        if (parse.isSuccessful()){
                            Expression expression = parse.getResult().get();
                            Log.info("expression: @",expression);
                            fieldAccess.replace(expression);
                            return expression;
                        } else {
                            throw new RuntimeException(parse.toString());
                        }
                    } else if (identifier.equals("bridgeConduit")){

//                        String expression = ;
                        ParseResult<Expression> parse = javaParser.parseExpression("Vars.content.blocks().find(b->b instanceof GasBridge)");
                        if (parse.isSuccessful()){
                            Expression expression = parse.getResult().get();
                            Log.info("expression: @",expression);
                            Node parentNode = fieldAccess.getParentNode().get();
                            parentNode.replace(fieldAccess,expression);
                            expression.setParentNode(parentNode);
                            return expression;
                        } else {
                            throw new RuntimeException(parse.toString());
                        }
                    }
                }
                return visit;
            }

            @Override
            public Visitable visit(MethodDeclaration method, Void arg){
                if(true) return super.visit(method, arg);
                NodeList<Parameter> parameters = null;
                for(AnnotationExpr annotation : method.getAnnotations()){
                    Log.info("annotation: @", annotation.getNameAsString());
                    if(annotation.getNameAsString().equals("Override")){
                        return method;
//                        parameters = modifyList(method.getParameters(), null);
//                        break;
                    }
                }

                Visitable visit = super.visit(method, arg);
                if(parameters != null){
                    method.setParameters(parameters);
                }
                return visit;
            }

            @Override
            public Visitable visit(FieldDeclaration field, Void arg){
                this.field = true;
                Visitable visit = super.visit(field, arg);
                if(field.getVariables().isEmpty()){
                    field.remove();
                }
                this.field = false;
                return visit;
            }

            @Override
            public Visitable visit(VariableDeclarator variable, Void arg){
                if(field){
                    if(variable.getNameAsString().toLowerCase().contains("liquid")){
                        variable.remove();
                    }
                }
                Visitable visit = super.visit(variable, arg);
                return visit;
            }

            @Override
            public Visitable visit(NameExpr nameExpr, Void arg){
                Visitable visit = super.visit(nameExpr, arg);
                return visit;
            }

        }, null);
       /* eachNode(compilationUnit, node -> {


        });*/
        saveCompilationUnit(compilationUnit, packageDeclaration.getNameAsString(), gasBlock.substring(0, gasBlock.indexOf(".")));
//        compilationUnit.getChildNodes()

    }

    private void handleBodyDeclaration(BodyDeclaration<?> type){
        if(type instanceof TypeDeclaration){
            for(BodyDeclaration<?> member : ((TypeDeclaration<?>)type).getMembers().toArray(new BodyDeclaration<?>[0])){
                handleBodyDeclaration(member);
            }
        }
        if(type.isClassOrInterfaceDeclaration()){
            ClassOrInterfaceDeclaration declaration = type.asClassOrInterfaceDeclaration();
            /*declaration.getMethods()*/
        }
        if(type.isMethodDeclaration()){
            MethodDeclaration declaration = type.asMethodDeclaration();
            if(declaration.getBody().isPresent()){
                BlockStmt body = declaration.getBody().get();
                handleStatement(body);
            }
        }
        if(type.isFieldDeclaration()){
            FieldDeclaration parent = type.asFieldDeclaration();
            for(VariableDeclarator variable : parent.getVariables()){
                String fieldName = variable.getNameAsString();
                for(AnnotationExpr annotation : parent.getAnnotations()){
                    annotation.setName(transform.annotation(annotation.getNameAsString()));
                }
                if(transform.fields.contains(fieldName)){
                    FieldDeclaration declaration = new FieldDeclaration(parent.getModifiers(),
                    new VariableDeclarator(variable.getType(), transform.field(fieldName))
                    );
                    for(AnnotationExpr annotation : parent.getAnnotations()){
                        AnnotationExpr annotationExpr = annotation.clone();
                        if(annotationExpr.isSingleMemberAnnotationExpr()){
                            SingleMemberAnnotationExpr expr = annotationExpr.asSingleMemberAnnotationExpr();
                            if(expr.getMemberValue().isStringLiteralExpr()){
                                StringLiteralExpr literalExpr = expr.getMemberValue().asStringLiteralExpr();
                                literalExpr.setString(literalExpr.getValue().replace("liquid", "gas"));
                            }
                        }
                        declaration.addAnnotation(annotationExpr);
                    }
                    Node rootNode = type.getParentNode().get();
                    NodeList<BodyDeclaration<?>> members = ((TypeDeclaration<?>)rootNode).getMembers();
                    members.add(members.indexOf(parent) + 1, declaration);
                  if(moreLogs)  Log.info("new field: @", transform.field(fieldName));
                }
            }
        }

    }

    private void handleStatement(Statement statement){
        if(statement.isBlockStmt()){
            for(Statement other : statement.asBlockStmt().getStatements()){
                handleStatement(other);
            }
        }else if(statement.isIfStmt()){
            IfStmt ifStmt = statement.asIfStmt();
            handleStatement(ifStmt.getThenStmt());
            if(ifStmt.getElseStmt().isPresent()){
                handleStatement(ifStmt.getElseStmt().get());
            }
            handleExpression(ifStmt.getCondition());
        }else if(statement.isReturnStmt()){
            ReturnStmt returnStmt = statement.asReturnStmt();
            if(returnStmt.getExpression().isPresent()){
                handleExpression(returnStmt.getExpression().get());
            }
        }else if(statement.isExpressionStmt()){
            handleExpression(statement.asExpressionStmt().getExpression());
        }else if(statement.isSwitchStmt()){
            SwitchStmt switchStmt = statement.asSwitchStmt();
        }
    }

    private void handleExpression(Expression expression){

    }

    private String getName(Node node){
        boolean named = node instanceof NodeWithSimpleName;
        String name = named ?
        ((NodeWithSimpleName<?>)node).getNameAsString() :
        (node.toString().split("\n", 2)[0]);
        if(!named) name += "\t\tNO_NAME";
        return name;
    }

    private boolean needTransformName(String name){
        return !existsClasses.containsKey(name);
    }
}
