package gas.tools.gasBlockConverter;

import arc.func.*;
import arc.struct.*;
import arc.util.*;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.Modifier.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.nodeTypes.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.*;
import com.github.javaparser.ast.visitor.*;
import com.github.javaparser.resolution.*;
import com.github.javaparser.resolution.declarations.*;
import com.github.javaparser.resolution.types.*;

import static gas.tools.updateVersion.CreatingGasBlocks.javaParser;

public class MethodsFixer extends ModifierVisitor<Void>{
    MethodDeclaration currentMethod;
    final Seq<Parameter> currentParameters=new Seq<>();
    boolean classField = false;
    private NameTransform transform;
    private GasBlocksConverter parent;

    public MethodsFixer(GasBlocksConverter parent){
        this.parent=parent;
        this.transform = parent.transform;
    }


    @Override
    public Visitable visit(MethodDeclaration method, Void arg){
        boolean override = false;
        Visitable visit = method;
        for(AnnotationExpr annotation : method.getAnnotations()){
            if(annotation.getNameAsString().equals("Override")){
                override = true;
            }
        }

        if(method.getBody().isPresent()){

            BlockStmt body = method.getBody().get();

            currentMethod = method;


            NodeList<Statement> statements = body.getStatements();
            for(Parameter parameter : method.getParameters()){
                Type type = parameter.getType();
                if(!type.isClassOrInterfaceType()) continue;
                ClassOrInterfaceType classType = type.asClassOrInterfaceType();
                ClassOrInterfaceType replacement = getReplacement(method, parameter, parent.gasBuilding.getClassByName("GasBuilding").get());
                Log.info("replacement");
                if(transform.names.contains(classType.getNameWithScope()) && replacement!=null) addRenameGasVariable(method, statements, parameter, classType);
                if (replacement!=null){
                    parameter.setType(replacement);
                }

            }
            visit(body, arg);
            currentMethod = null;
        }
        return visit;
    }

