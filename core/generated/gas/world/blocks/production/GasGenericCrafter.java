package gas.world.blocks.production;

import gas.entities.comp.*;
import mindustry.entities.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import mindustry.content.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.experimental.*;
import gas.world.meta.*;
import gas.world.blocks.units.*;
import mindustry.world.blocks.production.GenericCrafter.*;
import gas.world.blocks.defense.*;
import arc.util.*;
import mindustry.world.blocks.legacy.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import arc.math.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.meta.*;
import gas.world.blocks.distribution.*;
import gas.world.draw.*;
import mindustry.world.blocks.logic.*;
import mindustry.gen.*;
import gas.world.blocks.power.*;
import mindustry.world.*;
import gas.world.blocks.sandbox.*;
import mindustry.world.blocks.storage.*;
import gas.world.blocks.liquid.*;
import gas.entities.*;
import mindustry.world.blocks.campaign.*;
import gas.gen.*;
import gas.world.*;
import gas.world.blocks.defense.turrets.*;
import gas.world.blocks.gas.*;
import gas.world.blocks.campaign.*;
import mindustry.world.modules.*;
import gas.ui.*;
import mindustry.world.blocks.environment.*;
import gas.world.consumers.*;
import mindustry.world.blocks.payloads.*;
import mindustry.world.blocks.*;
import gas.world.blocks.production.GasGenericCrafter.*;
import arc.graphics.g2d.*;
import mindustry.world.consumers.*;
import gas.world.modules.*;
import gas.world.blocks.*;
import gas.*;
import gas.io.*;
import gas.world.blocks.payloads.*;
import mindustry.world.blocks.units.*;
import gas.content.*;
import gas.world.blocks.storage.*;
import gas.world.blocks.production.*;
import arc.struct.*;
import arc.util.io.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import gas.world.meta.values.*;
import mindustry.logic.*;
import mindustry.world.blocks.power.*;
import mindustry.type.*;
import mindustry.world.blocks.sandbox.*;

public class GasGenericCrafter extends GasBlock {

    /**
     * Written to outputItems as a single-element array if outputItems is null.
     */
    @Nullable
    public ItemStack outputItem;

    /**
     * Overwrites outputItem if not null.
     */
    @Nullable
    public ItemStack[] outputItems;

    @Nullable
    public LiquidStack outputLiquid;

    public float craftTime = 80;

    public Effect craftEffect = Fx.none;

    public Effect updateEffect = Fx.none;

    public float updateEffectChance = 0.04f;

    public float warmupSpeed = 0.019f;

    /**
     * Only used for legacy cultivator blocks.
     */
    public boolean legacyReadWarmup = false;

    public GasDrawBlock drawer = new GasDrawBlock();

    public GasGenericCrafter(String name) {
        super(name);
        update = true;
        solid = true;
        hasItems = true;
        ambientSound = Sounds.machine;
        sync = true;
        ambientSoundVolume = 0.03f;
        flags = EnumSet.of(BlockFlag.factory);
    }

    @Override
    public void setStats() {
        stats.timePeriod = craftTime;
        super.setStats();
        stats.add(Stat.productionTime, craftTime / 60f, StatUnit.seconds);
        if (outputItems != null) {
            stats.add(Stat.output, StatValues.items(craftTime, outputItems));
        }
        if (outputLiquid != null) {
            stats.add(Stat.output, outputLiquid.liquid, outputLiquid.amount * (60f / craftTime), true);
        }
    }

    @Override
    public void load() {
        super.load();
        drawer.load(this);
    }

    @Override
    public void init() {
        outputsLiquid = outputLiquid != null;
        if (outputItems == null && outputItem != null) {
            outputItems = new ItemStack[] { outputItem };
        }
        super.init();
    }

    @Override
    public TextureRegion[] icons() {
        return drawer.icons(this);
    }

    @Override
    public boolean outputsItems() {
        return outputItems != null;
    }

    public class GasGenericCrafterBuild extends GasBuilding {

        public float progress;

        public float totalProgress;

        public float warmup;

        @Override
        public void draw() {
            drawer.draw(this);
        }

        @Override
        public void drawLight() {
            super.drawLight();
            drawer.drawLight(this);
        }

        @Override
        public boolean shouldConsume() {
            if (outputItems != null) {
                for (var output : outputItems) {
                    if (items.get(output.item) + output.amount > itemCapacity) {
                        return false;
                    }
                }
            }
            return (outputLiquid == null || !(liquids.get(outputLiquid.liquid) >= liquidCapacity - 0.001f)) && enabled;
        }

        @Override
        public void updateTile() {
            if (consValid()) {
                progress += getProgressIncrease(craftTime);
                totalProgress += delta();
                warmup = Mathf.approachDelta(warmup, 1f, warmupSpeed);
                if (Mathf.chanceDelta(updateEffectChance)) {
                    updateEffect.at(x + Mathf.range(size * 4f), y + Mathf.range(size * 4));
                }
            } else {
                warmup = Mathf.approachDelta(warmup, 0f, warmupSpeed);
            }
            if (progress >= 1f) {
                craft();
            }
            dumpOutputs();
        }

        public void craft() {
            consume();
            if (outputItems != null) {
                for (var output : outputItems) {
                    for (int i = 0; i < output.amount; i++) {
                        offload(output.item);
                    }
                }
            }
            if (outputLiquid != null) {
                handleLiquid(this, outputLiquid.liquid, outputLiquid.amount);
            }
            craftEffect.at(x, y);
            progress %= 1f;
        }

        public void dumpOutputs() {
            if (outputItems != null && timer(timerDump, dumpTime / timeScale)) {
                for (var output : outputItems) {
                    dump(output.item);
                }
            }
            if (outputLiquid != null) {
                dumpLiquid(outputLiquid.liquid);
            }
        }

        @Override
        public double sense(LAccess sensor) {
            if (sensor == LAccess.progress)
                return Mathf.clamp(progress);
            return super.sense(sensor);
        }

        @Override
        public int getMaximumAccepted(Item item) {
            return itemCapacity;
        }

        @Override
        public boolean shouldAmbientSound() {
            return cons.valid();
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(progress);
            write.f(warmup);
            if (legacyReadWarmup)
                write.f(0f);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            progress = read.f();
            warmup = read.f();
            if (legacyReadWarmup)
                read.f();
        }
    }
}
