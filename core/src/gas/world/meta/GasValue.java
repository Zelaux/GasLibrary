package gas.world.meta;

import gas.type.Gas;
import gas.ui.GasDisplay;
import arc.scene.ui.layout.Table;
import mindustry.world.meta.StatValue;

public class GasValue  implements StatValue {

    private final Gas gas;
    private final float amount;
    private final boolean perSecond;

    public GasValue(Gas gas, float amount, boolean perSecond) {
        this.gas = gas;
        this.amount = amount;
        this.perSecond = perSecond;
    }

    @Override
    public void display(Table table) {
        table.add(new GasDisplay(this.gas, this.amount, this.perSecond));
    }
}
