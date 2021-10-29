package gas.world.blocks.defense.turrets;

import gas.entities.comp.*;
import mindustry.entities.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import mindustry.content.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.experimental.*;
import gas.world.meta.*;
import mindustry.annotations.Annotations.*;
import gas.world.blocks.units.*;
import gas.world.blocks.defense.*;
import arc.audio.*;
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
import gas.gen.*;
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
import gas.world.blocks.sandbox.*;
import mindustry.world.blocks.*;
import gas.world.blocks.production.GasGenericCrafter.*;
import arc.graphics.g2d.*;
import mindustry.world.blocks.defense.turrets.PointDefenseTurret.*;
import mindustry.world.blocks.logic.*;
import gas.world.modules.*;
import gas.world.blocks.*;
import gas.*;
import gas.io.*;
import gas.world.draw.*;
import mindustry.world.blocks.units.*;
import mindustry.world.blocks.defense.turrets.Turret.*;
import gas.content.*;
import gas.world.blocks.storage.*;
import mindustry.graphics.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import gas.world.meta.values.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;
import gas.world.blocks.power.*;

public class GasPointDefenseTurret extends GasReloadTurret {

    public final int timerTarget = timers++;

    public float retargetTime = 5f;

    @Load("block-@size")
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
        reloadTime = 30f;
        coolantMultiplier = 2f;
        // disabled due to version mismatch problems
        acceptCoolant = false;
    }

    @Override
    public TextureRegion[] icons() {
        return new TextureRegion[] { baseRegion, region };
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.reload, 60f / reloadTime, StatUnit.perSecond);
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
            if (acceptCoolant) {
                updateCooling();
            }
            // look at target
            if (target != null && target.within(this, range) && target.team != team && target.type() != null && target.type().hittable) {
                float dest = angleTo(target);
                rotation = Angles.moveToward(rotation, dest, rotateSpeed * edelta());
                reload += edelta();
                // shoot when possible
                if (Angles.within(rotation, dest, shootCone) && reload >= reloadTime) {
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
                    reload = 0;
                }
            }
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
