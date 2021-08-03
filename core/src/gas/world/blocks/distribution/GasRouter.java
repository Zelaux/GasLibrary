package gas.world.blocks.distribution;

import gas.annotations.GasAnnotations;
import gas.type.Gas;
import gas.world.blocks.gas.GasGasBlock;
import mindustry.gen.Building;

@GasAnnotations.GasAddition()
public class GasRouter extends GasGasBlock {
    public GasRouter(String name){
        super(name);
        noUpdateDisabled = true;
    }

    public class GasRouterBuild extends GasBuild {

        @Override
        public void updateTile(){
            if(gasses.total() > 0.01f){
                dumpGas(gasses.current());
            }
        }

        @Override
        public boolean acceptGas(Building source, Gas gas){
            return (gasses.current() == gas || gasses.currentAmount() < 0.2f);
        }
    }
}
