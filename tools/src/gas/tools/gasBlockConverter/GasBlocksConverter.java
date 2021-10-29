package gas.tools.gasBlockConverter;

import arc.files.*;
import arc.func.*;
import arc.struct.*;
import arc.struct.ObjectMap.*;
import arc.util.*;
import com.github.javaparser.*;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.Node.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.nodeTypes.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.*;
import com.github.javaparser.ast.visitor.*;
import com.github.javaparser.resolution.declarations.*;
import gas.tools.parsers.visitors.*;

import javax.annotation.Nullable;
import java.util.*;

import static gas.tools.updateVersion.CreatingGasBlocks.javaParser;

public class GasBlocksConverter{
    static boolean moreLogs = false;
    public final ObjectMap<String, CompilationUnit> classMap = new ObjectMap<>(), existsClasses = new ObjectMap<>();
    public final NameTransform transform;
    final ObjectSet<CompilationUnit> created = new ObjectSet<>();
    private final ObjectSet<String> skippedClasses = new ObjectSet<>();
    public CompilationUnit gasBuilding, gasBlock;
    public boolean logSkipped;
    //    static Seq<ImportDeclaration> packagesToImport = new Seq<>();
    Fi
    /**core/generated/gas/blocks*/
    blockDir,
    /** mindustry */
    root,
    /** core/generated */
    coreDir,
    /** core/src */
    mainDir,
    mainBlockDir;

    public GasBlocksConverter(NameTransform transform){
        this.transform = transform;
    }

    private void setupFiles(){
        Fi sourcesFi = new Fi("compDownloader").child("sources.zip");
        coreDir = Fi.get("core/generated");
        mainDir = Fi.get("core/src");
        blockDir = coreDir.child("gas/world").child("blocks");
        blockDir.mkdirs();
        mainBlockDir = mainDir.child("gas/world").child("blocks");
        mainBlockDir.mkdirs();
        ZipFi sourceZip = new ZipFi(sourcesFi);
        root = sourceZip.list()[0];
    }

    public void run(){
        if(coreDir == null || blockDir == null || root == null){
            setupFiles();
        }
        if(false){
            for(CompilationUnit compilationUnit : classMap.values()){

                for(NameExpr nameExpr : compilationUnit.findAll(NameExpr.class)){
                    Log.info("@", nameExpr);
                    try{
                        ResolvedValueDeclaration resolve = nameExpr.resolve();
                        Log.info("--resolved: @", resolve);
                    }catch(Exception e){
                        Log.info("--Cannot resolve");
                    }
                }
                Log.info("package @", compilationUnit.getPackageDeclaration().map(NodeWithName::getNameAsString).orElse("<none>"));
            }
            return;
        }
        ImportProcessor.reset();
        for(Entry<String, CompilationUnit> entry : classMap){
//            ClassEntry classEntry = entry.value;
            CompilationUnit compilationUnit = entry.value;
            if(entry.key.contains("mindustry.world")){
                ImportProcessor.compilationUnitPackageImport(compilationUnit);
                ImportProcessor.compilationUnitPackageImport(compilationUnit, s -> s.replace("mindustry", "gas"));
            }
//            handleBlock(entry.key,/* classEntry.fi,*/ compilationUnit);
        }

        skippedClasses.clear();
        for(Entry<String, CompilationUnit> entry : classMap){
            String[] keys = entry.key.split("\\.");
            transformClass(keys[keys.length - 1],/* classEntry.fi,*/ entry.value);
        }
        if(logSkipped){
            for(String skippedClass : skippedClasses){
                Log.info("skipped: @", skippedClass);
            }
        }
        Seq<ClassOrInterfaceType> seq = new Seq<>();
        Seq<CompilationUnit> sort = created.asArray().sort((a, b) -> {
            ClassOrInterfaceDeclaration aClass = a.findFirst(ClassOrInterfaceDeclaration.class).get();
            ClassOrInterfaceDeclaration bClass = b.findFirst(ClassOrInterfaceDeclaration.class).get();
            try{
                Func2<ClassOrInterfaceType, ClassOrInterfaceDeclaration, String> descriptor = (classType, declaration) -> {
                    try{
                        return classType.toDescriptor();
                    }catch(Exception e){

                        String packageName = declaration.findCompilationUnit()
                        .flatMap(CompilationUnit::getPackageDeclaration)
                        .map(NodeWithName::getNameAsString).orElse(null);
                        String className = classType.getNameAsString();
                        CompilationUnit unit = getCompilationUnit(packageName, className);
                        if(unit == null){
                            unit = created.asArray().find(c -> {
//                                boolean samePackage = c.getPackageDeclaration().map(NodeWithName::getNameAsString).orElse("").equals(packageName);
                                return c.getClassByName(className).isPresent();
                            });
                        }
                        if(unit != null){
                            return unit.getClassByName(className).get().getFullyQualifiedName().get();
                        }
                        throw new RuntimeException(e);
                    }
                };

                seq.addAll(aClass.getExtendedTypes());
                boolean first =
                !aClass.isInterface() &&
                seq.contains(type -> descriptor.get(type, aClass).equals(bClass.getFullyQualifiedName().orElse(null)));
                seq.clear();

                seq.addAll(bClass.getExtendedTypes());
                boolean second = !bClass.isInterface() &&
                seq.contains(type -> descriptor.get(type, bClass).equals(aClass.getFullyQualifiedName().orElse(null)));
                seq.clear();
                return Boolean.compare(first, second);
            }catch(Exception exception){
                String apackage = a.getPackageDeclaration().map(NodeWithName::getNameAsString).orElse("<none>");
                String bpackage = b.getPackageDeclaration().map(NodeWithName::getNameAsString).orElse("<none>");
                Log.info("(@.@;@.@)", apackage, aClass.getNameAsString(), bpackage, bClass.getNameAsString());
                throw exception;
            }
        });

        //post processing
        for(CompilationUnit compilationUnit : sort){
            postProcessing(compilationUnit);
        }
    }

