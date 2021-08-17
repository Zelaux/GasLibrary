package gas.tools.gasBlockConverter;

import arc.files.*;
import arc.func.*;
import arc.struct.*;
import arc.util.*;
import com.github.javaparser.*;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.symbolsolver.*;
import com.github.javaparser.symbolsolver.resolution.typesolvers.*;

import java.io.*;

public class CreatingGasBlocks{
    static GasBlocksConverter converter;
    static JavaParser javaParser = new JavaParser();
    static Seq<String> transformNames = Seq.with("Block", "Building");
    static Seq<String> transformFields = Seq.with();
    static Seq<String> mindustryAnnotations = Seq.with();
    static ObjectMap<String, CompilationUnit> existsClasses = ObjectMap.of();
    static ObjectMap<String, ClassEntry> classMap = ObjectMap.of();

    public static void main(String[] args) throws IOException{
        Fi compDownloader = new Fi("compDownloader");
        Fi sourcesFi = compDownloader.child("sources.zip");
        Fi dir = new Fi("core/src/gas/world");
//        blockDir = genDir.child("blocks");
//        blockDir.mkdirs();
        dir.walk(CreatingGasBlocks::addClass);
        ZipFi sourceZip = new ZipFi(sourcesFi);
        Fi libDir = compDownloader.child("mindustryLib");
        libDir.deleteDirectory();
        Func<String, Fi> library = name -> {
            String realName = name.substring(0, name.indexOf("/"));
            if(!libDir.child(name).exists()){
                for(Fi fi : sourceZip.list()[0].list()){
                    if(fi.name().equals(realName)){
                        fi.copyTo(libDir);
                    }
                }
                Log.info("added library: @", name);
            }
            return libDir.child(name);
        };

        CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(new JavaParserTypeSolver(library.get("core/src").file()));
        typeSolver.add(new JavaParserTypeSolver(library.get("annotations/src/main/java").file()));
        typeSolver.add(new ReflectionTypeSolver(false));
        typeSolver.add(new MemoryTypeSolver());
        javaParser.getParserConfiguration().setSymbolResolver(new JavaSymbolSolver(typeSolver));

        Fi root = sourceZip.list()[0];
        Fi worldRoot = root.child("core").child("src").child("mindustry").child("world");
        Fi annotationsRoot = root.child("annotations").child("src").child("main").child("java").child("mindustry").child("annotations").child("Annotations.java");
        compilationUnit(annotationsRoot, unit -> {
            for(TypeDeclaration<?> type : unit.getTypes()){
                if(type.isClassOrInterfaceDeclaration()){
                    for(BodyDeclaration<?> member : type.asClassOrInterfaceDeclaration().getMembers()){
                        if(member.isAnnotationDeclaration()){
                            mindustryAnnotations.add(member.asAnnotationDeclaration().getNameAsString());
                        }
                    }
                }
            }
        });
//        Fi blocksRoot = worldRoot.child("blocks");
//        blocksRoot.deleteDirectory();
        worldRoot.child("blocks").walk(fi -> {
            addTransforms(fi);
            if(fi.parent().nameWithoutExtension().equals("liquid")){
//                addTransforms(fi);
            }
        });
        worldRoot.child("draw").walk(fi -> {
            addTransforms(fi);
            if(fi.parent().nameWithoutExtension().equals("liquid")){
//                addTransforms(fi);
            }
        });
        worldRoot.walk(fi -> {
            compilationUnit(fi, compilationUnit -> {
                classMap.put(fi.nameWithoutExtension(), new ClassEntry(fi, compilationUnit));
            });
        });
        dir.child("trans.txt").writeString(transformNames.toString("\n"));
        dir.child("transField.txt").writeString(transformFields.toString("\n"));


        converter = new GasBlocksConverter(transformNames, transformFields, mindustryAnnotations, classMap, existsClasses);
        converter.run();
        libDir.deleteDirectory();
    }

    public static void compilationUnit(Fi fi, Cons<CompilationUnit> cons){
        try{
            ParseResult<CompilationUnit> parseResult = javaParser.parse(fi.readString());
            CompilationUnit compilationUnit = parseResult.getResult().get();
            cons.get(compilationUnit);
            ;
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    private static void addTransforms(Fi fi){
        compilationUnit(fi, compilationUnit -> {
            for(TypeDeclaration<?> type : compilationUnit.getTypes()){
                addTransforms(type);
            }
        });
    }

    private static void addTransforms(BodyDeclaration<?> type){
        if(type.isClassOrInterfaceDeclaration()){
            ClassOrInterfaceDeclaration declaration = type.asClassOrInterfaceDeclaration();
            if(!declaration.isInterface() && declaration.getExtendedTypes().size() > 0){
                transformNames.add(declaration.getNameAsString());
            }
        }
        if(type.isFieldDeclaration()){
            FieldDeclaration declaration = type.asFieldDeclaration();
            for(VariableDeclarator variable : declaration.getVariables()){
                String varName = variable.getNameAsString();
//                Log.info("varName: @", varName);
                if(varName.toLowerCase().contains("liquid")){
                    transformFields.add(varName);
                }
            }
        }
        if(type.isTypeDeclaration()){
            TypeDeclaration<?> declaration = type.asTypeDeclaration();
            for(BodyDeclaration<?> member : declaration.getMembers()){
                addTransforms(member);
            }
        }
    }

    private static void addClass(Fi fi){

        if(fi.isDirectory() || !fi.exists()) return;
        try{
            ParseResult<CompilationUnit> parseResult = javaParser.parse(fi.readString());
            CompilationUnit compilationUnit = parseResult.getResult().get();
            existsClasses.put(fi.name(), compilationUnit);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
