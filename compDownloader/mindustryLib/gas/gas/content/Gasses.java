package gas.content;

import arc.struct.Seq;
import gas.annotations.GasAnnotations;
import gas.type.Gas;
import mindustry.Vars;
import mindustry.ctype.ContentType;

public class Gasses {
    @GasAnnotations.GasAddition(analogue = "mindustry.ctype.ContentType.gas")
    public static ContentType gasType(){
        return ContentType.typeid_UNUSED;
    }
    @GasAnnotations.GasAddition(analogue = "mindustry.ctype.ContentType.gasses()")
    public static Seq<Gas> all(){
        return Vars.content.getBy(gasType());
    }
    @GasAnnotations.GasAddition(analogue = "mindustry.ctype.ContentType.gas(int id)")
    public static Gas getByID(int id){
        return Vars.content.getByID(gasType(),id);
    }
    @GasAnnotations.GasAddition(analogue = "mindustry.ctype.ContentType.gas(String name)")
    public static Gas getByName(String name){
        return Vars.content.getByName(gasType(),name);
    }


}
