package gas.world.blocks.production;

import mindustry.entities.units.*;
import gas.entities.comp.*;
import mindustry.entities.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import mindustry.content.*;
import gas.content.*;
import mindustry.world.blocks.defense.turrets.*;
import gas.world.blocks.payloads.*;
import gas.world.meta.*;
import gas.world.blocks.units.*;
import mindustry.world.blocks.heat.*;
import mindustry.world.blocks.production.GenericCrafter.*;
import arc.util.*;
import mindustry.world.blocks.legacy.*;
import mindustry.gen.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import arc.math.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.meta.*;
import gas.world.blocks.heat.*;
import gas.world.blocks.defense.*;
import mindustry.world.blocks.distribution.*;
import gas.world.blocks.power.*;
import mindustry.world.*;
import gas.world.blocks.sandbox.*;
import mindustry.world.blocks.storage.*;
import gas.world.blocks.liquid.*;
import gas.entities.*;
import mindustry.world.blocks.campaign.*;
import gas.world.blocks.defense.turrets.*;
import gas.world.blocks.distribution.*;
import gas.world.*;
import mindustry.world.consumers.*;
import gas.world.blocks.gas.*;
import arc.math.geom.*;
import gas.world.blocks.campaign.*;
import mindustry.world.modules.*;
import gas.ui.*;
import mindustry.world.blocks.environment.*;
import gas.world.consumers.*;
import mindustry.world.blocks.payloads.*;
import mindustry.world.blocks.*;
import gas.world.blocks.production.GasGenericCrafter.*;
import arc.graphics.g2d.*;
import mindustry.world.blocks.logic.*;
import gas.world.modules.*;
import gas.world.blocks.*;
import gas.*;
import gas.io.*;
import gas.world.draw.*;
import gas.gen.*;
import gas.world.blocks.storage.*;
import mindustry.world.blocks.units.*;
import gas.world.blocks.production.*;
import arc.struct.*;
import arc.util.io.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import mindustry.logic.*;
import mindustry.world.blocks.power.*;
import mindustry.type.*;
import mindustry.world.blocks.sandbox.*;
import static mindustry.Vars.*;

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

    /**
     * Written to outputLiquids as a single-element array if outputLiquids is null.
     */
    @Nullable
    public LiquidStack outputLiquid;

    /**
     * Overwrites outputLiquid if not null.
     */
    @Nullable
    public LiquidStack[] outputLiquids;

    /**
     * Liquid output directions, specified in the same order as outputLiquids. Use -1 to dump in every direction. Rotations are relative to block.
     */
    public int[] liquidOutputDirections = { -1 };

    /**
     * if true, crafters with multiple liquid outputs will dump excess when there's still space for at least one liquid type
     */
    public boolean dumpExtraLiquid = true;

    public boolean ignoreLiquidFullness = false;

    // TODO should be seconds?
    public float craftTime = 80;

    public Effect craftEffect = Fx.none;

    public Effect updateEffect = Fx.none;

    public float updateEffectChance = 0.04f;

    public float warmupSpeed = 0.019f;

    /**
     * Only used for legacy cultivator blocks.
     */
    public boolean legacyReadWarmup = false;

    public GasDrawBlock drawer = new GasDrawDefault();

    public GasGenericCrafter(String name) {
        super(name);
        update = true;
        solid = true;
        hasItems = true;
        ambientSound = Sounds.machine;
        sync = true;
        ambientSoundVolume = 0.03f;
        flags = EnumSet.of(BlockFlag.factory);
        drawArrow = false;
    }

    @Override
    public void setStats() {
        stats.timePeriod = craftTime;
        super.setStats();
        stats.add(Stat.productionTime, craftTime / 60f, StatUnit.seconds);
        if (outputItems != null) {
            stats.add(Stat.output, StatValues.items(craftTime, outputItems));
        }
        if (outputLiquids != null) {
            stats.add(Stat.output, StatValues.liquids(1f, outputLiquids));
        }
    }

    @Override
    public void setBars() {
        super.setBars();
        // set up liquid bars for liquid outputs
        if (outputLiquids != null && outputLiquids.length > 0) {
            // no need for dynamic liquid bar
            removeBar("liquid");
            // then display output buffer
            for (var stack : outputLiquids) {
                addLiquidBar(stack.liquid);
            }
        }
    }

    @Override
    public boolean rotatedOutput(int x, int y) {
        return false;
    }

    @Override
    public void load() {
        super.load();
        drawer.load(this);
    }

    @Override
    public void init() {
        if (outputItems == null && outputItem != null) {
            outputItems = new ItemStack[] { outputItem };
        }
        if (outputLiquids == null && outputLiquid != null) {
            outputLiquids = new LiquidStack[] { outputLiquid };
        }
        // write back to outputLiquid, as it helps with sensing
        if (outputLiquid == null && outputLiquids != null && outputLiquids.length > 0) {
            outputLiquid = outputLiquids[0];
        }
        outputsLiquid = outputLiquids != null;
        if (outputItems != null)
            hasItems = true;
        if (outputLiquids != null)
            hasLiquids = true;
        super.init();
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
        drawer.drawPlan(this, plan, list);
    }

    @Override
    public TextureRegion[] icons() {
        return drawer.finalIcons(this);
    }

    @Override
    public boolean outputsItems() {
        return outputItems != null;
    }

    @Override
    public void getRegionsToOutline(Seq<TextureRegion> out) {
        drawer.getRegionsToOutline(this, out);
    }

    @Override
    public void drawOverlay(float x, float y, int rotation) {
        if (outputLiquids != null) {
            for (int i = 0; i < outputLiquids.length; i++) {
                int dir = liquidOutputDirections.length > i ? liquidOutputDirections[i] : -1;
                if (dir != -1) {
                    Draw.rect(outputLiquids[i].liquid.fullIcon, x + Geometry.d4x(dir + rotation) * (size * tilesize / 2f + 4), y + Geometry.d4y(dir + rotation) * (size * tilesize / 2f + 4), 8f, 8f);
                }
            }
        }
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
            if (outputLiquids != null && !ignoreLiquidFullness) {
                boolean allFull = true;
                for (var output : outputLiquids) {
                    if (liquids.get(output.liquid) >= liquidCapacity - 0.001f) {
                        if (!dumpExtraLiquid) {
                            return false;
                        }
                    } else {
                        // if there's still space left, it's not full for all liquids
                        allFull = false;
                    }
                }
                // if there is no space left for any liquid, it can't reproduce
                if (allFull) {
                    return false;
                }
            }
            return enabled;
        }

        @Override
        public void updateTile() {
            if (efficiency > 0) {
                progress += getProgressIncrease(craftTime);
                warmup = Mathf.approachDelta(warmup, warmupTarget(), warmupSpeed);
                // continuously output based on efficiency
                if (outputLiquids != null) {
                    float inc = getProgressIncrease(1f);
                    for (var output : outputLiquids) {
                        handleLiquid(this, output.liquid, Math.min(output.amount * inc, liquidCapacity - liquids.get(output.liquid)));
                    }
                }
                if (wasVisible && Mathf.chanceDelta(updateEffectChance)) {
                    updateEffect.at(x + Mathf.range(size * 4f), y + Mathf.range(size * 4));
                }
            } else {
                warmup = Mathf.approachDelta(warmup, 0f, warmupSpeed);
            }
            // TODO may look bad, revert to edelta() if so
            totalProgress += warmup * Time.delta;
            if (progress >= 1f) {
                craft();
            }
            dumpOutputs();
        }

        public float warmupTarget() {
            return 1f;
        }

        @Override
        public float warmup() {
            return warmup;
        }

        @Override
        public float totalProgress() {
            return totalProgress;
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
            if (wasVisible) {
                craftEffect.at(x, y);
            }
            progress %= 1f;
        }

        public void dumpOutputs() {
            if (outputItems != null && timer(timerDump, dumpTime / timeScale)) {
                for (var output : outputItems) {
                    dump(output.item);
                }
            }
            if (outputLiquids != null) {
                for (int i = 0; i < outputLiquids.length; i++) {
                    int dir = liquidOutputDirections.length > i ? liquidOutputDirections[i] : -1;
                    dumpLiquid(outputLiquids[i].liquid, 2f, dir);
                }
            }
        }

        @Override
        public double sense(LAccess sensor) {
            if (sensor == LAccess.progress)
                return progress();
            // attempt to prevent wild total liquid fluctuation, at least for crafters
            if (sensor == LAccess.totalLiquids && outputLiquid != null)
                return liquids.get(outputLiquid.liquid);
            return super.sense(sensor);
        }

        @Override
        public float progress() {
            return Mathf.clamp(progress);
        }

        @Override
        public int getMaximumAccepted(Item item) {
            return itemCapacity;
        }

        @Override
        public boolean shouldAmbientSound() {
            return efficiency > 0;
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
