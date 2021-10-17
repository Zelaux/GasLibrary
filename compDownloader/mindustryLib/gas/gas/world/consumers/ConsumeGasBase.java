package gas.world.consumers;

import gas.gen.GasBuilding;
import mindustry.world.consumers.ConsumeType;

public abstract class ConsumeGasBase extends GasConsume {
    public final float amount;

    public ConsumeGasBase(float amount) {
        this.amount = amount;
    }

    public ConsumeType type() {
        return null;
    }

    protected float use(GasBuilding entity) {
        return Math.min(this.amount * entity.edelta(), entity.block.gasCapacity);
    }
}
