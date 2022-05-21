package gas.tools.gasBlockConverter;

import arc.func.*;
import arc.struct.*;
import com.github.javaparser.ast.*;

public class ImportProcessor{
    private static final ExtraImports extraImports = new ExtraImports(new ObjectSet<>(), new ObjectSet<>());
    private static final ObjectSet<String> illegalImports = ObjectSet.with(
    "gas.Vars",
    "gas.annotations.Annotations",
    "gas.core",
    "gas.entities.Units",
    "gas.entities.bullet",
    "gas.entities.units",
    "gas.game",
    "gas.game.EventType",
    "gas.graphics",
    "gas.input",
    "gas.io.TypeIO",
    "gas.logic",
    "gas.ui.dialogs",
    "gas.world.blocks.ConstructBlock",
    "gas.world.blocks.environment",
    "gas.world.blocks.experimental",
    "gas.world.blocks.legacy",
    "gas.world.blocks.storage.CoreBlock",
    "gas.entities.pattern",
    "gas.ctype",
    "gas.world.blocks.units.UnitAssemblerModule",
    "gas.world.blocks.units.UnitAssembler",
    "gas.entities.part"
    );
    private static final ObjectSet<String> illegalImportsStarts = ObjectSet.with(
    "gas.ai.",
    "gas.core.",
    "gas.entities.bullet.",
    "gas.entities.units.",
    "gas.game.",
    "gas.graphics.",
    "gas.input.",
    "gas.logic.",
    "gas.logic."
    );

    public static void reset(){
//        extraImports.clear();
    }

    public static void extraImport(String name, boolean isStatic, boolean isAsteric){
        extraImport(newImport(name, isStatic, isAsteric));
    }
private static void removeIllegal(ObjectSet<String>... importSets){
    Seq<String> illegalImportsStarts = ImportProcessor.illegalImportsStarts.toSeq();
    for(ObjectSet<String> importSet : importSets){
        for(String oneImport : importSet.toSeq()){
            if (illegalImports.contains(oneImport) || illegalImportsStarts.contains(oneImport::startsWith)){
                importSet.remove(oneImport);
            }
        }
    }
}
    private static void extraImport(ImportDeclaration newImport){

        addImportInto(extraImports.asteriskImports(), extraImports.staticImports(), newImport);
        removeIllegal(extraImports.asteriskImports(),extraImports.staticImports());
    }

    public static void extraImport(String name){
        extraImport(newImport(name));
    }

    public static void compilationUnitPackageImport(CompilationUnit compilationUnit){
        compilationUnitPackageImport(compilationUnit, s -> s);
    }

    public static void compilationUnitPackageImport(CompilationUnit compilationUnit, Func<String, String> transformer){
        if(compilationUnit.getPackageDeclaration().isPresent()){
            PackageDeclaration aPackage = compilationUnit.getPackageDeclaration().get();
            extraImport(newImport(transformer.get(aPackage.getNameAsString())));
        }
    }

    public static void process(ClassInfo classInfo, GasBlocksConverter converter, NodeList<ImportDeclaration> imports){
        String className = classInfo.className();
        String packageName = classInfo.packageName();
        ObjectSet<String> asteriskImports = new ObjectSet<>(extraImports.asteriskImports());
        ObjectSet<String> staticImports = new ObjectSet<>(extraImports.staticImports());

        for(ImportDeclaration anImport : imports.toArray(new ImportDeclaration[0])){
            addImportInto(asteriskImports, staticImports, anImport);
            addImportInto(asteriskImports, staticImports, anImport, name -> name
            .replace("mindustry", "gas")
            );
        }
        imports.clear();
        asteriskImports.add(packageName);
        asteriskImports.add(packageName + "." + className);
        if(packageName.equals("mindustry.world.blocks.defense.turrets")){
            asteriskImports.add("mindustry.world.blocks.defense.turrets.Turret");
        }
        if(asteriskImports.remove("gas.world.blocks.production.GenericCrafter")){
            asteriskImports.add("gas.world.blocks.production.GasGenericCrafter");
        }
        removeIllegal(asteriskImports,staticImports);
//        imports.remove("import gas.entities.units.*;");
        for(String asteriskImport : asteriskImports){
            imports.add(newImport(asteriskImport));
        }
        for(String staticImport : staticImports){
            imports.add(newImport(staticImport).setStatic(true));
        }
    }

    private static void addImportInto(ObjectSet<String> asteriskImports, ObjectSet<String> staticImports, ImportDeclaration anImport){
        addImportInto(asteriskImports, staticImports, anImport, s -> s);
    }

    private static void addImportInto(ObjectSet<String> asteriskImports, ObjectSet<String> staticImports, ImportDeclaration anImport, Func<String, String> nameTransformer){
        String importName = anImport.getNameAsString();

        String asteriskImportName = importName;
        if(!anImport.isAsterisk()){
            asteriskImportName = asteriskImportName.substring(0, asteriskImportName.lastIndexOf("."));
        }
        (anImport.isStatic() ? staticImports : asteriskImports).add(nameTransformer.get(asteriskImportName));
    }

    private static ImportDeclaration newImport(String name, boolean isStatic, boolean isAsteric){
        return new ImportDeclaration(name, isStatic, isAsteric);
    }

    private static ImportDeclaration newImport(String name){
        return newImport(name, false, true);
    }
}

record ExtraImports(ObjectSet<String> asteriskImports, ObjectSet<String> staticImports){
};