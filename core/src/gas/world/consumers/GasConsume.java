package gas.world.consumers;

import arc.struct.Bits;
import gas.world.*;
import mindustry.world.*;
import mindustry.world.consumers.Consume;

public abstract class GasConsume extends Consume {
    public GasBlock expectGasBlock(Block block){
        if(!(block instanceof GasBlock gasBlock)) throw new ClassCastException("This drawer requires the block to be a GasBlock. Use a different drawer.");
        return gasBlock;
    }
}
