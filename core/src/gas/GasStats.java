package gas;

import gas.type.Gas;
import gas.world.meta.GasValue;
import mindustry.world.meta.Stat;
import mindustry.world.meta.Stats;

public class GasStats extends Stats {

    public void add(Stat stat, Gas gas, float amount, boolean perSecond) {
        this.add(stat, (new GasValue(gas, amount, perSecond)));
    }
}
