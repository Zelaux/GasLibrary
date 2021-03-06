package gas.world.blocks.defense;

import gas.entities.comp.*;
import mindustry.entities.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import mindustry.world.blocks.defense.ForceProjector.*;
import mindustry.content.*;
import gas.content.*;
import mindustry.world.blocks.defense.turrets.*;
import gas.world.blocks.payloads.*;
import gas.world.meta.*;
import mindustry.annotations.Annotations.*;
import gas.world.blocks.units.*;
import mindustry.world.blocks.heat.*;
import gas.world.blocks.defense.*;
import arc.util.*;
import mindustry.world.blocks.legacy.*;
import mindustry.gen.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import arc.math.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.meta.*;
import gas.world.blocks.heat.*;
import gas.world.blocks.distribution.*;
import mindustry.world.blocks.distribution.*;
import gas.world.blocks.power.*;
import mindustry.world.*;
import gas.world.blocks.sandbox.*;
import mindustry.world.blocks.storage.*;
import gas.world.blocks.liquid.*;
import mindustry.game.*;
import gas.entities.*;
import mindustry.world.blocks.campaign.*;
import gas.world.blocks.defense.turrets.*;
import mindustry.ui.*;
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
import gas.world.draw.*;
import arc.func.*;
import gas.gen.*;
import gas.world.blocks.storage.*;
import mindustry.world.blocks.units.*;
import mindustry.graphics.*;
import gas.world.blocks.production.*;
import arc.util.io.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import mindustry.logic.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;
import static mindustry.Vars.*;

public class GasForceProjector extends GasBlock {

    public final int timerUse = timers++;

    public float phaseUseTime = 350f;

    public float phaseRadiusBoost = 80f;

    public float phaseShieldBoost = 400f;

    public float radius = 101.7f;

    public float shieldHealth = 700f;

    public float cooldownNormal = 1.75f;

    public float cooldownLiquid = 1.5f;

    public float cooldownBrokenBase = 0.35f;

    public float coolantConsumption = 0.1f;

    public boolean consumeCoolant = true;

    public Effect absorbEffect = Fx.absorb;

    public Effect shieldBreakEffect = Fx.shieldBreak;

    @Load("@-top")
    public TextureRegion topRegion;

    // TODO json support
    @Nullable
    public Consume itemConsumer, coolantConsumer;

    protected static GasForceBuild paramEntity;

    protected static Effect paramEffect;

    protected static final Cons<Bullet> shieldConsumer = bullet -> {
        if (bullet.team != paramEntity.team && bullet.type.absorbable && Intersector.isInsideHexagon(paramEntity.x, paramEntity.y, paramEntity.realRadius() * 2f, bullet.x, bullet.y)) {
            bullet.absorb();
            paramEffect.at(bullet);
            paramEntity.hit = 1f;
            paramEntity.buildup += bullet.damage;
        }
    };

    public GasForceProjector(String name) {
        super(name);
        update = true;
        solid = true;
        group = BlockGroup.projectors;
        hasPower = true;
        hasLiquids = true;
        hasItems = true;
        envEnabled |= Env.space;
        ambientSound = Sounds.shield;
        ambientSoundVolume = 0.08f;
        if (consumeCoolant) {
            consume(coolantConsumer = new ConsumeCoolant(coolantConsumption)).boost().update(false);
        }
    }

    @Override
    public void init() {
        updateClipRadius(radius + phaseRadiusBoost + 3f);
        super.init();
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("shield", (GasForceBuild entity) -> new Bar("stat.shieldhealth", Pal.accent, () -> entity.broken ? 0f : 1f - entity.buildup / (shieldHealth + phaseShieldBoost * entity.phaseHeat)).blink(Color.white));
    }

    @Override
    public boolean outputsItems() {
        return false;
    }

    @Override
    public void setStats() {
        boolean consItems = itemConsumer != null;
        if (consItems)
            stats.timePeriod = phaseUseTime;
        super.setStats();
        stats.add(Stat.shieldHealth, shieldHealth, StatUnit.none);
        stats.add(Stat.cooldownTime, (int) (shieldHealth / cooldownBrokenBase / 60f), StatUnit.seconds);
        if (consItems) {
            stats.add(Stat.boostEffect, phaseRadiusBoost / tilesize, StatUnit.blocks);
            stats.add(Stat.boostEffect, phaseShieldBoost, StatUnit.shieldHealth);
        }
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        Draw.color(Pal.gray);
        Lines.stroke(3f);
        Lines.poly(x * tilesize + offset, y * tilesize + offset, 6, radius);
        Draw.color(player.team().color);
        Lines.stroke(1f);
        Lines.poly(x * tilesize + offset, y * tilesize + offset, 6, radius);
        Draw.color();
    }

