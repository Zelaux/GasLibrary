package gas.content;

import arc.struct.Seq;
import gas.type.Gas;
import arc.graphics.Color;
import mindustry.Vars;
import mindustry.ctype.ContentList;
import mindustry.ctype.ContentType;

public class Gasses {
    public static ContentType gasType(){
        return ContentType.typeid_UNUSED;
    }
    public static Seq<Gas> all(){
        return Vars.content.getBy(gasType());
    }
    public static Gas getByID(int id){
        return Vars.content.getByID(gasType(),id);
    }
    public static Gas getByName(String name){
        return Vars.content.getByName(gasType(),name);
    }


}
