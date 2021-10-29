package gas.world.blocks.power;

import mindustry.annotations.Annotations.*;
import mindustry.logic.*;
import gas.entities.comp.*;
import mindustry.entities.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import mindustry.content.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.experimental.*;
import arc.*;
import gas.world.meta.*;
import mindustry.game.EventType.*;
import gas.world.blocks.units.*;
import gas.world.blocks.defense.*;
import arc.util.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import arc.math.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.meta.*;
import arc.graphics.*;
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
import mindustry.ui.*;
import gas.world.*;
import mindustry.world.consumers.*;
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
import arc.graphics.g2d.*;
import mindustry.world.blocks.logic.*;
import gas.world.modules.*;
import gas.world.blocks.*;
import gas.*;
import gas.io.*;
import gas.world.draw.*;
import mindustry.world.blocks.units.*;
import gas.content.*;
import gas.world.blocks.storage.*;
import mindustry.world.blocks.power.ImpactReactor.*;
import mindustry.graphics.*;
import gas.gen.*;
import arc.struct.*;
import arc.util.io.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import gas.world.meta.values.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;
import gas.world.blocks.power.*;
import static mindustry.Vars.*;

public class GasImpactReactor extends GasPowerGenerator {

    public final int timerUse = timers++;

    public float warmupSpeed = 0.001f;

    public float itemDuration = 60f;

    public int explosionRadius = 23;

    public int explosionDamage = 1900;

    public Effect explodeEffect = Fx.impactReactorExplosion;

    public Color plasma1 = Color.valueOf("ffd06b"), plasma2 = Color.valueOf("ff361b");

    @Load("@-bottom")
    public TextureRegion bottomRegion;

    @Load(value = "@-plasma-#", length = 4)
    public TextureRegion[] plasmaRegions;

    public GasImpactReactor(String name) {
        super(name);
        hasPower = true;
        hasLiquids = true;
        liquidCapacity = 30f;
        hasItems = true;
        outputsPower = consumesPower = true;
        flags = EnumSet.of(BlockFlag.reactor, BlockFlag.generator);
        lightRadius = 115f;
        emitLight = true;
        envEnabled = Env.any;
    }

    @Override
    public void setBars() {
        super.setBars();
        bars.add("poweroutput", (GasGeneratorBuild entity) -> new Bar(() -> Core.bundle.format("bar.poweroutput", Strings.fixed(Math.max(entity.getPowerProduction() - consumes.getPower().usage, 0) * 60 * entity.timeScale, 1)), () -> Pal.powerBar, () -> entity.productionEfficiency));
    }

    @Override
    public void setStats() {
        super.setStats();
        if (hasItems) {
            stats.add(Stat.productionTime, itemDuration / 60f, StatUnit.seconds);
        }
    }

    @Override
    public TextureRegion[] icons() {
        return new TextureRegion[] { bottomRegion, region };
    }

    public class GasImpactReactorBuild extends GasGeneratorBuild {

        public float warmup;

        @Override
        public void updateTile() {
            if (consValid() && power.status >= 0.99f) {
                boolean prevOut = getPowerProduction() <= consumes.getPower().requestedPower(this);
                warmup = Mathf.lerpDelta(warmup, 1f, warmupSpeed * timeScale);
                if (Mathf.equal(warmup, 1f, 0.001f)) {
                    warmup = 1f;
                }
                if (!prevOut && (getPowerProduction() > consumes.getPower().requestedPower(this))) {
                    Events.fire(Trigger.impactPower);
                }
                if (timer(timerUse, itemDuration / timeScale)) {
                    consume();
                }
            } else {
                warmup = Mathf.lerpDelta(warmup, 0f, 0.01f);
            }
            productionEfficiency = Mathf.pow(warmup, 5f);
        }

        @Override
        public float ambientVolume() {
            return warmup;
        }

        @Override
        public void draw() {
            Draw.rect(bottomRegion, x, y);
            for (int i = 0; i < plasmaRegions.length; i++) {
                float r = size * tilesize - 3f + Mathf.absin(Time.time, 2f + i * 1f, 5f - i * 0.5f);
                Draw.color(plasma1, plasma2, (float) i / plasmaRegions.length);
                Draw.alpha((0.3f + Mathf.absin(Time.time, 2f + i * 2f, 0.3f + i * 0.05f)) * warmup);
                Draw.blend(Blending.additive);
                Draw.rect(plasmaRegions[i], x, y, r, r, Time.time * (12 + i * 6f) * warmup);
                Draw.blend();
            }
            Draw.color();
            Draw.rect(region, x, y);
            Draw.color();
        }

        @Override
        public void drawLight() {
            Drawf.light(team, x, y, (110f + Mathf.absin(5, 5f)) * warmup, Tmp.c1.set(plasma2).lerp(plasma1, Mathf.absin(7f, 0.2f)), 0.8f * warmup);
        }

        @Override
        public double sense(LAccess sensor) {
            if (sensor == LAccess.heat)
                return warmup;
            return super.sense(sensor);
        }

        @Override
        public void onDestroyed() {
            super.onDestroyed();
            if (warmup < 0.3f || !state.rules.reactorExplosions)
                return;
            Sounds.explosionbig.at(tile);
            Damage.damage(x, y, explosionRadius * tilesize, explosionDamage * 4);
            Effect.shake(6f, 16f, x, y);
            explodeEffect.at(x, y);
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(warmup);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            warmup = read.f();
        }
    }
}
