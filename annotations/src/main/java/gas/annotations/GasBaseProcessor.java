package gas.annotations;

import arc.files.Fi;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.OS;
import arc.util.Strings;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import mindustry.annotations.BaseProcessor;
import mindustry.annotations.util.Selement;
import mma.annotations.ModBaseProcessor;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
public abstract class GasBaseProcessor extends ModBaseProcessor {

    public void delete(String name) throws IOException {
//        print("delete name: @",name);
        FileObject resource;
        resource = filer.getResource(StandardLocation.SOURCE_OUTPUT, packageName, name);
//        boolean delete = resource.delete();
//        print("delete: @ ,named: @, filer: @",delete,resource.getName(),resource.getClass().getName());
        Files.delete(Paths.get(resource.getName() + ".java"));
    }
}
