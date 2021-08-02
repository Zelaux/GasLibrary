package gas.world.blocks.production;

import gas.GasStack;
import gas.gen.GasBuilding;
import gas.world.GasBlock;
import gas.world.draw.GasDrawBlock;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.struct.EnumSet;
import arc.util.io.Reads;
import arc.util.io.Writes;
import gas.world.meta.GasValue;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.gen.Building;
import mindustry.gen.Sounds;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.Liquid;
import mindustry.type.LiquidStack;
import mindustry.world.meta.BlockFlag;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

import static mindustry.Vars.net;
import static mindustry.Vars.state;

public class GasGenericCrafter extends GasBlock {
    public ItemStack outputItem;
    public LiquidStack outputLiquid;
    public GasStack outputGas;

    public float craftTime = 80;
    public Effect craftEffect = Fx.none;
    public Effect updateEffect = Fx.none;
    public float updateEffectChance = 0.04f;

    public GasDrawBlock drawer = new GasDrawBlock();

    public GasGenericCrafter(String name){
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
    public void setStats(){
        super.setStats();
        aStats.add(Stat.productionTime, craftTime / 60f, StatUnit.seconds);

        if (outputItem != null) {
            aStats.add(Stat.output, outputItem);
        }

        if (outputLiquid != null) {
            aStats.add(Stat.output, outputLiquid.liquid, outputLiquid.amount * (60f / craftTime), true);
        }

        if (outputGas != null) {
            aStats.add(Stat.output, new GasValue(outputGas.gas, outputGas.amount * (60f / craftTime), true));
        }
    }

    @Override
    public void load(){
        super.load();

        drawer.load(this);
    }

    @Override
    public void init(){
        outputsLiquid = outputLiquid != null;
        outputsGas = outputGas != null;
        super.init();
    }

    @Override
    public TextureRegion[] icons(){
        return drawer.icons(this);
    }

    @Override
    public boolean outputsItems(){
        return outputItem != null;
    }

    public class GasGenericCrafterBuild extends GasBuilding {
        public float progress;
        public float totalProgress;
        public float warmup;

        @Override
        public void draw(){
            drawer.draw(this);
        }

        @Override
        public boolean shouldConsume(){
            if (outputItem != null && items.get(outputItem.item) >= itemCapacity) {
                return false;
            }
            boolean gasBool = outputGas != null && gasses != null && gasses.get(outputGas.gas) >= gasCapacity - 0.001f;
            boolean liquidBool = outputLiquid == null || !(liquids!=null && liquids.get(outputLiquid.liquid) >= liquidCapacity - 0.001f);
            return (liquidBool || gasBool) && enabled;
        }
        public void dumpLiquid(Liquid liquid, float scaling) {
            int dump = this.cdump;
            if (liquids.get(liquid) <= 1.0E-4F) return;
            if (!net.client() && state.isCampaign() && team == state.rules.defaultTeam) liquid.unlock();
            for (int i = 0; i < proximity.size; i++) {
                incrementDump(proximity.size);
                Building other = proximity.get((i + dump) % proximity.size);
                other = other.getLiquidDestination(this, liquid);
                if (other != null && other.team == team && other.block.hasLiquids && canDumpLiquid(other, liquid) && other.liquids != null) {
                    float ofract = other.liquids.get(liquid) / other.block.liquidCapacity;
                    float fract = liquids.get(liquid) / block.liquidCapacity;
                    if (ofract < fract) transferLiquid(other, (fract - ofract) * block.liquidCapacity / scaling, liquid);
                }
            }
        }
        @Override
        public void updateTile(){
            if (consValid()) {
                progress += getProgressIncrease(craftTime);
                totalProgress += delta();
                warmup = Mathf.lerpDelta(warmup, 1f, 0.02f);

                if(Mathf.chanceDelta(updateEffectChance)){
                    updateEffect.at(getX() + Mathf.range(size * 4f), getY() + Mathf.range(size * 4));
                }
            } else {
                warmup = Mathf.lerp(warmup, 0f, 0.02f);
            }

            if (progress >= 1f) {
                consume();

                if(outputItem != null){
                    for(int i = 0; i < outputItem.amount; i++){
                        offload(outputItem.item);
                    }
                }

                if(outputLiquid != null){
                    handleLiquid(this, outputLiquid.liquid, outputLiquid.amount);
                }

                if(outputGas != null){
                    handleGas(this, outputGas.gas, outputGas.amount);
                }

                craftEffect.at(x, y);
                progress = 0f;
            }

            if (outputItem != null && timer(timerDump, dumpTime)) {
                dump(outputItem.item);
            }

            if (outputLiquid != null && outputLiquid.liquid!=null && hasLiquids) {
                dumpLiquid(outputLiquid.liquid);
            }

            if (outputGas != null && outputGas.gas!=null && hasGas) {
                dumpGas(outputGas.gas);
            }
        }

        @Override
        public int getMaximumAccepted(Item item){
            return itemCapacity;
        }

        @Override
        public boolean shouldAmbientSound(){
            return cons.valid();
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(progress);
            write.f(warmup);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            progress = read.f();
            warmup = read.f();
        }
    }
}
