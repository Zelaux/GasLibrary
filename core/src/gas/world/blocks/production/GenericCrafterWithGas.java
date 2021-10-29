package gas.world.blocks.production;

import arc.math.*;
import arc.util.*;
import gas.*;
import gas.world.meta.*;
import mindustry.type.*;
import mindustry.world.meta.*;

public class GenericCrafterWithGas extends GasGenericCrafter{
    public @Nullable GasStack outputGas;

    public GenericCrafterWithGas(String name){
        super(name);
    }

    @Override
    public void setStats(){
        super.setStats();

        if (outputGas != null) {
            aStats.add(Stat.output, new GasValue(outputGas.gas, outputGas.amount * (60f / craftTime), true));
        }
    }

    @Override
    public void init(){
        super.init();

        outputsGas = outputGas != null;
    }
    public class GenericCrafterWithGasBuild extends GasGenericCrafterBuild{

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

            if (outputGas != null && outputGas.gas != null && hasGasses) {
                dumpGas(outputGas.gas);
            }
        }
    }
}
