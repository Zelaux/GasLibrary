package gas.annotations.classMap;

import com.squareup.javapoet.*;
import com.squareup.javapoet.TypeSpec.*;
import mindustry.annotations.util.*;
import mindustry.mod.*;
import mma.annotations.*;

import javax.annotation.processing.*;
import javax.lang.model.element.*;

@SupportedAnnotationTypes("gas.annotations.GasAnnotations.GasClassMapGenerator")
public class ClassMapProc extends ModBaseProcessor{
    @Override
    public void process(RoundEnvironment env) throws Exception{
        Builder classMap = TypeSpec.classBuilder("GasClassMap").addModifiers(Modifier.PUBLIC);
        classMap.addMethod(generateInitMethod().build());
        write(classMap,"gas");
    }

    private MethodSpec.Builder generateInitMethod(){
        MethodSpec.Builder builder = MethodSpec.methodBuilder("init").addModifiers(Modifier.STATIC);
        ClassName classMap = ClassName.get(ClassMap.class);
        for(Element element : env.getRootElements()){
            if(element instanceof TypeElement){
                Stype stype = new Stype((TypeElement)element);
                if(!stype.e.getModifiers().contains(Modifier.PUBLIC) || stype.cname().simpleName().equals("NonExistentClass")){
                    continue;
                }
                builder.addStatement("$T.classes.put($S,$T.class)", classMap, stype.cname().simpleName(), stype.tname());
            }
        }
        return builder;
    }
}
