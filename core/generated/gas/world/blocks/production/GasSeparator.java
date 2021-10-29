package gas.world.blocks.production;

import gas.entities.comp.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import mindustry.type.*;
import mindustry.world.blocks.experimental.*;
import gas.io.*;
import gas.world.meta.*;
import mindustry.annotations.Annotations.*;
import gas.world.blocks.units.*;
import gas.world.blocks.defense.*;
import arc.util.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import arc.math.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.meta.*;
import gas.world.blocks.distribution.*;
import mindustry.gen.*;
import gas.world.blocks.payloads.*;
import mindustry.world.*;
import gas.world.blocks.production.*;
import mindustry.world.blocks.legacy.*;
import gas.world.blocks.liquid.*;
import mindustry.world.blocks.storage.*;
import gas.entities.*;
import mindustry.world.blocks.campaign.*;
import gas.world.blocks.defense.turrets.*;
import gas.gen.*;
import gas.world.*;
import mindustry.world.consumers.*;
import mindustry.world.blocks.defense.turrets.*;
import gas.world.blocks.gas.*;
import gas.world.blocks.campaign.*;
import mindustry.world.modules.*;
import gas.ui.*;
import mindustry.world.blocks.environment.*;
import gas.world.consumers.*;
import mindustry.world.blocks.payloads.*;
import gas.world.blocks.sandbox.*;
import mindustry.world.blocks.*;
import gas.world.blocks.production.GasGenericCrafter.*;
import arc.*;
import mindustry.world.blocks.logic.*;
import gas.world.modules.*;
import gas.world.blocks.*;
import gas.*;
import arc.graphics.g2d.*;
import gas.world.draw.*;
import mindustry.world.blocks.units.*;
import gas.content.*;
import gas.world.blocks.storage.*;
import mindustry.graphics.*;
import mindustry.world.blocks.production.Separator.*;
import arc.util.io.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import gas.world.meta.values.*;
import mindustry.logic.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;
import gas.world.blocks.power.*;

/**
 * Extracts a random list of items from an input item and an input liquid.
 */
public class GasSeparator extends GasBlock {

    public ItemStack[] results;

    public float craftTime;

    @Load("@-liquid")
    public TextureRegion liquidRegion;

    @Load("@-spinner")
    public TextureRegion spinnerRegion;

    public float spinnerSpeed = 3f;

    public GasSeparator(String name) {
        super(name);
        update = true;
        solid = true;
        hasItems = true;
        hasLiquids = true;
        sync = true;
    }

    @Override
    public void setStats() {
        stats.timePeriod = craftTime;
        super.setStats();
        stats.add(Stat.output, StatValues.items(item -> Structs.contains(results, i -> i.item == item)));
        stats.add(Stat.productionTime, craftTime / 60f, StatUnit.seconds);
    }

    public class GasSeparatorBuild extends GasBuilding {

        public float progress;

        public float totalProgress;

        public float warmup;

        public int seed;

        @Override
        public void created() {
            seed = Mathf.randomSeed(tile.pos(), 0, Integer.MAX_VALUE - 1);
        }

        @Override
        public boolean shouldAmbientSound() {
            return cons.valid();
        }

        @Override
        public boolean shouldConsume() {
            int total = items.total();
            // very inefficient way of allowing separators to ignore input buffer storage
            if (consumes.has(ConsumeType.item) && consumes.get(ConsumeType.item) instanceof ConsumeItems) {
                ConsumeItems c = consumes.get(ConsumeType.item);
                for (var stack : c.items) {
                    total -= items.get(stack.item);
                }
            }
            return total < itemCapacity && enabled;
        }

        @Override
        public void draw() {
            super.draw();
            Drawf.liquid(liquidRegion, x, y, liquids.total() / liquidCapacity, liquids.current().color);
            if (Core.atlas.isFound(spinnerRegion)) {
                Draw.rect(spinnerRegion, x, y, totalProgress * spinnerSpeed);
            }
        }

        @Override
        public void updateTile() {
            totalProgress += warmup * delta();
            if (consValid()) {
                progress += getProgressIncrease(craftTime);
                warmup = Mathf.lerpDelta(warmup, 1f, 0.02f);
            } else {
                warmup = Mathf.lerpDelta(warmup, 0f, 0.02f);
            }
            if (progress >= 1f) {
                progress %= 1f;
                int sum = 0;
                for (var stack : results) sum += stack.amount;
                int i = Mathf.randomSeed(seed++, 0, sum);
                int count = 0;
                Item item = null;
                // guaranteed desync since items are random - won't be fixed and probably isn't too important
                for (var stack : results) {
                    if (i >= count && i < count + stack.amount) {
                        item = stack.item;
                        break;
                    }
                    count += stack.amount;
                }
                consume();
                if (item != null && items.get(item) < itemCapacity) {
                    offload(item);
                }
            }
            if (timer(timerDump, dumpTime)) {
                dump();
            }
        }

        @Override
        public double sense(LAccess sensor) {
            if (sensor == LAccess.progress)
                return progress;
            return super.sense(sensor);
        }

        @Override
        public boolean canDump(Building to, Item item) {
            return !consumes.itemFilters.get(item.id);
        }

        @Override
        public byte version() {
            return 1;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(progress);
            write.f(warmup);
            write.i(seed);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            progress = read.f();
            warmup = read.f();
            if (revision == 1)
                seed = read.i();
        }
    }
}
