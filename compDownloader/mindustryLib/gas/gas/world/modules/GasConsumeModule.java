package gas.world.modules;

import gas.gen.GasBuilding;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.world.consumers.Consume;
import mindustry.world.meta.BlockStatus;
import mindustry.world.modules.ConsumeModule;

public class GasConsumeModule extends ConsumeModule {
    private boolean valid;
    private boolean optionalValid;
    private final GasBuilding entity;
    public GasConsumeModule(GasBuilding entity) {
        super(entity);
        this.entity=entity;
    }
    public BlockStatus status() {
        if (!this.entity.shouldConsume()) {
            return BlockStatus.noOutput;
        } else {
            return this.valid && this.entity.productionValid() ? BlockStatus.active : BlockStatus.noInput;
        }
    }

    public void update() {
        if (this.entity.cheating()) {
            this.valid = this.optionalValid = true;
        } else {
            boolean prevValid = this.valid();
            this.valid = true;
            this.optionalValid = true;
            boolean docons = this.entity.shouldConsume() && this.entity.productionValid();
            for (Consume cons:this.entity.block.consumes.all()){
                if (!cons.isOptional()) {
                    if (docons && cons.isUpdate() && prevValid && cons.valid(this.entity)) {
                        cons.update(this.entity);
                    }

                    this.valid &= cons.valid(this.entity);
                }
            }

            for (Consume cons:entity.block.consumes.optionals()){
                if (docons && cons.isUpdate() && prevValid && cons.valid(entity)) {
                    cons.update(entity);
                }

                this.optionalValid &= cons.valid(entity);
            }

        }
    }

    public void trigger() {
        for (Consume cons : entity.block.consumes.all()) {
            cons.trigger(entity);
        }
    }


    public boolean valid() {
        return valid && entity.shouldConsume() && entity.enabled;
    }

    public boolean optionalValid() {
        return valid() && optionalValid && entity.enabled;
    }

    @Override
    public void read(Reads read, boolean legacy) {
        read(read);
    }

    public void write(Writes write) {
        write.bool(this.valid);
    }

    public void read(Reads read) {
        this.valid = read.bool();
    }
}
