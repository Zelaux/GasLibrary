package gas.world.consumers;

import gas.gen.GasBuilding;
import gas.type.Gas;
import gas.world.meta.values.GasFilterValue;
import arc.func.Boolf;
import arc.scene.ui.layout.Table;
import arc.struct.Bits;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.ctype.ContentType;
import mindustry.gen.Building;
//import mindustry.ui.Cicon;
import mindustry.ui.MultiReqImage;
import mindustry.ui.ReqImage;
import mindustry.world.meta.Stat;
import mindustry.world.meta.Stats;

public class ConsumeGasFilter extends ConsumeGasBase {
    public final Boolf<Gas> filter;

    public ConsumeGasFilter(Boolf<Gas> gas, float amount) {
        super(amount);
        this.filter = gas;
    }

    public void applyLiquidFilter(Bits arr) {
        Vars.content.getBy(ContentType.typeid_UNUSED).<Gas>each((gas)->{
            return filter.get((Gas) gas);
        }, (gas) -> {
            arr.set(((Gas)gas).id);
        });
    }

    public void build(Building t, Table table) {
        GasBuilding tile=(GasBuilding)t;
        Seq<Gas> list = Vars.content.<Gas>getBy(ContentType.typeid_UNUSED).select((g) -> {
            return !g.isHidden() && this.filter.get(g);
        });
        MultiReqImage image = new MultiReqImage();
        list.each((gas) -> {
            image.add(new ReqImage(gas.uiIcon, () -> {
                return tile.gasses != null && tile.gasses.get(gas) >= this.use(tile);
            }));
        });
        table.add(image).size(32.0F);
    }

    public String getIcon() {
        return "icon-liquid-consume";
    }

    public void update(Building e) {
        GasBuilding entity=(GasBuilding)e;
        entity.gasses.remove(entity.gasses.current(), this.use(entity));
    }

    public boolean valid(Building e) {
        GasBuilding entity=(GasBuilding)e;
        return entity != null && entity.gasses != null && this.filter.get(entity.gasses.current()) && entity.gasses.currentAmount() >= this.use(entity);
    }

    public void display(Stats stats) {
        stats.add(this.booster ? Stat.booster : Stat.input, new GasFilterValue(this.filter, this.amount * 60.0F, true));
    }
}
