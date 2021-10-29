package gas.world.blocks.gas;

import gas.type.*;
import mindustry.gen.*;

public class GasRouter extends GasGasBlock {

    public GasRouter(String name) {
        super(name);
        noUpdateDisabled = true;
        canOverdrive = false;
    }

    public class GasRouterBuild extends GasBuild {

        @Override
        public void updateTile() {
            if (gasses.total() > 0.01f) {
                dumpGas(gasses.current());
            }
        }

        @Override
        public boolean acceptGas(Building source, Gas gas) {
            return (gasses.current() == gas || gasses.currentAmount() < 0.2f);
        }
    }
}
