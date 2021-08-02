package gas.annotations;

import arc.struct.Seq;
import arc.util.Strings;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import mindustry.annotations.BaseProcessor;
import mindustry.annotations.util.Selement;

import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
public abstract class GasBaseProcessor extends BaseProcessor {
    static final String parentName = "mindustry.gen";
    public static String packageName = "gas.gen";

    public static void print(String obj, Object... args) {
        String message = Strings.format(obj.toString(), args);
        System.out.println(message);
//    messager.printMessage(Diagnostic.Kind.OTHER,message);
    }

    //    public TypeElement typeElement(String name){
//        return elementu.getTypeElement(name);
//    }
    public static void err(String message, Selement elem, int zero) {
        err(message, elem.e);
    }

    public static TypeName tname(String pack, String simple) {
        return ClassName.get(pack, simple);
    }

    public static TypeName tname(String name) {
        if (!name.contains(".")) return ClassName.get(packageName, name);

        String pack = name.substring(0, name.lastIndexOf("."));
        String simple = name.substring(name.lastIndexOf(".") + 1);
        return ClassName.get(pack, simple);
    }

    public static TypeName tname(Class<?> c) {
        return ClassName.get(c).box();
    }

    public static void write(TypeSpec.Builder builder) throws Exception {
        write(builder, (Seq<String>) null);
    }

    public static void write(TypeSpec.Builder builder, String packageName) throws Exception {
        write(builder, packageName, (Seq<String>) null);
    }

    public static void write(TypeSpec.Builder builder, String packageName, Seq<ClassName> imports, int ZERO) throws Exception {
        write(builder, packageName, imports.<String>map(className -> "import " + className.reflectionName() + ";"));
    }

    public static void write(TypeSpec.Builder builder, Seq<ClassName> imports, int ZERO) throws Exception {
        write(builder, packageName, imports, ZERO);
    }

    public static void write(TypeSpec.Builder builder, Seq<String> imports) throws Exception {
        write(builder, packageName, imports);
    }

    public static void write(TypeSpec.Builder builder, String packageName, Seq<String> imports) throws Exception {
//        Log.logger=new Log.DefaultLogHandler();
//        Log.err());
        String message = Strings.format("builder.build().name=@", builder.build().name);

//      if (message.contains("Stealthc"))  new RuntimeException(message).printStackTrace();
//        System.out.println(message);
        JavaFile file = JavaFile.builder(packageName, builder.build()).skipJavaLangImports(true).build();

        if (imports != null) {
            String rawSource = file.toString();
            Seq<String> result = new Seq<>();
            for (String s : rawSource.split("\n", -1)) {
                result.add(s);
                if (s.startsWith("package ")) {
                    result.add("");
                    for (String i : imports) {
                        result.add(i);
                    }
                }
            }

            String out = result.toString("\n");
            JavaFileObject object = filer.createSourceFile(file.packageName + "." + file.typeSpec.name, file.typeSpec.originatingElements.toArray(new Element[0]));
//            processingEnv
            OutputStream stream = object.openOutputStream();
            stream.write(out.getBytes());
            stream.close();
        } else {
            file.writeTo(filer);
        }
    }

    public void delete(String name) throws IOException {
//        print("delete name: @",name);
        FileObject resource;
        resource = filer.getResource(StandardLocation.SOURCE_OUTPUT, packageName, name);
//        boolean delete = resource.delete();
//        print("delete: @ ,named: @, filer: @",delete,resource.getName(),resource.getClass().getName());
        Files.delete(Paths.get(resource.getName() + ".java"));

    }
}
