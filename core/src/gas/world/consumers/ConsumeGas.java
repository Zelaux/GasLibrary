package gas.world.consumers;

import arc.scene.ui.layout.*;
import gas.annotations.*;
import gas.gen.*;
import gas.type.*;
import gas.world.*;
import gas.world.meta.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.meta.*;

import static mindustry.Vars.iconMed;

@GasAnnotations.GasAddition(analogue = "mindustry.world.consumers.ConsumeLiquid")
public class ConsumeGas extends ConsumeGasBase{
    public final Gas gas;

    public ConsumeGas(Gas gas, float amount){
        super(amount);
        this.gas = gas;
    }

    protected ConsumeGas(){
        this(null, 0f);
    }

    @Override
    public void apply(Block b){
        super.apply(b);
        GasBlock block = expectGasBlock(b);
        block.gasFilter[gas.id] = true;
    }

    @Override
    public void build(Building b, Table table){
        GasBuilding build = b.as();
        table.add(new ReqImage(gas.uiIcon, () -> build.gasses.get(gas) > 0)).size(iconMed).top().left();
    }

    @Override
    public void update(Building b){
        GasBuilding build = b.as();
        build.gasses.remove(gas, amount * build.edelta());
    }

    @Override
    public float efficiency(Building b){
        GasBuilding build = b.as();
        //there can be more gas than necessary, so cap at 1
        return Math.min(build.gasses.get(gas) / (amount * build.edelta()), 1f);
    }

    @Override
    public void display(Stats stats){
        stats.add(booster ? Stat.booster : Stat.input, GasStatValues.gas(gas, amount * 60f, true));
    }
}
