package gas.world.consumers;

import arc.func.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import gas.annotations.*;
import gas.content.*;
import gas.gen.*;
import gas.type.*;
import gas.world.*;
import gas.world.meta.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.meta.*;

import static mindustry.Vars.content;

@GasAnnotations.GasAddition(analogue = "mindustry.world.consumers.ConsumeLiquidFilter")
public class ConsumeGasFilter extends ConsumeGasBase{
    public Boolf<Gas> filter = l -> false;

    public ConsumeGasFilter(Boolf<Gas> gas, float amount){
        super(amount);
        this.filter = gas;
    }

    public ConsumeGasFilter(){
    }

    @Override
    public void apply(Block b){
        GasBlock block = expectGasBlock(b);
        block.hasGasses = true;
        Gasses.all().each(filter, item -> block.gasFilter[item.id] = true);
    }

    @Override
    public void build(Building b, Table table){
        GasBuilding build = b.as();
        Seq<Gas> list = Gasses.all().select(l -> !l.isHidden() && filter.get(l));
        MultiReqImage image = new MultiReqImage();
        list.each(gas -> image.add(new ReqImage(gas.uiIcon, () ->
        build.gasses != null && build.gasses.get(gas) > 0)));
        table.add(image).size(8 * 4);
    }

    @Override
    public void update(Building b){
        GasBuilding build = b.as();
        Gas gas = getConsumed(build);
        if(gas != null){
            build.gasses.remove(gas, amount * build.edelta());
        }
    }

    @Override
    public float efficiency(Building b){
        GasBuilding build = b.as();
        var gas = getConsumed(build);
        return gas != null ? Math.min(build.gasses.get(gas) / (amount * build.edelta()), 1f) : 0f;
    }

    public @Nullable Gas getConsumed(Building b){
        GasBuilding build = b.as();
        if(filter.get(build.gasses.current()) && build.gasses.currentAmount() > 0){
            return build.gasses.current();
        }

        var gasses = Gasses.all();

        for(int i = 0; i < gasses.size; i++){
            var gas = gasses.get(i);
            if(filter.get(gas) && build.gasses.get(gas) > 0){
                return gas;
            }
        }
        return null;
    }

    @Override
    public void display(Stats stats){
        stats.add(booster ? Stat.booster : Stat.input, GasStatValues.gasses(filter, amount * 60f, true));
    }
}
