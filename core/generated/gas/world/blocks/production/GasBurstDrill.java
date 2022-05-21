package gas.world.blocks.production;

import gas.entities.comp.*;
import mindustry.entities.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import mindustry.content.*;
import gas.content.*;
import mindustry.world.blocks.defense.turrets.*;
import gas.world.blocks.payloads.*;
import gas.world.meta.*;
import mindustry.annotations.Annotations.*;
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
import arc.graphics.g2d.*;
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
import mindustry.world.blocks.production.BurstDrill.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;

public class GasBurstDrill extends GasDrill {

    public float shake = 2f;

    public Interp speedCurve = Interp.pow2In;

    @Load("@-top-invert")
    public TextureRegion topInvertRegion;

    @Load("@-glow")
    public TextureRegion glowRegion;

    @Load("@-arrow")
    public TextureRegion arrowRegion;

    @Load("@-arrow-blur")
    public TextureRegion arrowBlurRegion;

    public float invertedTime = 200f;

    public float arrowSpacing = 4f, arrowOffset = 0f;

    public int arrows = 3;

    public Color arrowColor = Color.valueOf("feb380"), baseArrowColor = Color.valueOf("6e7080");

    public Color glowColor = arrowColor.cpy();

    public GasBurstDrill(String name) {
        super(name);
        // does not drill in the traditional sense, so this is not even used
        hardnessDrillMultiplier = 0f;
        // generally at center
        drillEffectRnd = 0f;
        drillEffect = Fx.shockwave;
    }

    @Override
    public TextureRegion[] icons() {
        return new TextureRegion[] { region, topRegion };
    }

    public class GasBurstDrillBuild extends GasDrillBuild {

        // used so the lights don't fade out immediately
        public float smoothProgress = 0f;

        public float invertTime = 0f;

        @Override
        public void updateTile() {
            if (dominantItem == null) {
                return;
            }
            if (invertTime > 0f)
                invertTime -= delta() / invertedTime;
            if (timer(timerDump, dumpTime)) {
                dump(items.has(dominantItem) ? dominantItem : null);
            }
            smoothProgress = Mathf.lerpDelta(smoothProgress, progress / (drillTime - 20f), 0.1f);
            if (items.total() <= itemCapacity - dominantItems && dominantItems > 0 && efficiency > 0) {
                warmup = Mathf.approachDelta(warmup, progress / drillTime, 0.01f);
                float speed = efficiency;
                timeDrilled += speedCurve.apply(progress / drillTime) * speed;
                lastDrillSpeed = 1f / drillTime * speed * dominantItems;
                progress += delta() * speed;
            } else {
                warmup = Mathf.approachDelta(warmup, 0f, 0.01f);
                lastDrillSpeed = 0f;
                return;
            }
            if (dominantItems > 0 && progress >= drillTime && items.total() < itemCapacity) {
                for (int i = 0; i < dominantItems; i++) {
                    offload(dominantItem);
                }
                invertTime = 1f;
                progress %= drillTime;
                if (wasVisible) {
                    Effect.shake(shake, shake, this);
                    drillEffect.at(x + Mathf.range(drillEffectRnd), y + Mathf.range(drillEffectRnd), dominantItem.color);
                }
            }
        }

        @Override
        public boolean shouldConsume() {
            return items.total() <= itemCapacity - dominantItems && enabled;
        }

        @Override
        public void draw() {
            Draw.rect(region, x, y);
            drawDefaultCracks();
            Draw.rect(topRegion, x, y);
            if (invertTime > 0 && topInvertRegion.found()) {
                Draw.alpha(Interp.pow3Out.apply(invertTime));
                Draw.rect(topInvertRegion, x, y);
                Draw.color();
            }
            if (dominantItem != null && drawMineItem) {
                Draw.color(dominantItem.color);
                Draw.rect(itemRegion, x, y);
                Draw.color();
            }
            float fract = smoothProgress;
            Draw.color(arrowColor);
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < arrows; j++) {
                    float arrowFract = (arrows - 1 - j);
                    float a = Mathf.clamp(fract * arrows - arrowFract);
                    Tmp.v1.trns(i * 90 + 45, j * arrowSpacing + arrowOffset);
                    // TODO maybe just use arrow alpha and draw gray on the base?
                    Draw.z(Layer.block);
                    Draw.color(baseArrowColor, arrowColor, a);
                    Draw.rect(arrowRegion, x + Tmp.v1.x, y + Tmp.v1.y, i * 90);
                    Draw.color(arrowColor);
                    if (arrowBlurRegion.found()) {
                        Draw.z(Layer.blockAdditive);
                        Draw.blend(Blending.additive);
                        Draw.alpha(Mathf.pow(a, 10f));
                        Draw.rect(arrowBlurRegion, x + Tmp.v1.x, y + Tmp.v1.y, i * 90);
                        Draw.blend();
                    }
                }
            }
            Draw.color();
            if (glowRegion.found()) {
                Drawf.additive(glowRegion, Tmp.c2.set(glowColor).a(Mathf.pow(fract, 3f) * glowColor.a), x, y);
            }
        }
    }
}
