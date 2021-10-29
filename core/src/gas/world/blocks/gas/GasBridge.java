package gas.world.blocks.gas;

import gas.world.blocks.distribution.*;
import mindustry.gen.*;
import mindustry.world.meta.*;

public class GasBridge extends GasItemBridge {

    public GasBridge(String name) {
        super(name);
        hasItems = false;
        hasGasses = true;
        outputsGas = true;
        canOverdrive = false;
        group = BlockGroup.liquids;
        envEnabled = Env.any;
    }

    public class GasBridgeBuild extends GasItemBridgeBuild {

        @Override
        public void updateTransport(Building other) {
            if (warmup >= 0.25f) {
                moved |= moveGas(other, gasses.current()) > 0.05f;
            }
        }

        @Override
        public void doDump() {
            dumpGas(gasses.current(), 1f);
        }
    }
}
