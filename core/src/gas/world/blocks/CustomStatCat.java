package gas.world.blocks;

import arc.Core;
import arc.struct.Seq;
import gas.annotations.GasAnnotations;
import mindustry.world.meta.StatCat;

@GasAnnotations.CustomStatCat
public enum CustomStatCat {
    gasses;
    public String localized(){
        return Core.bundle.get("category." + name());
    }
    public static CustomStatCat fromExist(StatCat stat){
        return Seq.with(values()).find((v)-> v.name().equals(stat.name()));
    }
}
