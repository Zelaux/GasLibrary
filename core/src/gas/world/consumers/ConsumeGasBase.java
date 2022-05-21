package gas.world.consumers;

import mindustry.world.*;

public abstract class ConsumeGasBase extends GasConsume{
    /** amount used per frame */
    public float amount;

    public ConsumeGasBase(float amount){
        this.amount = amount;
    }

    public ConsumeGasBase(){
    }

    @Override
    public void apply(Block b){
        var block = expectGasBlock(b);
        block.hasLiquids = true;

    }
}
