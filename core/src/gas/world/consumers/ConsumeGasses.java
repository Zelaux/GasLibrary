package gas.world.consumers;

import arc.scene.ui.layout.*;
import gas.*;
import gas.gen.*;
import gas.world.*;
import gas.world.meta.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;

public class ConsumeGasses extends GasConsume{
    public final GasStack[] gasStacks;

    public ConsumeGasses(GasStack[] liquids){
        this.gasStacks = liquids;
    }

    /** Mods. */
    protected ConsumeGasses(){
        this(GasStack.empty);
    }


    @Override
    public void apply(Block b){
        GasBlock block = expectGasBlock(b);
        block.hasGasses = true;
        for(var stack : gasStacks){
            block.gasFilter[stack.gas.id] = true;
        }
    }

    @Override
    public void build(Building b, Table table){
        GasBuilding build = b.as();
        table.table(c -> {
            int i = 0;
            for(var stack : gasStacks){
                c.add(new ReqImage(stack.gas.uiIcon,
                () -> build.gasses.get(stack.gas) > 0)).size(Vars.iconMed).padRight(8);
                if(++i % 4 == 0) c.row();
            }
        }).left();
    }

    @Override
    public void update(Building b){
        GasBuilding build = b.as();
        for(var stack : gasStacks){
            build.gasses.remove(stack.gas, stack.amount * build.edelta());
        }
    }

    @Override
    public float efficiency(Building b){
        GasBuilding build = b.as();
        float min = 1f, delta = build.edelta();
        for(var stack : gasStacks){
            min = Math.min(build.gasses.get(stack.gas) / (stack.amount * delta), min);
        }
        return min;
    }

    @Override
    public void display(Stats stats){
        stats.add(booster ? Stat.booster : Stat.input, GasStatValues.gasses(1f, true, gasStacks));
    }

}
