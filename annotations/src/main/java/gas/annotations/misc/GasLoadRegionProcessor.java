package gas.annotations.misc;

import arc.util.Log;
import gas.annotations.GasAnnotations;
import gas.annotations.GasBaseProcessor;
import arc.Core;
import arc.graphics.g2d.TextureRegion;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import mindustry.annotations.util.Stype;
import mindustry.annotations.util.Svar;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Modifier;

@SupportedAnnotationTypes("gas.annotations.GasAnnotations.Load")
public class GasLoadRegionProcessor extends GasBaseProcessor {
    @Override
    public void process(RoundEnvironment env) throws Exception{
        Log.info(getClass().getSimpleName() + ".work(" + round + ")");
        TypeSpec.Builder regionClass = TypeSpec.classBuilder("GasContentRegions")
                .addModifiers(Modifier.PUBLIC);
        MethodSpec.Builder method = MethodSpec.methodBuilder("loadRegions")
                .addParameter(tname("mindustry.ctype.MappableContent"), "content")
                .addModifiers(Modifier.STATIC, Modifier.PUBLIC);

        ObjectMap<Stype, Seq<Svar>> fieldMap = new ObjectMap<>();

        for(Svar field : fields(GasAnnotations.Load.class)){
            if(!field.is(Modifier.PUBLIC)){
                err("@LoadRegion field must be public", field);
            }
            if (!field.is(Modifier.STATIC) && !field.is(Modifier.FINAL)){
                fieldMap.get(field.enclosingType(), Seq::new).add(field);
            }
        }

        for(ObjectMap.Entry<Stype, Seq<Svar>> entry : fieldMap){
            method.beginControlFlow("if(content instanceof $T)", entry.key.tname());

            for(Svar field : entry.value){
                GasAnnotations.Load an = field.annotation(GasAnnotations.Load.class);
                //get # of array dimensions
                int dims = count(field.mirror().toString(), "[]");
                boolean doFallback = !an.fallback().equals("error");
                String fallbackString = doFallback ? ", " + parse(an.fallback()) : "";

                //not an array
                if(dims == 0){
                    method.addStatement("(($T)content).$L = $T.atlas.find($L$L)", entry.key.tname(), field.name(), Core.class, parse(an.value()), fallbackString);
                }else{
                    //is an array, create length string
                    int[] lengths = an.lengths();
                    if(lengths.length == 0) lengths = new int[]{an.length()};

                    if(dims != lengths.length){
                        err("Length dimensions must match array dimensions: " + dims + " != " + lengths.length, field);
                    }

                    StringBuilder lengthString = new StringBuilder();
                    for(int value : lengths) lengthString.append("[").append(value).append("]");

                    method.addStatement("(($T)content).$L = new $T$L", entry.key.tname(), field.name(), TextureRegion.class, lengthString.toString());

                    for(int i = 0; i < dims; i++){
                        method.beginControlFlow("for(int INDEX$L = 0; INDEX$L < $L; INDEX$L ++)", i, i, lengths[i], i);
                    }

                    StringBuilder indexString = new StringBuilder();
                    for(int i = 0; i < dims; i++){
                        indexString.append("[INDEX").append(i).append("]");
                    }

                    method.addStatement("(($T)content).$L$L = $T.atlas.find($L$L)", entry.key.tname(), field.name(), indexString.toString(), Core.class, parse(an.value()), fallbackString);

                    for(int i = 0; i < dims; i++){
                        method.endControlFlow();
                    }
                }
            }

            method.endControlFlow();
        }

        regionClass.addMethod(method.build());

        write(regionClass);
    }

    private static int count(String str, String substring){
        int lastIndex = 0;
        int count = 0;

        while(lastIndex != -1){

            lastIndex = str.indexOf(substring, lastIndex);

            if(lastIndex != -1){
                count ++;
                lastIndex += substring.length();
            }
        }
        return count;
    }

    private String parse(String value){
        value = '"' + value + '"';
        value = value.replace("@size", "\" + ((gas.world.GasBlock)content).size + \"");
        value = value.replace("@", "\" + content.name + \"");
        value = value.replace("#1", "\" + INDEX0 + \"");
        value = value.replace("#2", "\" + INDEX1 + \"");
        value = value.replace("#", "\" + INDEX0 + \"");
        return value;
    }
}
