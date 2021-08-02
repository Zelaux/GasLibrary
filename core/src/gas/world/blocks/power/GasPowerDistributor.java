package gas.world.blocks.power;

import gas.world.GasBlock;

public class GasPowerDistributor extends GasBlock {
    public GasPowerDistributor(String name) {
        super(name);
        this.consumesPower = false;
        this.outputsPower = true;
    }
}
