package gas.world.blocks.power;

import gas.entities.comp.*;
import mindustry.entities.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import mindustry.content.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.experimental.*;
import arc.*;
import gas.world.meta.*;
import mindustry.annotations.Annotations.*;
import gas.world.blocks.units.*;
import gas.world.blocks.defense.*;
import arc.util.*;
import mindustry.world.blocks.legacy.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import arc.math.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.meta.*;
import arc.graphics.*;
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
import mindustry.world.blocks.power.ItemLiquidGenerator.*;
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
import mindustry.graphics.*;
import gas.world.blocks.production.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import gas.world.meta.values.*;
import mindustry.world.blocks.power.*;
import mindustry.type.*;
import mindustry.world.blocks.sandbox.*;
import static mindustry.Vars.*;

/**
 * Power generation block which can use items, liquids or both as input sources for power production.
 * Liquids will take priority over items.
 */
public class GasItemLiquidGenerator extends GasPowerGenerator {

    public float minItemEfficiency = 0.2f;

    /**
     * The time in number of ticks during which a single item will produce power.
     */
    public float itemDuration = 70f;

    public float minLiquidEfficiency = 0.2f;

    /**
     * Maximum liquid used per frame.
     */
    public float maxLiquidGenerate = 0.4f;

    public Effect generateEffect = Fx.generatespark;

    public float generateEffectRnd = 3f;

    public Effect explodeEffect = Fx.generatespark;

    public Color heatColor = Color.valueOf("ff9b59");

    @Load("@-top")
    public TextureRegion topRegion;

    @Load("@-liquid")
    public TextureRegion liquidRegion;

    public boolean randomlyExplode = true;

    public boolean defaults = false;

    public GasItemLiquidGenerator(boolean hasItems, boolean hasLiquids, String name) {
        this(name);
        this.hasItems = hasItems;
        this.hasLiquids = hasLiquids;
        setDefaults();
    }

    public GasItemLiquidGenerator(String name) {
        super(name);
    }

    protected void setDefaults() {
        if (hasItems) {
            consumes.add(new ConsumeItemFilter(item -> getItemEfficiency(item) >= minItemEfficiency)).update(false).optional(true, false);
        }
        if (hasLiquids) {
            consumes.add(new ConsumeLiquidFilter(liquid -> getLiquidEfficiency(liquid) >= minLiquidEfficiency, maxLiquidGenerate)).update(false).optional(true, false);
        }
        defaults = true;
    }

    @Override
    public void init() {
        emitLight = true;
        lightRadius = 65f * size;
        if (!defaults) {
            setDefaults();
        }
        super.init();
    }

    @Override
    public void setStats() {
        super.setStats();
        if (hasItems) {
            stats.add(Stat.productionTime, itemDuration / 60f, StatUnit.seconds);
        }
    }

    protected float getItemEfficiency(Item item) {
        return 0.0f;
    }

    protected float getLiquidEfficiency(Liquid liquid) {
        return 0.0f;
    }

    public class GasItemLiquidGeneratorBuild extends GasGeneratorBuild {

        public float explosiveness, heat, totalTime;

        @Override
        public boolean productionValid() {
            return generateTime > 0;
        }

        @Override
        public void updateTile() {
            // Note: Do not use this delta when calculating the amount of power or the power efficiency, but use it for resource consumption if necessary.
            // Power amount is delta'd by PowerGraph class already.
            float calculationDelta = delta();
            boolean cons = consValid();
            heat = Mathf.lerpDelta(heat, generateTime >= 0.001f && enabled && cons ? 1f : 0f, 0.05f);
            if (!cons) {
                productionEfficiency = 0.0f;
                return;
            }
            Liquid liquid = null;
            for (var other : content.liquids()) {
                if (hasLiquids && liquids.get(other) >= 0.001f && getLiquidEfficiency(other) >= minLiquidEfficiency) {
                    liquid = other;
                    break;
                }
            }
            totalTime += heat * Time.delta;
            // liquid takes priority over solids
            if (hasLiquids && liquid != null && liquids.get(liquid) >= 0.001f) {
                float baseLiquidEfficiency = getLiquidEfficiency(liquid);
                float maximumPossible = maxLiquidGenerate * calculationDelta;
                float used = Math.min(liquids.get(liquid) * calculationDelta, maximumPossible);
                liquids.remove(liquid, used * power.graph.getUsageFraction());
                productionEfficiency = baseLiquidEfficiency * used / maximumPossible;
                if (used > 0.001f && (generateTime -= delta()) <= 0f) {
                    generateEffect.at(x + Mathf.range(generateEffectRnd), y + Mathf.range(generateEffectRnd));
                    generateTime = 1f;
                }
            } else if (hasItems) {
                // No liquids accepted or none supplied, try using items if accepted
                if (generateTime <= 0f && items.total() > 0) {
                    generateEffect.at(x + Mathf.range(generateEffectRnd), y + Mathf.range(generateEffectRnd));
                    Item item = items.take();
                    productionEfficiency = getItemEfficiency(item);
                    explosiveness = item.explosiveness;
                    generateTime = 1f;
                }
                if (generateTime > 0f) {
                    generateTime -= Math.min(1f / itemDuration * delta() * power.graph.getUsageFraction(), generateTime);
                    if (randomlyExplode && state.rules.reactorExplosions && Mathf.chance(delta() * 0.06 * Mathf.clamp(explosiveness - 0.5f))) {
                        // this block is run last so that in the event of a block destruction, no code relies on the block type
                        Core.app.post(() -> {
                            damage(Mathf.random(11f));
                            explodeEffect.at(x + Mathf.range(size * tilesize / 2f), y + Mathf.range(size * tilesize / 2f));
                        });
                    }
                } else {
                    productionEfficiency = 0.0f;
                }
            }
        }

        @Override
        public void draw() {
            super.draw();
            if (hasItems) {
                Draw.color(heatColor);
                Draw.alpha(heat * 0.4f + Mathf.absin(Time.time, 8f, 0.6f) * heat);
                Draw.rect(topRegion, x, y);
                Draw.reset();
            }
            if (hasLiquids) {
                Drawf.liquid(liquidRegion, x, y, liquids.total() / liquidCapacity, liquids.current().color);
            }
        }

        @Override
        public void drawLight() {
            Drawf.light(team, x, y, (60f + Mathf.absin(10f, 5f)) * size, Color.orange, 0.5f * heat);
        }
    }
}
