package gas.world.consumers;

import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import mindustry.gen.Building;
import mindustry.world.consumers.Consume;
import mindustry.world.consumers.ConsumePower;
import mindustry.world.consumers.ConsumeType;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.Stats;

public class ConsumePowerDynamic extends ConsumePower {
    public final float usage;
    public final float capacity;
    public final boolean buffered;

    public ConsumePowerDynamic(float usage, float capacity, boolean buffered) {
        this.usage = usage;
        this.capacity = capacity;
        this.buffered = buffered;
    }

    protected ConsumePowerDynamic() {
        this(0.0F, 0.0F, false);
    }

    public ConsumeType type() {
        return ConsumeType.power;
    }

    public void build(Building tile, Table table) {
    }

    public String getIcon() {
        return "icon-power";
    }

    public void update(Building entity) {
    }

    @Override
    public void trigger(Building entity) {
        super.trigger(entity);
    }

    public boolean valid(Building entity) {
        if (this.buffered) {
            return true;
        } else {
            return entity.power.status > usage;
        }
    }

    public void display(Stats stats) {
        if (this.buffered) {
            stats.add(Stat.powerCapacity, this.capacity, StatUnit.none);
        } else {
            stats.add(Stat.powerUse, this.usage * 60.0F, StatUnit.powerSecond);
        }

    }

    public float requestedPower(Building entity) {
        if (entity.tile().build == null || !valid(entity)) {
            return 0.0F;
        } else if (this.buffered) {
            return (1.0F - entity.power.status) * this.capacity;
        } else {
            try {
                return this.usage * (float) Mathf.num(entity.shouldConsume());
            } catch (Exception var3) {
                return 0.0F;
            }
        }
    }
}
