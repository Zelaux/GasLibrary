package gas.tools.gasBlockConverter;

import arc.struct.*;
import arc.util.*;
import com.github.javaparser.*;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.visitor.*;
import gas.tools.parsers.visitors.*;

public class SmartTypeReplacer{
    public static void main(String[] args){
        String testMethod = """
        void test(){
            if (tile.block() != MessageBlock.this) {
                dialog.hide();
            }
        }
        """;
        NameTransform generatorBuild = new NameTransform(Seq.with("MessageBlock"), Seq.with(), Seq.with());
        MethodDeclaration declaration = StaticJavaParser.parseMethodDeclaration(testMethod);
        declaration.accept(new TypeTransformVisitor<>(generatorBuild){
            @Override
            public Visitable visit(SimpleName simpleName, Object arg){
                return super.visit(simpleName, arg);
            }
        }, null);
        Log.info("result: @", declaration);
    }

    public static void handle(ClassOrInterfaceDeclaration declaration, NameTransform transform){
        CompilationUnit compilationUnit = declaration.findCompilationUnit().get();
        PackageDeclaration packageDeclaration = compilationUnit.getPackageDeclaration().get();
        String packageName = packageDeclaration.getNameAsString();
        String className = declaration.getNameAsString();
        ClassInfo classInfo = new ClassInfo(packageName, className);

        //check for Struct class
        for(AnnotationExpr annotation : declaration.getAnnotations()){
            if(annotation.getNameAsString().equals("Struct")){
                declaration.remove();
                return;
            }
        }

        if(packageName.startsWith("gas.world.draw")){
            declaration.accept(new TypeTransformVisitor<>(transform), null);
            return;
        }else{
            ObjectSet<String> validNames = ObjectSet.with(className);
            for(ClassOrInterfaceDeclaration other : declaration.findAll(ClassOrInterfaceDeclaration.class)){
                validNames.add(transform.type(other.getNameAsString()));
            }
            if(className.equals("GasImpactReactor")){
                validNames.add("GasGeneratorBuild");
            }
            if(className.equals("GasUnitAssemblerModule")){
                validNames.add("GasUnitAssemblerBuild");
            }
            declaration.accept(new TypeTransformVisitor<>(transform){
                @Override
                protected boolean applyTransform(String name, String into){
                    return validNames.contains(into);
                }
            }, null);
        }
//        declaration.accept(new TypeTransformVisitor<>(transform),null);

        if(className.equals("GasGenericCrafter")){
            declaration.getFieldByName("drawer").get().accept(new TypeTransformVisitor<>(transform), null);
        }
    }
}
