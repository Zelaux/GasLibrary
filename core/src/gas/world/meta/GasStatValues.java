package gas.world.meta;

import arc.func.*;
import arc.struct.*;
import gas.*;
import gas.content.*;
import gas.type.*;
import gas.ui.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.meta.*;

public class GasStatValues{
    public static StatValue gas(Gas gas, float amount, boolean perSecond){
        return table -> table.add(new GasDisplay(gas, amount, perSecond));
    }

    public static StatValue gasses(Boolf<Gas> filter, float amount, boolean perSecond){
        return table -> {
            Seq<Gas> list = Gasses.all().select(i -> filter.get(i) && i.unlockedNow());

            for(int i = 0; i < list.size; i++){
                table.add(new GasDisplay(list.get(i), amount, perSecond)).padRight(5);

                if(i != list.size - 1){
                    table.add("/");
                }
            }
        };
    }

    public static StatValue gasses(float timePeriod, GasStack... stacks){
        return gasses(timePeriod, true, stacks);
    }

    public static StatValue gasses(float timePeriod, boolean perSecond, GasStack... stacks){
        return table -> {
            for(var stack : stacks){
                table.add(new GasDisplay(stack.gas, stack.amount * (60f / timePeriod), perSecond)).padRight(5);
            }
        };
    }

}