    private void postProcessing(CompilationUnit compilationUnit){
        if(compilationUnit.getPackageDeclaration().map(NodeWithName::getNameAsString).orElse("").startsWith("gas.world.draw")){
            return;
        }
        ClassOrInterfaceDeclaration firstClass = compilationUnit.findFirst(ClassOrInterfaceDeclaration.class).get();
        String className = firstClass.getNameAsString();
        Log.info("postProcessing: @", firstClass.getFullyQualifiedName().orElse(className));
//        compilationUnit.accept(new MethodsFixer(this), null);
        compilationUnit.accept(new ModifierVisitor<Void>(){
            boolean returnSwitch = false;

            @Override
            public Visitable visit(MethodCallExpr method, Void arg){
//                super.visit(method, arg);
                if(true) return super.visit(method, arg);
                Expression scope = method.getScope().orElse(null);
                if(scope == null || scope.isSuperExpr()){
                    return super.visit(method, arg);
                }
                try{
                    if(scope.isNameExpr()){
//                        NameExpr scopeAsName = scope.asNameExpr();
//                        ResolvedValueDeclaration resolve = scopeAsName.resolve();
//                        Log.info("scopeResoled: @",resolve.getType());
                    }
                    Log.info("methodName: @", method.getNameAsString());
                    ResolvedMethodDeclaration resolve = method.resolve();
                    ;
                    ObjectSet<String> set = new ObjectSet<>();
                    Fi child = blockDir.child("methods.txt");
                    if(child.exists()){
                        set.addAll(child.readString().split("\n"));
                    }
                    set.add(resolve.getQualifiedName());
                    child.writeString(set.toString("\n"));
                    return method;
                }catch(Exception exception){
                    if(true) throw new RuntimeException(exception);
                    Log.err("Cannot resolve method: @", method.getNameAsString());
                    return method;
                }catch(NoClassDefFoundError classDefFoundError){
                    Log.info("Cannot find class: @", classDefFoundError.getMessage());
                    return method;
                }
            }
        }, null);
        saveCompilationUnitAndAddCreated(compilationUnit, compilationUnit.getPackageDeclaration().get().getNameAsString(), className);
    }


