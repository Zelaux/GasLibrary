package gas.annotations.GasTypeIO;

import arc.func.Prov;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.io.Writes;
import gas.annotations.GasAnnotations;
import gas.annotations.GasBaseProcessor;
import com.squareup.javapoet.*;
import mindustry.annotations.util.Selement;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Modifier;

@SupportedAnnotationTypes({
        "gas.annotations.GasAnnotations.WritableObject",
        "gas.annotations.GasAnnotations.WritableObjects"
})
public class ObjectReadProcessor extends GasBaseProcessor {
    int lastId = 0;
    private ClassName writableInterface;

    {
        rounds = 1;
    }

    @Override
    public void process(RoundEnvironment env) throws Exception {
        Seq<Selement> elements = elements(GasAnnotations.WritableObject.class);
        Selement configurationMethod = elements(GasAnnotations.WritableObjectsConfig.class).first();
        TypeSpec.Builder idBuilder = TypeSpec.classBuilder("ObjectOperations").addModifiers(Modifier.PUBLIC)
                .addField(FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(ObjectMap.class),
                        tname(Integer.class), tname(Prov.class)),
                        "idMap", Modifier.PUBLIC, Modifier.STATIC).initializer("new ObjectMap<>()").build())
                .addField(FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(ObjectMap.class),
                        tname(Class.class), tname(Integer.class)),
                        "nameMap", Modifier.PUBLIC, Modifier.STATIC).initializer("new ObjectMap<>()").build())
                .addField(FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(ObjectMap.class),
                        tname(Class.class), tname(Prov.class)),
                        "classMap", Modifier.PUBLIC, Modifier.STATIC).initializer("new ObjectMap<>()").build())
                .addMethod(MethodSpec.methodBuilder("map").addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(TypeName.get(Prov.class)).addParameter(int.class, "id").addStatement("return idMap.get(id)").build())
                .addMethod(MethodSpec.methodBuilder("map").addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(TypeName.get(Prov.class)).addParameter(Class.class, "c").addStatement("return classMap.get(c)").build());
        writableInterface = ClassName.get(packageName, "WritableInterface");
        idBuilder.addMethod(mapClassMethod());
        idBuilder.addMethod(getByIdMethod());
        idBuilder.addMethod(containsMethod());
        CodeBlock.Builder idStore = CodeBlock.builder();
//        Log.info("elements.size=@", elements.size);
        GasAnnotations.WritableObjectsConfig configurationAnnotation = (GasAnnotations.WritableObjectsConfig) configurationMethod.annotation(GasAnnotations.WritableObjectsConfig.class);
        int offset = configurationAnnotation.offset();
        elements.each(element -> {
            int id = -(elements.indexOf(element) + offset+1);
            idStore.addStatement("mapClass($L,$L.class,$L::new)",id,element.asType().cname(),element.asType().cname());
//            idStore.addStatement("idMap.put($L, $L::new)", id, element.asType().cname());
//            idStore.addStatement("nameMap.put($L, $L)",  element.asType().cname() + ".class",id);
//            idStore.addStatement("classMap.put($L, $L::new)", element.asType().cname() + ".class", element.asType().cname());
        });


        idBuilder.addStaticBlock(idStore.build());

        Seq<ClassName> imports = Seq.with();
        imports.add(ClassName.get(Log.class));
        write(idBuilder, imports,0);
    }
    private MethodSpec mapClassMethod(){
        MethodSpec.Builder method = MethodSpec.methodBuilder("mapClass").addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class).addParameter(int.class, "id").addParameter(Class.class,"aClass").addParameter(Prov.class,"prov");

        method.addStatement("idMap.put(id, prov)");
        method.addStatement("nameMap.put(aClass, id)");
        method.addStatement("classMap.put(aClass, prov)");
        return method.build();
    }
    private MethodSpec getByIdMethod() {
        MethodSpec.Builder method = MethodSpec.methodBuilder("getById").addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(writableInterface).addParameter(int.class, "id");
        method.beginControlFlow("try");
        method.addStatement("Prov p=map(id)");
        method.addStatement("return ($L)p.get()", writableInterface);
        method.endControlFlow("catch (Exception e){return null");
        method.addCode("}");
        return method.build();
    }

    private MethodSpec containsMethod() {
        MethodSpec.Builder method = MethodSpec.methodBuilder("contains").addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.get(boolean.class)).addParameter(Writes.class, "write").addParameter(Object.class, "obj");
        method.beginControlFlow("try");
        method.addStatement("Class aClass=obj.getClass()");
        method.addStatement("int id=nameMap.get(aClass,Integer.MIN_VALUE)");
//        method.addStatement("Log.info(\"class(@).id: @\",aClass.getName(),nameMap.get(aClass))");
        method.addStatement("$L w=($L)obj", writableInterface, writableInterface);
        method.addStatement("write.b(id)");
        method.addStatement("w.write(write)");
        method.addStatement("return true");
        method.endControlFlow();
        method.addCode("catch (IllegalArgumentException e){throw e;}");
//        cont.addCode("");
        method.beginControlFlow(/*"catch (NullPointerException e){\n}"+*/" catch (Exception e)");
//        method.addStatement("Log.err(e)");
        method.endControlFlow();
        method.addStatement("return false");
        return method.build();
    }
}
