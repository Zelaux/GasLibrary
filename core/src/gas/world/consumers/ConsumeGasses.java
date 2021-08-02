package gas.world.consumers;

import acontent.world.meta.AStats;
import gas.gen.GasBuilding;
import gas.type.Gas;
import arc.scene.ui.layout.Table;
import arc.struct.Bits;
import gas.world.meta.GasValue;
import mindustry.gen.Building;
//import mindustry.ui.Cicon;
import mindustry.ui.ReqImage;
import mindustry.world.consumers.ConsumeType;
import mindustry.world.meta.Stat;
import mindustry.world.meta.Stats;

public class ConsumeGasses extends GasConsume {
    public final Gas gas;
    public final float amount;

    public ConsumeType type() {
        return ConsumeType.liquid;
    }

    protected float use(Building entity) {
        return Math.min(this.amount * entity.edelta(), entity.block.liquidCapacity);
    }
    public ConsumeGasses(Gas gas, float amount) {
        this.amount = amount;
        this.gas = gas;
    }

    protected ConsumeGasses() {
        this((Gas) null, 0.0F);
    }

    @Override
    public void applyGasFilter(Bits filter) {
        filter.set(this.gas.id,true);
    }

    public void build(Building tile, Table table) {
        table.add(new ReqImage(this.gas.uiIcon, () -> {
            return this.valid(tile);
        })).size(32.0F);
    }

    public String getIcon() {
        return "icon-liquid-consume";
    }

    public void update(Building b) {
        if (b==null || !(b instanceof GasBuilding))return;
        GasBuilding entity=(GasBuilding)b;
        entity.gasses.remove(this.gas, Math.min(this.use(entity), entity.gasses.get(this.gas)));
    }

    public boolean valid(Building b) {
        if (b==null || !(b instanceof GasBuilding))return false;
        GasBuilding entity=(GasBuilding)b;
        return entity.gasses != null && entity.gasses.get(this.gas) >= this.use(entity);
    }

    public void display(Stats stat) {
        if (!(stat instanceof AStats))return;
        AStats stats=(AStats)stat;
        stats.add(this.booster ? Stat.booster : Stat.input, new GasValue(this.gas, this.amount * 60.0F, true));
    }
}
