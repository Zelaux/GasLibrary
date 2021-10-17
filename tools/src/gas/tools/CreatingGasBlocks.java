package gas.tools;

import arc.files.*;
import arc.func.*;
import arc.struct.*;
import arc.util.*;
import com.github.javaparser.*;
import com.github.javaparser.ParserConfiguration.*;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.visitor.*;
import com.github.javaparser.symbolsolver.*;
import com.github.javaparser.symbolsolver.resolution.typesolvers.*;
import com.github.javaparser.utils.Log.*;
import gas.tools.gasBlockConverter.*;
import gas.tools.parsers.*;
import gas.tools.parsers.resolution.*;

import java.io.*;

public class CreatingGasBlocks{
    public static JavaParser javaParser = new JavaParser();
    static GasBlocksConverter converter;
    static Seq<String> transformNames = Seq.with("Block", "Building", "Placement");
    static Seq<String> transformFields = Seq.with();
    static Seq<String> mindustryAnnotations = Seq.with();
    static ObjectMap<String, CompilationUnit> existsClasses = ObjectMap.of();
    static ObjectMap<String, CompilationUnit> classMap = ObjectMap.of();
    static Seq<String> mustTransform = new Seq<>();
    static Fi
    coreDir = new Fi("core/src"),
    gasDir = coreDir.child("gas"),
    worldDir = gasDir.child("world");

    public static void main(String[] args) throws IOException{
        com.github.javaparser.utils.Log.setAdapter(new StandardOutStandardErrorAdapter());

        String mindustryVersion = Seq.with(args).find(s -> s.startsWith("v"));
        if(mindustryVersion == null){
            System.out.println("Please put mindustry version in args!!!");
            System.exit(1);
            return;
        }

        Log.info("Starting creating gasBlocks for version " + mindustryVersion);

        LibrariesDownloader.download(mindustryVersion);

        Fi compDownloader = new Fi("compDownloader");

        ZipFi sourceZip = LibrariesDownloader.coreZip();
        ZipFi arcZip = LibrariesDownloader.arcZip();

        Fi libDir = compDownloader.child("mindustryLib");

        libDir.deleteDirectory();


        Func<String, Fi> library = name -> {
            String realName = name.substring(0, name.indexOf("/"));
            Fi realFi = libDir.child(realName);
            if(!realFi.exists()){
                Fi fi = sourceZip.list()[0];
                for(String part : name.split("/")){
                    fi = fi.child(part);
                }
                fi.copyTo(realFi);
                Log.info("added library: @", name);
            }
            return realFi;
        };

        Fi gasLib = libDir.child("gas"), arcLib = libDir.child("arc");
        coreDir.child("gas").copyTo(gasLib);
        arcZip.list()[0].child("arc-core").child("src").child("arc").copyTo(arcLib);
//        addArcLib("");


        CacheTypeSolver cacheTypeSolver;
        CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(new JavaParserTypeSolver(library.get("core/src/mindustry").file()));
        typeSolver.add(new JavaParserTypeSolver(library.get("annotations/src/main/java/mindustry").file()));
        typeSolver.add(new JavaParserTypeSolver(gasLib.file()));
        typeSolver.add(new JavaParserTypeSolver(arcLib.file()));
        typeSolver.add(new ReflectionTypeSolver(false));
        typeSolver.add(cacheTypeSolver = new CacheTypeSolver());
//        MemoryTypeSolver memoryTypeSolver = ;
        typeSolver.add(new MemoryTypeSolver());
//        memoryTypeSolver.addDeclaration();

        ParserConfiguration parserConfiguration = javaParser.getParserConfiguration();
        parserConfiguration.setSymbolResolver(new JavaSymbolSolver(typeSolver));
        parserConfiguration.setLanguageLevel(LanguageLevel.JAVA_16_PREVIEW);
        parserConfiguration.getPostProcessors().add(cacheTypeSolver);
        worldDir.walk(CreatingGasBlocks::addClass);

        Fi root = sourceZip.list()[0];
        Fi worldRoot = root.child("core").child("src").child("mindustry").child("world");
        //collecting each annotation
        Fi annotationsRoot = root.child("annotations").child("src").child("main").child("java").child("mindustry").child("annotations").child("Annotations.java");
        compilationUnit(annotationsRoot, unit -> {
            unit.accept(new ModifierVisitor<Void>(){
                @Override
                public Visitable visit(AnnotationDeclaration n, Void arg){
                    mindustryAnnotations.add(n.getNameAsString());
                    return super.visit(n, arg);
                }
            }, null);
        });
        //collecting each block class
        for(Fi blocksDir : worldRoot.child("blocks").list()){
            if(blocksDir.isDirectory()){
                blocksDir.walk(CreatingGasBlocks::addTransforms);
            }
        }
        //collecting each blockDraw class
        mustTransform.add("DrawBlock");
        worldRoot.child("draw").walk(CreatingGasBlocks::addTransforms);

        //collecting each class
        root.child("core").child("src").child("mindustry").walk(fi -> {
            compilationUnit(fi, aClass -> {
                String name = fi.nameWithoutExtension();
                if(aClass.getPackageDeclaration().isPresent()){
                    name = aClass.getPackageDeclaration().get().getNameAsString() + "." + fi.nameWithoutExtension();
                }
                classMap.put(name, aClass);
            });
        });


        converter = new GasBlocksConverter(new NameTransform(transformNames, transformFields, mindustryAnnotations));

        worldDir.child("blocks").child("trans.txt").writeString(converter.transform.names.toString("\n"));
        worldDir.child("blocks").child("transField.txt").writeString(converter.transform.fields.toString("\n"));
        worldDir.child("blocks").child("mindustryAnnotations.txt").writeString(mindustryAnnotations.toString("\n"));
        worldDir.child("blocks").child("existsClasses.txt").writeString(existsClasses.keys().toSeq().toString("\n"));
        worldDir.child("blocks").child("classMap.txt").writeString(classMap.keys().toSeq().toString("\n"));

        converter.classMap.putAll(classMap);
        converter.existsClasses.putAll(existsClasses);

        Fi.get("core/src").walk(fi -> {
            if(fi.extension().equals("java")){
                compilationUnit(fi, compilationUnit -> converter.compilationUnitPackageImport(compilationUnit));
            }
        });
        converter.extraImport.add(new ImportDeclaration("mindustry.gen", false, true));
        converter.extraImport.add(new ImportDeclaration("gas.world.blocks.production.GasGenericCrafter", false, true));

        converter.gasBlock = existsClasses.get("GasBlock");
        compilationUnit(gasDir.child("gen").child("GasBuilding.java"), compilationUnit -> {
            converter.gasBuilding = compilationUnit;
        });

//        if(true) return;
        converter.run();

        Fi coreDir = Fi.get("core/src");
        final Fi needToReplaceFile = coreDir.child("gas/world/blocks").child("needToReplace.txt");
        needToReplaceFile.writeString("");
        coreDir.child("gas/world/blocks").walk(fi -> {
            if(fi.extension().equals("java")){
                compilationUnit(fi, gasBlock -> {
                    gasBlock.accept(new ModifierVisitor<Void>(){
                        ClassOrInterfaceDeclaration currentClass;

                        @Override
                        public Visitable visit(SimpleName simpleName, Void arg){
                            super.visit(simpleName, arg);
                            String lowerName = simpleName.getIdentifier().toLowerCase();
                            if(lowerName.contains("liquid")){
                                Node node = simpleName.getParentNode().get();
                                StringBuilder builder = new StringBuilder();
//                    builder.append(simpleName);
                                while(!(node instanceof CompilationUnit)){
                                    if(builder.length() == 0) builder.insert(0, ".");
                                    builder.insert(0, converter.getName(node)
                                    .replace("ClassOrInterfaceDeclaration", "")
                                    .replace("MethodDeclaration", "")
                                    .replace("FieldDeclaration", "")
                                    );
                                    node = node.getParentNode().get();
                                }

                                needToReplaceFile.writeString(builder.toString() + "\n", true);
                            }
                            return simpleName;
                        }

                        @Override
                        public Visitable visit(ClassOrInterfaceDeclaration n, Void arg){
                            ClassOrInterfaceDeclaration prev = currentClass;
                            currentClass = n;
                            super.visit(n, arg);
                            currentClass = prev;
                            return n;
                        }
                    }, null);
                });
            }
        });
        libDir.deleteDirectory();
    }

