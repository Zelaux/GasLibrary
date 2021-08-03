package gas.world.blocks.power;

import gas.annotations.GasAnnotations;
import gas.world.GasBlock;
@GasAnnotations.GasAddition(analogue = "auto")
public class GasPowerDistributor extends GasBlock {
    public GasPowerDistributor(String name) {
        super(name);
        this.consumesPower = false;
        this.outputsPower = true;
    }
}
