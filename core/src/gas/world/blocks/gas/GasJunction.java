package gas.world.blocks.gas;

import arc.graphics.g2d.*;
import gas.gen.*;
import gas.type.*;
import gas.world.meta.*;
import mindustry.gen.*;

public class GasJunction extends GasGasBlock{


    public GasJunction(String name){
        super(name);
        int i;
    }

    @Override
    public void setStats(){
        super.setStats();
        aStats.remove(GasStats.gasCapacity);
//        stats.remove(Stat.liquidCapacity);
    }

    @Override
    public void setBars(){
        super.setBars();
        bars.remove("liquid");
    }

    @Override
    public TextureRegion[] icons(){
        return new TextureRegion[]{region};
    }

    public class GasJunctionBuild extends GasBuilding{

        @Override
        public void draw(){
            Draw.rect(region, x, y);
        }

        @Override
        public GasBuilding getGasDestination(Building source, Gas gas){
            if(!enabled)
                return this;
            int dir = source.relativeTo(tile.x, tile.y);
            dir = (dir + 4) % 4;
            GasBuilding next = nearby(dir) instanceof GasBuilding b ? b : null;
            if(next == null || (!next.acceptGas(this, gas) && !(next.block instanceof GasJunction))){
                return this;
            }
            return next.getGasDestination(this, gas);
        }
    }
}