    public static void compilationUnit(Fi fi, Cons<CompilationUnit> cons){
        try{
//            String convert = codeConverter.convert(fi.readString(), fi.nameWithoutExtension());
            ParseResult<CompilationUnit> parseResult = javaParser.parse(fi.readString());
            if(!parseResult.getProblems().isEmpty()){
                Log.info("@ has problems:", fi.nameWithoutExtension());
                for(Problem problem : parseResult.getProblems()){
                    Log.info("\t@", problem);
                }
            }
            CompilationUnit compilationUnit = parseResult.getResult().get();
            cons.get(compilationUnit);
            ;
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    private static void addTransforms(Fi fi){

        compilationUnit(fi, compilationUnit -> {
            compilationUnit.accept(new ModifierVisitor<Void>(){
                @Override
                public Visitable visit(ClassOrInterfaceDeclaration declaration, Void arg){

                    if((!declaration.isInterface() && declaration.getExtendedTypes().size() > 0) || mustTransform.contains(declaration.getNameAsString())){
                        transformNames.add(declaration.getNameAsString());
                    }
                    return super.visit(declaration, arg);
                }
            }, null);
        });
    }


    private static void addClass(Fi fi){

        if(fi.isDirectory() || !fi.exists() || !fi.extension().equals("java")) return;
        try{
            ParseResult<CompilationUnit> parseResult = javaParser.parse(fi.readString());
            CompilationUnit compilationUnit = parseResult.getResult().get();
            existsClasses.put(fi.nameWithoutExtension(), compilationUnit);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