    private void transformClass(String className, CompilationUnit parent){
//        className=className.substring(!className.contains(".")?0:className.lastIndexOf(".")+1);
        String prevPackage = parent.getPackageDeclaration().get().getNameAsString();
        ClassInfo classInfo = new ClassInfo(prevPackage, className);

        String gasBlockName = transform.type(className) + ".java";
        boolean exists =  exists(gasBlockName);
        boolean inBlackList = false;
        if(!transform.names.contains(className) || (inBlackList = BlockConverterSettings.inBlackList(classInfo)) || exists){
            String fullName = prevPackage + "." + className;
            skippedClasses.add(fullName);
            if(exists){
                Log.info("exists(@)", fullName);
            }
            return;
        }
        CompilationUnit compilationUnit = parent.clone();
        ImportProcessor.process(classInfo, this, compilationUnit.getImports());

        String aPackage = prevPackage.replace("mindustry.", "gas.");
        Log.info("[@]", className);
        compilationUnit.setPackageDeclaration(aPackage);


//        Log.info("types");
//transfromning class names, super types and constructors
        for(ClassOrInterfaceDeclaration declaration : compilationUnit.findAll(ClassOrInterfaceDeclaration.class)){
            declaration.setName(transform.type(declaration.getNameAsString()));
            for(int i = 0; i < declaration.getExtendedTypes().size(); i++){

                declaration.getExtendedTypes(i).accept(new TypeTransformVisitor<>(transform),null);
//                declaration.setExtendedType(i,new ClassOrInterfaceType())
            }
            for(ConstructorDeclaration constructor : declaration.getConstructors()){
                constructor.setName(declaration.getNameAsString());
            }
            SmartTypeReplacer.handle(declaration,transform);
        }
        compilationUnit.accept(new ModifierVisitor<Void>(){
            boolean returnSwitch = false;

            @Override
            public Visitable visit(VariableDeclarator n, Void arg){
                Type clone = n.getType().clone();
                Visitable visit = super.visit(n, arg);
                String asString;
                if(clone.isClassOrInterfaceType() && ((asString = clone.asClassOrInterfaceType().getNameAsString()).equals("Block") || asString.equals("Building"))){
                    n.setType(clone);
                }
                return visit;
            }

            @Override
            public Visitable visit(ClassOrInterfaceDeclaration declaration, Void arg){
//                boolean extra= declaration.getExtendedTypes().toString().contains("mindustry");
                ClassOrInterfaceDeclaration visit = (ClassOrInterfaceDeclaration)super.visit(declaration, arg);
                for(int i = 0; i < visit.getExtendedTypes().size(); i++){
                    ClassOrInterfaceType current = visit.getExtendedTypes(i);
                    if(current.toString().contains("mindustry.world")){
                        String strType = current.toString().replace("mindustry.world", "gas.world");
                        Optional<ClassOrInterfaceType> result = javaParser.parseClassOrInterfaceType(strType).getResult();
                        if(result.isPresent()){
                            visit.setExtendedType(i, result.get());
                        }else{
                            Log.err("Cannot parseClassOrInterfaceType: @", strType);
                        }
                    }
                }
                return visit;
            }

            @Override
            public Visitable visit(SimpleName simpleName, Void arg){
                String type = transform.type(simpleName.getIdentifier());
                //TODO commented transformation
//                simpleName.setIdentifier(type);
                return super.visit(simpleName, arg);
            }

            @Override
            public Visitable visit(ForEachStmt forEachStmt, Void arg){
                VariableDeclarationExpr variable = forEachStmt.getVariable().clone();
                super.visit(forEachStmt, arg);
                forEachStmt.setVariable(variable);
                for(VariableDeclarator variableVariable : variable.getVariables()){
                    variableVariable.setType(new VarType());
                }
                return forEachStmt;
            }

            @Override
            public Visitable visit(MethodCallExpr method, Void arg){
                super.visit(method, arg);
                try{
                    Node node = method.getParentNode().orElse(null);
                    if(node instanceof VariableDeclarator){
                        VariableDeclarator var = (VariableDeclarator)node;
                        if(var.getParentNode().orElse(null) instanceof VariableDeclarationExpr){
                            if(!var.getType().isPrimitiveType()){
//                                var.setType(new VarType());
                            }
                        }
                    }
                    return method;
                }catch(Exception exception){
                    Log.err("Cannot resolve method: @", method.getNameAsString());
                    return method;
                }catch(NoClassDefFoundError classDefFoundError){
                    Log.info("Cannot find class: @", classDefFoundError.getMessage());
                    return method;
                }
            }

            @Override
            public Visitable visit(MethodDeclaration declaration, Void arg){
                if(declaration.getBody().isPresent()){
                    BlockStmt body = declaration.getBody().get();
                    handleStatement(body);
                }
//                NodeList<Parameter> parameters = (NodeList<Parameter>)new CloneVisitor().visit(declaration.getParameters(), null);
//                Visitable visit = super.visit(declaration, arg);
//                declaration.setParameters(parameters);

                return super.visit(declaration, arg);
            }

            @Override
            public Visitable visit(SwitchEntry entry, Void arg){
                SwitchEntry visit = (SwitchEntry)super.visit(entry, arg);
                if(returnSwitch){
                    if(entry.getStatements().size() == 1){
                        ReturnStmt returnStmt = new ReturnStmt(entry.getStatement(0).asExpressionStmt().getExpression());
                        entry.getStatements().set(0, returnStmt);
                    }else{
                        Log.info("Cannot transform entry: @", entry);
                    }
                }
                return visit;
            }

            @Override
            public Visitable visit(ReturnStmt returnStmt, Void arg){
                if(returnStmt.getExpression().isPresent()){
                    Expression expression = returnStmt.getExpression().get();
                    if(expression.isSwitchExpr()){
                        returnSwitch = true;
                    }
                }
                Visitable visit = super.visit(returnStmt, arg);
                if(returnSwitch){
//                    visit=new BlockComment("here was return switch");
                    SwitchStmt switchStmt = new SwitchStmt();
                    SwitchExpr switchExpr = returnStmt.getExpression().get().asSwitchExpr();
                    switchStmt.setEntries(switchExpr.getEntries());
                    switchStmt.setSelector(switchExpr.getSelector());
                    visit = switchStmt;
                }
                returnSwitch = false;
                return visit;
            }
        }, null);
        for(TypeDeclaration<?> type : compilationUnit.findAll(TypeDeclaration.class)){
            if(type.isClassOrInterfaceDeclaration()){
                for(AnnotationExpr annotation : type.getAnnotations()){
                    if(annotation.getNameAsString().contains("Component")){
                        type.remove();
                        break;
                    }
                }
            }
            NodeList<Modifier> modifiers = type.getModifiers();
            if(modifiers.contains(Modifier.publicModifier()) &&
            (type.isEnumDeclaration() || (type.isClassOrInterfaceDeclaration() && modifiers.contains(Modifier.staticModifier())))
            ){
                Seq<String> path = new Seq<>();
                type.walk(TreeTraversal.PARENTS, parentNode -> {
                    if(parentNode instanceof NodeWithSimpleName){
                        path.insert(0, ((NodeWithSimpleName<?>)parentNode).getNameAsString());
                    }else if(parentNode instanceof CompilationUnit){
                        ((CompilationUnit)parentNode).getPackageDeclaration().ifPresent(packageDeclaration -> {
                            path.insert(0, packageDeclaration.getNameAsString());
                        });
                    }
                });
                path.add(type.getNameAsString());
                compilationUnit.addImport(path.toString(".").replace("Gas", "").replace("gas.", "mindustry."), false, false);
                type.remove();
            }
        }

        for(FieldDeclaration parentField : compilationUnit.findAll(FieldDeclaration.class)){
            for(VariableDeclarator variable : parentField.getVariables()){
                String fieldName = variable.getNameAsString();
                for(AnnotationExpr annotation : parentField.getAnnotations()){
//                    annotation.setName(transform.annotation(annotation.getNameAsString()));
                }
                //TODO commented transform
                if(transform.fields.contains(fieldName) && false){
                    FieldDeclaration declaration = new FieldDeclaration(parentField.getModifiers(),
                    new VariableDeclarator(variable.getType(), transform.field(fieldName))
                    );
                    for(AnnotationExpr annotation : parentField.getAnnotations()){
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
                    Node rootNode = parentField.getParentNode().get();
                    NodeList<BodyDeclaration<?>> members = ((TypeDeclaration<?>)rootNode).getMembers();
                    members.add(members.indexOf(parentField) + 1, declaration);
                    if(moreLogs) Log.info("new field: @", transform.field(fieldName));
                }
            }
        }
        for(AnnotationExpr annotationExpr : compilationUnit.findAll(AnnotationExpr.class)){
//            annotationExpr.setName(transform.annotation(annotationExpr.getNameAsString()));
        }
        boolean liquid = false;
        if(compilationUnit.getPackageDeclaration().get().getNameAsString().contains("gas.world.blocks.liquid")){
            liquid = true;
            String newName = gasBlockName.replace("Liquid", "Gas");
            if (!newName.contains("GasGasBlock")){
                newName=newName.replace("GasGas","Gas");
            }
            if (exists(newName)){
                Log.info("gasBlockExists(@.@)",classInfo.packageName(),newName);
            }else{
//                Log.info("canMake: @.@",classInfo.packageName(),newName);
                fromLiquidBlock(gasBlockName, /*file,*/ compilationUnit.clone());
            }
        }
        if(!liquid || gasBlockName.contains("Liquid")) saveCompilationUnitAndAddCreated(compilationUnit, aPackage, gasBlockName);
//        blockDir.child(gasBlockName).writeString(compilationUnit.toString());
    }

    private boolean exists(String gasBlockName){
        boolean[] exists = {false};
        mainBlockDir.walk(fi -> {
            if(fi.name().equals(gasBlockName)){
                exists[0] = true;
            }
        });
        return exists[0];
    }

    private void saveCompilationUnitAndAddCreated(CompilationUnit aClass, String aPackage, String className){
        created.add(aClass);
        saveCompilationUnit(aClass, aPackage, className);
    }

    private @Nullable
    CompilationUnit getCompilationUnit(String aPackage, String className){
        Fi file = classFile(aPackage, className);
        CompilationUnit result = null;
        if(file.exists()){
            ParseResult<CompilationUnit> parse = javaParser.parse(file.readString());
            if(parse.getResult().isPresent()){
                result = parse.getResult().get();
            }
        }
        return result;
    }

    private Fi classFile(String aPackage, String className){
        return coreDir.child(aPackage.replace(".", "/") + "/" + (className.contains(".") ? className : className + ".java"));
    }

    private void saveCompilationUnit(CompilationUnit aClass, String aPackage, String className){
        classFile(aPackage, className).writeString(aClass.toString());
    }

    private void fromLiquidBlock(String name, /*Fi file,*/ CompilationUnit compilationUnit){
        String gasBlock = name.replace("Liquid", "Gas");
        if(!gasBlock.contains("GasGasBlock")){
            gasBlock = gasBlock.replace("GasGas", "Gas");
        }
        PackageDeclaration packageDeclaration = compilationUnit.getPackageDeclaration().get();
        packageDeclaration.setName(packageDeclaration.getNameAsString().replace(".liquid", ".gas"));
        compilationUnit.accept(new ModifierVisitor<Void>(){
            boolean field = false;

            @Override
            public Visitable visit(SimpleName simpleName, Void arg){
                simpleName.setIdentifier(simpleName.getIdentifier()
                .replace("Liquids", "Gasses")
                .replace("Liquid", "Gas")
                .replace("liquids", "gasses")
                .replace("liquid", "gas")
                .replace("GasGasBlock", "GAS_GAS_BLOCK")
                .replace("GasGas", "Gas")
                .replace("GAS_GAS_BLOCK", "GasGasBlock")
                );
                return super.visit(simpleName, arg);
            }

            @Override
            public Visitable visit(MethodReferenceExpr methodReference, Void arg){
                super.visit(methodReference, arg);
                if(methodReference.getScope().isTypeExpr()){
                    Type type = methodReference.getScope().asTypeExpr().getType();
                    if(type.isClassOrInterfaceType() && type.asClassOrInterfaceType().getNameAsString().equals("GasBuilding")){
                        type.asClassOrInterfaceType().setName("Building");
                    }
                }
                return methodReference;
            }

            @Override
            public Visitable visit(FieldAccessExpr fieldAccess, Void arg){

                Visitable visit = super.visit(fieldAccess, arg);
                if(fieldAccess.getScope().toString().equals("Blocks")){
                    SimpleName simpleName = fieldAccess.getName();
                    String identifier = simpleName.getIdentifier();
                    String className = null;
                    if(identifier.equals("gasJunction") || identifier.equals("liquidJunction")){
                        className = "GasJunction";
                    }else if(identifier.equals("bridgeConduit")){
                        className = "GasBridge";
                    }else if(identifier.equals("ductBridge")){
                        className = "GasDuctBridge";
                    }
                    if(className != null){
                        ParseResult<Expression> parse = javaParser.parseExpression("Vars.content.blocks().find(b->b instanceof " + className + ")");
                        if(parse.isSuccessful()){
                            Expression expression = parse.getResult().get();
                            Log.info("expression: @", expression);
                            Node parentNode = fieldAccess.getParentNode().get();
                            parentNode.replace(fieldAccess, expression);
                            expression.setParentNode(parentNode);
                            return expression;
                        }else{
                            throw new RuntimeException(parse.toString());
                        }
                    }
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
        saveCompilationUnitAndAddCreated(compilationUnit, packageDeclaration.getNameAsString(), gasBlock.substring(0, gasBlock.indexOf(".")));
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
                    if(moreLogs) Log.info("new field: @", transform.field(fieldName));
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

    public String getName(Node node){
        boolean named = node instanceof NodeWithSimpleName;
        String name = node.getClass().getSimpleName();
        if(named){
            name += "[" + ((NodeWithSimpleName<?>)node).getNameAsString() + "]";
        }
        return name;
    }

    private boolean needTransformName(String name){
        return !existsClasses.containsKey(name);
    }
}