    public class GasForceBuild extends GasBuilding implements Ranged {

        public boolean broken = true;

        public float buildup, radscl, hit, warmup, phaseHeat;

        @Override
        public float range() {
            return realRadius();
        }

        @Override
        public boolean shouldAmbientSound() {
            return !broken && realRadius() > 1f;
        }

        @Override
        public void onRemoved() {
            float radius = realRadius();
            if (!broken && radius > 1f)
                Fx.forceShrink.at(x, y, radius, team.color);
            super.onRemoved();
        }

        @Override
        public void pickedUp() {
            super.pickedUp();
            radscl = warmup = 0f;
        }

        @Override
        public boolean inFogTo(Team viewer) {
            return false;
        }

        @Override
        public void updateTile() {
            boolean phaseValid = itemConsumer != null && itemConsumer.efficiency(this) > 0;
            phaseHeat = Mathf.lerpDelta(phaseHeat, Mathf.num(phaseValid), 0.1f);
            if (phaseValid && !broken && timer(timerUse, phaseUseTime) && efficiency > 0) {
                consume();
            }
            radscl = Mathf.lerpDelta(radscl, broken ? 0f : warmup, 0.05f);
            if (Mathf.chanceDelta(buildup / shieldHealth * 0.1f)) {
                Fx.reactorsmoke.at(x + Mathf.range(tilesize / 2f), y + Mathf.range(tilesize / 2f));
            }
            warmup = Mathf.lerpDelta(warmup, efficiency, 0.1f);
            if (buildup > 0) {
                float scale = !broken ? cooldownNormal : cooldownBrokenBase;
                // TODO I hate this system
                if (coolantConsumer != null) {
                    if (coolantConsumer.efficiency(this) > 0) {
                        coolantConsumer.update(this);
                        scale *= (cooldownLiquid * (1f + (liquids.current().heatCapacity - 0.4f) * 0.9f));
                    }
                }
                buildup -= delta() * scale;
            }
            if (broken && buildup <= 0) {
                broken = false;
            }
            if (buildup >= shieldHealth + phaseShieldBoost * phaseHeat && !broken) {
                broken = true;
                buildup = shieldHealth;
                shieldBreakEffect.at(x, y, realRadius(), team.color);
            }
            if (hit > 0f) {
                hit -= 1f / 5f * Time.delta;
            }
            deflectBullets();
        }

        public void deflectBullets() {
            float realRadius = realRadius();
            if (realRadius > 0 && !broken) {
                paramEntity = this;
                paramEffect = absorbEffect;
                Groups.bullet.intersect(x - realRadius, y - realRadius, realRadius * 2f, realRadius * 2f, shieldConsumer);
            }
        }

        public float realRadius() {
            return (radius + phaseHeat * phaseRadiusBoost) * radscl;
        }

        @Override
        public double sense(LAccess sensor) {
            if (sensor == LAccess.heat)
                return buildup;
            return super.sense(sensor);
        }

        @Override
        public void draw() {
            super.draw();
            if (buildup > 0f) {
                Draw.alpha(buildup / shieldHealth * 0.75f);
                Draw.z(Layer.blockAdditive);
                Draw.blend(Blending.additive);
                Draw.rect(topRegion, x, y);
                Draw.blend();
                Draw.z(Layer.block);
                Draw.reset();
            }
            drawShield();
        }

        public void drawShield() {
            if (!broken) {
                float radius = realRadius();
                Draw.z(Layer.shields);
                Draw.color(team.color, Color.white, Mathf.clamp(hit));
                if (renderer.animateShields) {
                    Fill.poly(x, y, 6, radius);
                } else {
                    Lines.stroke(1.5f);
                    Draw.alpha(0.09f + Mathf.clamp(0.08f * hit));
                    Fill.poly(x, y, 6, radius);
                    Draw.alpha(1f);
                    Lines.poly(x, y, 6, radius);
                    Draw.reset();
                }
            }
            Draw.reset();
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.bool(broken);
            write.f(buildup);
            write.f(radscl);
            write.f(warmup);
            write.f(phaseHeat);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            broken = read.bool();
            buildup = read.f();
            radscl = read.f();
            warmup = read.f();
            phaseHeat = read.f();
        }
    }
}
