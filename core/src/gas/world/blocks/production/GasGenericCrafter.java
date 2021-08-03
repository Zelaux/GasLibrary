package gas.world.blocks.production;

import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.struct.EnumSet;
import arc.util.Nullable;
import arc.util.io.Reads;
import arc.util.io.Writes;
import gas.GasStack;
import gas.annotations.GasAnnotations;
import gas.gen.GasBuilding;
import gas.world.GasBlock;
import gas.world.draw.GasDrawBlock;
import gas.world.meta.GasValue;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.gen.Building;
import mindustry.gen.Sounds;
import mindustry.logic.LAccess;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.Liquid;
import mindustry.type.LiquidStack;
import mindustry.world.meta.BlockFlag;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.StatValues;

import static mindustry.Vars.net;
import static mindustry.Vars.state;

@GasAnnotations.GasAddition(analogue = "mindustry.world.blocks.production.GenericCrafter")
public class GasGenericCrafter extends GasBlock {
    /**
     * Written to outputItems as a single-element array if outputItems is null.
     */
    public @Nullable
    ItemStack outputItem;
    /**
     * Overwrites outputItem if not null.
     */
    public @Nullable
    ItemStack[] outputItems;
    public @Nullable
    LiquidStack outputLiquid;
    public @Nullable
    GasStack outputGas;

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
        aStats.timePeriod = craftTime;
        super.setStats();
        aStats.add(Stat.productionTime, craftTime / 60f, StatUnit.seconds);

        if (outputItems != null) {
            aStats.add(Stat.output, StatValues.items(craftTime, outputItems));
        }

        if (outputLiquid != null) {
            aStats.add(Stat.output, outputLiquid.liquid, outputLiquid.amount * (60f / craftTime), true);
        }

        if (outputGas != null) {
            aStats.add(Stat.output, new GasValue(outputGas.gas, outputGas.amount * (60f / craftTime), true));
        }
    }

    @Override
    public void load() {
        super.load();

        drawer.load(this);
    }

    @Override
    public void init() {
        if (outputItems == null && outputItem != null) {
            outputItems = new ItemStack[]{outputItem};
        }
        outputsLiquid = outputLiquid != null;
        outputsGas = outputGas != null;
        super.init();
    }

    @Override
    public TextureRegion[] icons() {
        return drawer.icons(this);
    }

    @Override
    public boolean outputsItems() {
        return outputItem != null;
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
                for (ItemStack output : outputItems) {
                    if (items.get(output.item) + output.amount > itemCapacity) {
                        return false;
                    }
                }
            }
            boolean gasBool = outputGas != null && gasses != null && gasses.get(outputGas.gas) >= gasCapacity - 0.001f;
            boolean liquidBool = outputLiquid == null || !(liquids != null && liquids.get(outputLiquid.liquid) >= liquidCapacity - 0.001f);
            return (liquidBool || gasBool) && enabled;
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
                consume();


                if(outputItems != null){
                    for(ItemStack output : outputItems){
                        for(int i = 0; i < output.amount; i++){
                            offload(output.item);
                        }
                    }
                }

                if (outputLiquid != null) {
                    handleLiquid(this, outputLiquid.liquid, outputLiquid.amount);
                }

                if (outputGas != null) {
                    handleGas(this, outputGas.gas, outputGas.amount);
                }

                craftEffect.at(x, y);
                progress %= 1f;
            }

            if(outputItems != null && timer(timerDump, dumpTime / timeScale)){
                for(ItemStack output : outputItems){
                    dump(output.item);
                }
            }

            if (outputLiquid != null && outputLiquid.liquid != null && hasLiquids) {
                dumpLiquid(outputLiquid.liquid);
            }

            if (outputGas != null && outputGas.gas != null && hasGas) {
                dumpGas(outputGas.gas);
            }
        }
        @Override
        public double sense(LAccess sensor){
            if(sensor == LAccess.progress) return Mathf.clamp(progress);
            return super.sense(sensor);
        }
        @Override
        public boolean shouldAmbientSound(){
            return cons.valid();
        }

        @Override
        public int getMaximumAccepted(Item item){
            return itemCapacity;
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(progress);
            write.f(warmup);
            if(legacyReadWarmup) write.f(0f);
        }
    }
}
