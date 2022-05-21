package gas.world.blocks.power;

import gas.entities.comp.*;
import mindustry.entities.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import mindustry.world.blocks.power.ConsumeGenerator.*;
import mindustry.content.*;
import gas.content.*;
import mindustry.world.blocks.defense.turrets.*;
import gas.world.blocks.payloads.*;
import gas.world.meta.*;
import gas.world.blocks.units.*;
import mindustry.world.blocks.heat.*;
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
import gas.world.blocks.campaign.*;
import mindustry.world.modules.*;
import gas.ui.*;
import mindustry.world.blocks.environment.*;
import gas.world.consumers.*;
import mindustry.world.blocks.payloads.*;
import mindustry.world.blocks.*;
import gas.world.blocks.production.GasGenericCrafter.*;
import mindustry.world.blocks.logic.*;
import gas.world.modules.*;
import gas.world.blocks.*;
import arc.graphics.*;
import gas.*;
import gas.io.*;
import gas.world.draw.*;
import gas.gen.*;
import gas.world.blocks.storage.*;
import mindustry.world.blocks.units.*;
import mindustry.graphics.*;
import gas.world.blocks.production.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import mindustry.world.blocks.power.*;
import mindustry.type.*;
import mindustry.world.blocks.sandbox.*;

/**
 * A generator that just takes in certain items or liquids. Basically SingleTypeGenerator, but not unreliable garbage.
 */
public class GasConsumeGenerator extends GasPowerGenerator {

    /**
     * The time in number of ticks during which a single item will produce power.
     */
    public float itemDuration = 120f;

    public float effectChance = 0.01f;

    public Effect generateEffect = Fx.none, consumeEffect = Fx.none;

    public float generateEffectRange = 3f;

    @Nullable
    public LiquidStack liquidOutput;

    @Nullable
    public ConsumeItemFilter filterItem;

    @Nullable
    public ConsumeLiquidFilter filterLiquid;

    public GasConsumeGenerator(String name) {
        super(name);
    }

    @Override
    public void setBars() {
        super.setBars();
        if (liquidOutput != null) {
            addLiquidBar(liquidOutput.liquid);
        }
    }

    @Override
    public void init() {
        filterItem = findConsumer(c -> c instanceof ConsumeItemFilter);
        filterLiquid = findConsumer(c -> c instanceof ConsumeLiquidFilter);
        if (liquidOutput != null) {
            outputsLiquid = true;
            hasLiquids = true;
        }
        // TODO hardcoded
        emitLight = true;
        lightRadius = 65f * size;
        super.init();
    }

    @Override
    public void setStats() {
        super.setStats();
        if (hasItems) {
            stats.add(Stat.productionTime, itemDuration / 60f, StatUnit.seconds);
        }
        if (liquidOutput != null) {
            stats.add(Stat.output, StatValues.liquid(liquidOutput.liquid, liquidOutput.amount * 60f, true));
        }
    }

    public class GasConsumeGeneratorBuild extends GasGeneratorBuild {

        public float warmup, totalTime, efficiencyMultiplier = 1f;

        @Override
        public void updateEfficiencyMultiplier() {
            if (filterItem != null) {
                float m = filterItem.efficiencyMultiplier(this);
                if (m > 0)
                    efficiencyMultiplier = m;
            } else if (filterLiquid != null) {
                float m = filterLiquid.efficiencyMultiplier(this);
                if (m > 0)
                    efficiencyMultiplier = m;
            }
        }

        @Override
        public void updateTile() {
            boolean valid = efficiency > 0;
            warmup = Mathf.lerpDelta(warmup, valid ? 1f : 0f, 0.05f);
            productionEfficiency = efficiency * efficiencyMultiplier;
            totalTime += warmup * Time.delta;
            // randomly produce the effect
            if (valid && Mathf.chanceDelta(effectChance)) {
                generateEffect.at(x + Mathf.range(generateEffectRange), y + Mathf.range(generateEffectRange));
            }
            // take in items periodically
            if (hasItems && valid && generateTime <= 0f) {
                consume();
                consumeEffect.at(x + Mathf.range(generateEffectRange), y + Mathf.range(generateEffectRange));
                generateTime = 1f;
            }
            if (liquidOutput != null) {
                float added = Math.min(productionEfficiency * delta() * liquidOutput.amount, liquidCapacity - liquids.get(liquidOutput.liquid));
                liquids.add(liquidOutput.liquid, added);
                dumpLiquid(liquidOutput.liquid);
            }
            // generation time always goes down, but only at the end so consumeTriggerValid doesn't assume fake items
            generateTime -= delta() / itemDuration;
        }

        @Override
        public boolean consumeTriggerValid() {
            return generateTime > 0;
        }

        @Override
        public float warmup() {
            return warmup;
        }

        @Override
        public float totalProgress() {
            return totalTime;
        }

        @Override
        public void drawLight() {
            // ???
            drawer.drawLight(this);
            // TODO hard coded
            Drawf.light(x, y, (60f + Mathf.absin(10f, 5f)) * size, Color.orange, 0.5f * warmup);
        }
    }
}