    private void addRenameGasVariable(MethodDeclaration method, NodeList<Statement> statements, Parameter parameter, ClassOrInterfaceType classType){
        String varType = transform.type(classType.getNameWithScope());
        String paramName = parameter.getNameAsString();
        String varName = "gas" + Strings.capitalize(paramName);
        IfStmt ifStmt = new IfStmt();
        ifStmt.setCondition(javaParser.parseExpression(Strings.format("!(@ instanceof @)", paramName, varType)).getResult().get());
        statements.add(0, new ExpressionStmt(javaParser.parseVariableDeclarationExpr(Strings.format("@ @=(@)@", varType, varName, varType, paramName)).getResult().get()));
        if(!method.getType().isVoidType()){
            ReturnStmt thenStmt = new ReturnStmt();
            ifStmt.setThenStmt(thenStmt);
            if(method.getType().isPrimitiveType()){
                PrimitiveType primitiveType = method.getType().asPrimitiveType();
                String descriptor = primitiveType.toDescriptor();
                boolean isNumeric = Structs.contains("BSIJFD".split(""), descriptor), isBoolean = descriptor.equals("Z");
                if(isBoolean){
                    thenStmt.setExpression(new BooleanLiteralExpr(false));
                }else if(isNumeric){
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

    protected boolean isInstanceOf(ClassOrInterfaceType type, String parent){
        try{
            for(ResolvedReferenceType allAncestor : type.resolve().getAllAncestors()){
                if(allAncestor.toString().equals(parent)){
                    return true;
                }
            }
            return false;
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    private static ClassOrInterfaceType getReplacement(final MethodDeclaration method, final Parameter parameter, final ClassOrInterfaceDeclaration gasBuilding){
        ResolvedReferenceType building = null;
        final ClassOrInterfaceType
        gasBuildingType = new ClassOrInterfaceType().setName(gasBuilding.getNameAsString()),
        buildingType = new ClassOrInterfaceType().setName("Building");

        BlockStmt body = method.getBody().orElse(null);

        if(!parameter.getType().isClassOrInterfaceType()) return null;

        ClassOrInterfaceType type = parameter.getType().asClassOrInterfaceType();
        String buildingPath = "mindustry.gen.Building";
        boolean buildOrBlock = false;
        for(ResolvedReferenceType ancestor : type.resolve().getAllAncestors()){
            if(buildOrBlock = ancestor.getQualifiedName().equals(buildingPath)){
                building = ancestor.asReferenceType();
                break;
            }
        }
        if(!buildOrBlock) return null;

        Boolf<FieldDeclaration> validModifiers = node -> {
            return (node.hasModifier(Keyword.PUBLIC) || node.hasModifier(Keyword.PROTECTED)) && !node.hasModifier(Keyword.STATIC);
        };
        ClassOrInterfaceType[] returnType = {null};
        ResolvedMethodDeclaration superMethod = getSuperMethod(method);
        if(superMethod != null){
            for(int i = 0; i < superMethod.getNumberOfParams(); i++){
                if(superMethod.getParam(i).getName().equals(parameter.getNameAsString())){
                    return new ClassOrInterfaceType().setName(superMethod.getParam(i).getType().describe());
//                    break;
                }
            }
        }
        Log.info("methodCallExpr: @", superMethod);
        for(ResolvedMethodDeclaration buildingMethod : building.getAllMethods()){
            if(deepEquals(method, buildingMethod)){
                returnType[0] = buildingType;
                break;
            }

        }
        for(MethodDeclaration gasBuildingMethod : gasBuilding.getMethods()){
            if(gasBuildingMethod.equals(method)){
                returnType[0] = gasBuildingType;
                break;
            }
        }
        if(returnType[0] == null && body != null){
            ResolvedReferenceType cacheBuilding = building;
            body.getStatements().accept(new VoidVisitorAdapter<Void>(){
                @Override
                public void visit(FieldAccessExpr accessExpr, Void arg){
                    if(returnType[0] != null) return;
                    super.visit(accessExpr, arg);
//                    Log.info("accessExpr: @", accessExpr);

                    if(accessExpr.getScope().isNameExpr()){
                        NameExpr nameScore = accessExpr.getScope().asNameExpr();
                        for(Parameter param : method.getParameters()){
                            if(param.getNameAsString().equals(nameScore.getNameAsString())){
                                for(ResolvedFieldDeclaration field : cacheBuilding.getDeclaredFields()){
                                    if(field.getName().equals(accessExpr.getNameAsString())){
                                        returnType[0] = buildingType;
                                        return;
                                    }
                                }
                                for(FieldDeclaration field : gasBuilding.getFields()){
                                    for(VariableDeclarator variable : field.getVariables()){
                                        if(validModifiers.get(field) && variable.getNameAsString().equals(accessExpr.getNameAsString())){
                                            returnType[0] = gasBuildingType;
                                            return;
                                        }
                                    }
                                }
                                return;
                            }
                        }
                    }
                }
            }, null);
        }
        if(returnType[0] != null){
            if(returnType[0].getNameAsString().equals(type.getNameAsString())){
                returnType[0] = null;
            }
        }
        return returnType[0];
    }


    private static ResolvedMethodDeclaration getSuperMethod(MethodDeclaration method){
        ResolvedMethodDeclaration resolve = null;
        NodeList<Statement> statements = method.getBody().map(BlockStmt::getStatements).orElse(null);
        if(statements == null) return null;
        Statement node=null;
        try{
            String strStatement = Strings.format("super.@(@);", method.getNameAsString(), Seq.with(method.getParameters()).map(NodeWithSimpleName::getNameAsString).toString(", "));
            node = javaParser.parseStatement(strStatement).getResult().get();
            statements.addFirst(node);
            MethodCallExpr methodCallExpr = node.asExpressionStmt().getExpression().asMethodCallExpr();
            resolve = methodCallExpr.resolve();
        }catch(UnsolvedSymbolException ignored){
        }catch(Throwable other){
            Log.err("something went wrong: \n@", other);
            return null;
        }
        if (node!=null)  statements.removeFirst();
        return resolve;
    }

    private static boolean deepEquals(MethodDeclaration method, ResolvedMethodDeclaration resolved){
        boolean equals =
        method.getParameters().size() == resolved.getNumberOfParams() ||
        method.getNameAsString().equals(resolved.getName());
        if(equals){
            equals = method.getType().toDescriptor().equals(resolved.getReturnType().describe());
        }
        if(equals){
            for(int i = 0; i < method.getParameters().size(); i++){
                ResolvedParameterDeclaration resolvedParam = resolved.getParam(i);
                Parameter param = method.getParameter(i);
                equals &= param.getNameAsString().equals(resolvedParam.getName());
                equals &= param.getType().toDescriptor().equals(resolvedParam.getType().describe());
            }
        }
        return equals;
    }
    private boolean visitMethodBody(MethodDeclaration method){
        return false;
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

}
