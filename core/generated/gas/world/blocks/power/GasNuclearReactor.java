package gas.world.blocks.power;

import mindustry.annotations.Annotations.*;
import mindustry.logic.*;
import gas.entities.comp.*;
import mindustry.entities.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import mindustry.content.*;
import gas.content.*;
import mindustry.world.blocks.defense.turrets.*;
import gas.world.blocks.payloads.*;
import arc.*;
import gas.world.meta.*;
import mindustry.game.EventType.*;
import gas.world.blocks.units.*;
import mindustry.world.blocks.heat.*;
import arc.util.*;
import mindustry.world.blocks.legacy.*;
import mindustry.gen.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import arc.math.*;
import mindustry.world.meta.*;
import gas.world.blocks.heat.*;
import gas.world.blocks.defense.*;
import gas.world.draw.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.blocks.distribution.*;
import gas.world.blocks.power.*;
import mindustry.world.*;
import gas.world.blocks.sandbox.*;
import mindustry.world.blocks.storage.*;
import gas.world.blocks.liquid.*;
import gas.entities.*;
import mindustry.world.blocks.power.NuclearReactor.*;
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
import arc.graphics.*;
import gas.*;
import gas.io.*;
import mindustry.ui.*;
import gas.gen.*;
import gas.world.blocks.storage.*;
import mindustry.world.blocks.units.*;
import mindustry.graphics.*;
import gas.world.blocks.production.*;
import arc.struct.*;
import arc.util.io.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import mindustry.world.blocks.power.*;
import mindustry.type.*;
import mindustry.world.blocks.sandbox.*;
import static mindustry.Vars.*;

public class GasNuclearReactor extends GasPowerGenerator {

    public final int timerFuel = timers++;

    public final Vec2 tr = new Vec2();

    public Color lightColor = Color.valueOf("7f19ea");

    public Color coolColor = new Color(1, 1, 1, 0f);

    public Color hotColor = Color.valueOf("ff9575a3");

    public Effect explodeEffect = Fx.reactorExplosion;

    /**
     * ticks to consume 1 fuel
     */
    public float itemDuration = 120;

    /**
     * heating per frame * fullness
     */
    public float heating = 0.01f;

    /**
     * threshold at which block starts smoking
     */
    public float smokeThreshold = 0.3f;

    /**
     * heat threshold at which lights start flashing
     */
    public float flashThreshold = 0.46f;

    public int explosionRadius = 19;

    public int explosionDamage = 1250;

    /**
     * heat removed per unit of coolant
     */
    public float coolantPower = 0.5f;

    public float smoothLight;

    public Item fuelItem = Items.thorium;

    @Load("@-top")
    public TextureRegion topRegion;

    @Load("@-lights")
    public TextureRegion lightsRegion;

    public GasNuclearReactor(String name) {
        super(name);
        itemCapacity = 30;
        liquidCapacity = 30;
        hasItems = true;
        hasLiquids = true;
        rebuildable = false;
        flags = EnumSet.of(BlockFlag.reactor, BlockFlag.generator);
        schematicPriority = -5;
        envEnabled = Env.any;
    }

    @Override
    public void setStats() {
        super.setStats();
        if (hasItems) {
            stats.add(Stat.productionTime, itemDuration / 60f, StatUnit.seconds);
        }
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("heat", (GasNuclearReactorBuild entity) -> new Bar("bar.heat", Pal.lightOrange, () -> entity.heat));
    }

    public class GasNuclearReactorBuild extends GasGeneratorBuild {

        public float heat;

        public float flash;

        @Override
        public void updateTile() {
            int fuel = items.get(fuelItem);
            float fullness = (float) fuel / itemCapacity;
            productionEfficiency = fullness;
            if (fuel > 0 && enabled) {
                heat += fullness * heating * Math.min(delta(), 4f);
                if (timer(timerFuel, itemDuration / timeScale)) {
                    consume();
                }
            } else {
                productionEfficiency = 0f;
            }
            if (heat > 0) {
                float maxUsed = Math.min(liquids.currentAmount(), heat / coolantPower);
                heat -= maxUsed * coolantPower;
                liquids.remove(liquids.current(), maxUsed);
            }
            if (heat > smokeThreshold) {
                // ranges from 1.0 to 2.0
                float smoke = 1.0f + (heat - smokeThreshold) / (1f - smokeThreshold);
                if (Mathf.chance(smoke / 20.0 * delta())) {
                    Fx.reactorsmoke.at(x + Mathf.range(size * tilesize / 2f), y + Mathf.range(size * tilesize / 2f));
                }
            }
            heat = Mathf.clamp(heat);
            if (heat >= 0.999f) {
                Events.fire(Trigger.thoriumReactorOverheat);
                kill();
            }
        }

        @Override
        public double sense(LAccess sensor) {
            if (sensor == LAccess.heat)
                return heat;
            return super.sense(sensor);
        }

        @Override
        public void onDestroyed() {
            super.onDestroyed();
            Sounds.explosionbig.at(this);
            int fuel = items.get(fuelItem);
            if ((fuel < 5 && heat < 0.5f) || !state.rules.reactorExplosions)
                return;
            Effect.shake(6f, 16f, x, y);
            // * ((float)fuel / itemCapacity) to scale based on fullness
            Damage.damage(x, y, explosionRadius * tilesize, explosionDamage * 4);
            explodeEffect.at(x, y);
        }

        @Override
        public void drawLight() {
            float fract = productionEfficiency;
            smoothLight = Mathf.lerpDelta(smoothLight, fract, 0.08f);
            Drawf.light(x, y, (90f + Mathf.absin(5, 5f)) * smoothLight, Tmp.c1.set(lightColor).lerp(Color.scarlet, heat), 0.6f * smoothLight);
        }

        @Override
        public void draw() {
            super.draw();
            Draw.color(coolColor, hotColor, heat);
            Fill.rect(x, y, size * tilesize, size * tilesize);
            Draw.color(liquids.current().color);
            Draw.alpha(liquids.currentAmount() / liquidCapacity);
            Draw.rect(topRegion, x, y);
            if (heat > flashThreshold) {
                flash += (1f + ((heat - flashThreshold) / (1f - flashThreshold)) * 5.4f) * Time.delta;
                Draw.color(Color.red, Color.yellow, Mathf.absin(flash, 9f, 1f));
                Draw.alpha(0.3f);
                Draw.rect(lightsRegion, x, y);
            }
            Draw.reset();
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(heat);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            heat = read.f();
        }
    }
}
