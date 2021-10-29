package gas.annotations.analogues;

import arc.files.Fi;
import arc.struct.Seq;
import arc.util.Log;
import gas.annotations.GasAnnotations;
import mindustry.annotations.util.Selement;
import mindustry.annotations.util.Smethod;
import mindustry.annotations.util.Stype;
import mma.annotations.*;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.TypeElement;

@SupportedAnnotationTypes("gas.annotations.GasAnnotations.GasAddition")
public class AnaloguesProc extends ModBaseProcessor{
    @Override
    public void process(RoundEnvironment env) throws Exception {
        Seq<Selement> elements = elements(GasAnnotations.GasAddition.class);
        Log.info("AnaloguesProc");
        Fi file = rootDirectory.child("AllClassesAndMethods.md");
        StringBuilder builder = new StringBuilder();
        builder.append("### Classes\n\n");
        for (Stype type : elements.select(Selement::isType).map(Selement::asType)) {
            GasAnnotations.GasAddition gasAddition = type.annotation(GasAnnotations.GasAddition.class);
            String fullName = type.asType().cname().reflectionName();
            handle(builder, gasAddition, fullName,type);
        }
        builder.append("### Methods\n\n");
        for (Smethod type : elements.select(Selement::isMethod).map(Selement::asMethod)) {
            GasAnnotations.GasAddition gasAddition = type.annotation(GasAnnotations.GasAddition.class);
            String fullName = new Stype((TypeElement) type.asMethod().up()).cname().reflectionName() + "." + type.fullName();
            handle(builder, gasAddition, fullName,type);

        }
        file.writeString(builder.toString());
    }

    private void handle(StringBuilder builder, GasAnnotations.GasAddition gasAddition, String fullName, Selement type) {
        builder.append("- ```").append(fullName).append("```");
        if (!gasAddition.analogue().equals("\n")) {
            builder.append(" analogue of ");
            String analogue = gasAddition.analogue();
            if (analogue.equals("auto")){
                if (type.isMethod()) {
                    Stype stype = new Stype((TypeElement) type.asMethod().up());
                    String reflectionName = stype.cname().reflectionName();
                    reflectionName=reflectionName.substring(reflectionName.indexOf(".")+1,reflectionName.lastIndexOf("."));
                    String className=stype.name().replace("Gas","");
                    reflectionName="mindustry."+reflectionName+"."+className;
                    analogue=reflectionName+"."+type.fullName();
                } else if (type.isType()){
                    String reflectionName = type.cname().reflectionName();
                    reflectionName=reflectionName.substring(reflectionName.indexOf(".")+1,reflectionName.lastIndexOf("."));
                    String className=type.name().replace("Gas","");
                    analogue="mindustry."+reflectionName+"."+className;
                }
            }
            builder.append("```").append(analogue).append("```\n\n");
        } else if (!gasAddition.description().equals("\n")) {
            builder.append("\n    >").append(gasAddition.analogue()).append("\n\n");
        } else {
            builder.append("\n\n");
        }
    }
}
