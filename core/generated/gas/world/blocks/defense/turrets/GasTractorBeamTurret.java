package gas.world.blocks.defense.turrets;

import gas.entities.comp.*;
import mindustry.entities.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import mindustry.content.*;
import gas.content.*;
import mindustry.type.*;
import gas.world.blocks.payloads.*;
import mindustry.world.blocks.defense.turrets.TractorBeamTurret.*;
import gas.world.meta.*;
import mindustry.annotations.Annotations.*;
import gas.world.blocks.units.*;
import mindustry.world.blocks.heat.*;
import arc.audio.*;
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
import mindustry.world.blocks.defense.turrets.*;
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
import mindustry.world.blocks.defense.turrets.Turret.*;
import gas.world.blocks.storage.*;
import mindustry.world.blocks.units.*;
import mindustry.graphics.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.world.blocks.defense.*;
import gas.world.blocks.production.*;
import gas.entities.bullets.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;
import static mindustry.Vars.*;

public class GasTractorBeamTurret extends GasBaseTurret {

    public final int timerTarget = timers++;

    public float retargetTime = 5f;

    public float shootCone = 6f;

    public float shootLength = 5f;

    public float laserWidth = 0.6f;

    public float force = 0.3f;

    public float scaledForce = 0f;

    public float damage = 0f;

    public boolean targetAir = true, targetGround = false;

    public Color laserColor = Color.white;

    public StatusEffect status = StatusEffects.none;

    public float statusDuration = 300;

    public Sound shootSound = Sounds.tractorbeam;

    public float shootSoundVolume = 0.9f;

    @Load(value = "@-base", fallback = "block-@size")
    public TextureRegion baseRegion;

    @Load("@-laser")
    public TextureRegion laser;

    @Load(value = "@-laser-start", fallback = "@-laser-end")
    public TextureRegion laserStart;

    @Load("@-laser-end")
    public TextureRegion laserEnd;

    public GasTractorBeamTurret(String name) {
        super(name);
        rotateSpeed = 10f;
        coolantMultiplier = 1f;
        envEnabled |= Env.space;
    }

    @Override
    public TextureRegion[] icons() {
        return new TextureRegion[] { baseRegion, region };
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.targetsAir, targetAir);
        stats.add(Stat.targetsGround, targetGround);
        stats.add(Stat.damage, damage * 60f, StatUnit.perSecond);
    }

    @Override
    public void init() {
        super.init();
        updateClipRadius(range + tilesize);
    }

    public class GasTractorBeamBuild extends GasBaseTurretBuild {

        @Nullable
        public Unit target;

        public float lastX, lastY, strength;

        public boolean any;

        public float coolantMultiplier = 1f;

        @Override
        public void updateTile() {
            float eff = efficiency * coolantMultiplier, edelta = eff * delta();
            // retarget
            if (timer(timerTarget, retargetTime)) {
                target = Units.closestEnemy(team, x, y, range, u -> u.checkTarget(targetAir, targetGround));
            }
            // consume coolant
            if (target != null && coolant != null) {
                float maxUsed = coolant.amount;
                Liquid liquid = liquids.current();
                float used = Math.min(Math.min(liquids.get(liquid), maxUsed * Time.delta), Math.max(0, (1f / coolantMultiplier) / liquid.heatCapacity));
                liquids.remove(liquid, used);
                if (Mathf.chance(0.06 * used)) {
                    coolEffect.at(x + Mathf.range(size * tilesize / 2f), y + Mathf.range(size * tilesize / 2f));
                }
                coolantMultiplier = 1f + (used * liquid.heatCapacity * coolantMultiplier);
            }
            any = false;
            // look at target
            if (target != null && target.within(this, range + target.hitSize / 2f) && target.team() != team && target.checkTarget(targetAir, targetGround) && efficiency > 0.02f) {
                if (!headless) {
                    control.sound.loop(shootSound, this, shootSoundVolume);
                }
                float dest = angleTo(target);
                rotation = Angles.moveToward(rotation, dest, rotateSpeed * edelta);
                lastX = target.x;
                lastY = target.y;
                strength = Mathf.lerpDelta(strength, 1f, 0.1f);
                // shoot when possible
                if (Angles.within(rotation, dest, shootCone)) {
                    if (damage > 0) {
                        target.damageContinuous(damage * eff);
                    }
                    if (status != StatusEffects.none) {
                        target.apply(status, statusDuration);
                    }
                    any = true;
                    target.impulseNet(Tmp.v1.set(this).sub(target).limit((force + (1f - target.dst(this) / range) * scaledForce) * edelta));
                }
            } else {
                strength = Mathf.lerpDelta(strength, 0, 0.1f);
            }
        }

        @Override
        public boolean shouldConsume() {
            return super.shouldConsume() && target != null;
        }

        @Override
        public void draw() {
            Draw.rect(baseRegion, x, y);
            Drawf.shadow(region, x - (size / 2f), y - (size / 2f), rotation - 90);
            Draw.rect(region, x, y, rotation - 90);
            // draw laser if applicable
            if (any) {
                Draw.z(Layer.bullet);
                float ang = angleTo(lastX, lastY);
                Draw.mixcol(laserColor, Mathf.absin(4f, 0.6f));
                Drawf.laser(laser, laserStart, laserEnd, x + Angles.trnsx(ang, shootLength), y + Angles.trnsy(ang, shootLength), lastX, lastY, strength * efficiency * laserWidth);
                Draw.mixcol();
            }
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(rotation);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            rotation = read.f();
        }
    }
}
