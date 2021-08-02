package gas.annotations.dependencies;

import arc.files.Fi;
import arc.util.serialization.Json;
import arc.util.serialization.Jval;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import gas.annotations.GasBaseProcessor;
import mindustry.mod.Mods;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Modifier;


@SupportedAnnotationTypes("gas.annotations.GasAnnotations.CashAnnotation2")
public class DependenciesProc extends GasBaseProcessor {
    @Override
    public void process(RoundEnvironment env) throws Exception {
        TypeSpec.Builder builder = TypeSpec.classBuilder("GasDependencies").addModifiers(Modifier.PUBLIC, Modifier.FINAL);


        //valid method
        MethodSpec.Builder valid = MethodSpec.methodBuilder("valid").addModifiers(Modifier.PUBLIC, Modifier.STATIC).returns(TypeName.get(boolean.class));
        Json json = new Json();
        Fi metaf = rootDirectory.child("mod.hjson");
        Mods.ModMeta modMeta = json.fromJson(Mods.ModMeta.class, Jval.read(metaf.readString()).toString(Jval.Jformat.plain));
        valid.addStatement("boolean valid=true");
        valid.beginControlFlow("try");

        for (String dependency : modMeta.dependencies) {
            valid.addStatement("valid&=exists($S)", dependency);
        }
        valid.nextControlFlow("catch(Exception e)");
        valid.addStatement("e.printStackTrace()");
        valid.addStatement("valid=false");
        valid.endControlFlow();
        valid.addStatement("return valid");
        builder.addMethod(valid.build());
        //exists method
        MethodSpec.Builder existsBuilder = MethodSpec.methodBuilder("exists").addParameter(String.class, "mod").addModifiers(Modifier.PUBLIC, Modifier.STATIC).returns(TypeName.get(boolean.class));
        existsBuilder.addStatement("arc.util.serialization.Json json = new arc.util.serialization.Json()");
        existsBuilder.beginControlFlow("for (arc.files.Fi fi : mindustry.Vars.modDirectory.list())");
        existsBuilder.addStatement("arc.files.Fi zip = fi.isDirectory() ? fi : (new arc.files.ZipFi(fi))");
        existsBuilder.addStatement("arc.files.Fi metaf =zip.child(\"mod.json\").exists() ? zip.child(\"mod.json\") : zip.child(\"mod.hjson\")");
        existsBuilder.addStatement("if (!metaf.exists()) continue");
        existsBuilder.addStatement("if (json.fromJson(mindustry.mod.Mods.ModMeta.class, arc.util.serialization.Jval.read(metaf.readString()).toString(arc.util.serialization.Jval.Jformat.plain)).name.equals(mod)) return arc.Core.settings.getBool(\"mod-\"+mod+\"-enabled\",false)");
        existsBuilder.endControlFlow();
        existsBuilder.addStatement("return false");
        builder.addMethod(existsBuilder.build());

        write(builder);
//        super.process(env);
    }
}
