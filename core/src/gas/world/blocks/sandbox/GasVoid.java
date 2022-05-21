package gas.world.blocks.sandbox;

import gas.annotations.GasAnnotations;
import gas.gen.GasBuilding;
import gas.type.Gas;
import gas.world.GasBlock;
import mindustry.gen.Building;
import mindustry.type.Liquid;
import mindustry.world.Block;
import mindustry.world.meta.BlockGroup;

public class GasVoid extends GasBlock {
    public GasVoid(String name) {
        super(name);
        hasGasses = true;
        solid = true;
        update = true;
        group = BlockGroup.liquids;
    }
    @Override
    public void setBars(){
        super.setBars();
        removeBar("gas");
    }
    public class GasVoidBuild extends GasBuilding {
        @Override
        public boolean acceptGas(Building source, Gas gas){
            return enabled;
        }

        @Override
        public void handleGas(Building source, Gas gas, float amount){
        }
    }
}
