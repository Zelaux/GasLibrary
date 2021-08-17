package gas.tools.gasBlockConverter;

import arc.struct.*;
import arc.util.*;

public class NameTransform{
    public final Seq<String> names, fields, annotations;
    private static final String annotationPackage = "gas.annotations.GasAnnotations.";

    public NameTransform(Seq<String> names, Seq<String> fields, Seq<String> annotations){
        this.names = names;
        this.fields = fields;
        this.annotations = annotations;
    }

    public String field(String name){
        if (fields.contains(name))return name.replace("liquid","gas").replace("Liquid","Gas");
        return name;
    }
    public String type(String name){
return names.contains(name)?"Gas"+ Strings.capitalize(name):name;
    }
    public String annotation(String name){
        return annotations.contains(name)?annotationPackage+name:name;
    }
}
