package gas.world.blocks.defense.turrets;

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
import mindustry.world.blocks.logic.*;
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
import mindustry.world.blocks.defense.turrets.PointDefenseTurret.*;
import arc.audio.*;
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
import gas.world.blocks.production.*;
import arc.util.io.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;

public class GasPointDefenseTurret extends GasReloadTurret {

    public final int timerTarget = timers++;

    public float retargetTime = 5f;

    @Load(value = "@-base", fallback = "block-@size")
    public TextureRegion baseRegion;

    public Color color = Color.white;

    public Effect beamEffect = Fx.pointBeam;

    public Effect hitEffect = Fx.pointHit;

    public Effect shootEffect = Fx.sparkShoot;

    public Sound shootSound = Sounds.lasershoot;

    public float shootCone = 5f;

    public float bulletDamage = 10f;

    public float shootLength = 3f;

    public GasPointDefenseTurret(String name) {
        super(name);
        rotateSpeed = 20f;
        reload = 30f;
        coolantMultiplier = 2f;
    }

    @Override
    public TextureRegion[] icons() {
        return new TextureRegion[] { baseRegion, region };
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.reload, 60f / reload, StatUnit.perSecond);
    }

    public class GasPointDefenseBuild extends GasReloadTurretBuild {

        @Nullable
        public Bullet target;

        @Override
        public void updateTile() {
            // retarget
            if (timer(timerTarget, retargetTime)) {
                target = Groups.bullet.intersect(x - range, y - range, range * 2, range * 2).min(b -> b.team != team && b.type().hittable, b -> b.dst2(this));
            }
            // pooled bullets
            if (target != null && !target.isAdded()) {
                target = null;
            }
            if (coolant != null) {
                updateCooling();
            }
            // look at target
            if (target != null && target.within(this, range) && target.team != team && target.type() != null && target.type().hittable) {
                float dest = angleTo(target);
                rotation = Angles.moveToward(rotation, dest, rotateSpeed * edelta());
                reloadCounter += edelta();
                // shoot when possible
                if (Angles.within(rotation, dest, shootCone) && reloadCounter >= reload) {
                    if (target.damage() > bulletDamage) {
                        target.damage(target.damage() - bulletDamage);
                    } else {
                        target.remove();
                    }
                    Tmp.v1.trns(rotation, shootLength);
                    beamEffect.at(x + Tmp.v1.x, y + Tmp.v1.y, rotation, color, new Vec2().set(target));
                    shootEffect.at(x + Tmp.v1.x, y + Tmp.v1.y, rotation, color);
                    hitEffect.at(target.x, target.y, color);
                    shootSound.at(x + Tmp.v1.x, y + Tmp.v1.y, Mathf.random(0.9f, 1.1f));
                    reloadCounter = 0;
                }
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
