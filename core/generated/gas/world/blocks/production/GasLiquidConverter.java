package gas.world.blocks.production;

import gas.entities.comp.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.experimental.*;
import gas.world.meta.*;
import gas.world.blocks.units.*;
import gas.world.blocks.defense.*;
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
import mindustry.world.consumers.*;
import gas.world.modules.*;
import gas.world.blocks.*;
import mindustry.world.blocks.production.LiquidConverter.*;
import gas.*;
import gas.io.*;
import gas.world.blocks.payloads.*;
import mindustry.world.blocks.units.*;
import gas.content.*;
import gas.world.blocks.storage.*;
import gas.world.blocks.production.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import gas.world.meta.values.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;

public class GasLiquidConverter extends GasGenericCrafter {

    public GasLiquidConverter(String name) {
        super(name);
        hasLiquids = true;
    }

    @Override
    public boolean outputsItems() {
        return false;
    }

    @Override
    public void init() {
        if (!consumes.has(ConsumeType.liquid) || !(consumes.get(ConsumeType.liquid) instanceof ConsumeLiquid)) {
            throw new RuntimeException("LiquidsConverters must have a ConsumeLiquid. Note that filters are not supported.");
        }
        ConsumeLiquid cl = consumes.get(ConsumeType.liquid);
        cl.update(false);
        outputLiquid.amount = cl.amount;
        super.init();
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.remove(Stat.output);
        stats.add(Stat.output, outputLiquid.liquid, outputLiquid.amount * 60f, true);
    }

    public class GasLiquidConverterBuild extends GasGenericCrafterBuild {

        @Override
        public void drawLight() {
            if (hasLiquids && drawLiquidLight && outputLiquid.liquid.lightColor.a > 0.001f) {
                drawLiquidLight(outputLiquid.liquid, liquids.get(outputLiquid.liquid));
            }
        }

        @Override
        public void updateTile() {
            ConsumeLiquid cl = consumes.get(ConsumeType.liquid);
            if (cons.valid()) {
                if (Mathf.chanceDelta(updateEffectChance)) {
                    updateEffect.at(x + Mathf.range(size * 4f), y + Mathf.range(size * 4));
                }
                warmup = Mathf.lerpDelta(warmup, 1f, 0.02f);
                float use = Math.min(cl.amount * edelta(), liquidCapacity - liquids.get(outputLiquid.liquid));
                float ratio = outputLiquid.amount / cl.amount;
                liquids.remove(cl.liquid, Math.min(use, liquids.get(cl.liquid)));
                progress += use / cl.amount;
                liquids.add(outputLiquid.liquid, use * ratio);
                if (progress >= craftTime) {
                    consume();
                    progress %= craftTime;
                }
            } else {
                // warmup is still 1 even if not consuming
                warmup = Mathf.lerp(warmup, cons.canConsume() ? 1f : 0f, 0.02f);
            }
            dumpLiquid(outputLiquid.liquid);
        }
    }
}
