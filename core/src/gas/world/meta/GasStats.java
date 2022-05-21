package gas.world.meta;

import acontent.world.meta.AStat;
import acontent.world.meta.AStatCat;
import mindustry.world.meta.StatCat;

public class GasStats {
    public static AStat gasCapacity = AStat.get("gasCapacity", AStatCat.get("gasses", StatCat.liquids.id+1));
}
