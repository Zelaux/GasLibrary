package gas.world.blocks.defense.turrets;

import gas.entities.comp.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.experimental.*;
import gas.world.meta.*;
import gas.world.blocks.units.*;
import mindustry.world.blocks.defense.turrets.LaserTurret.*;
import gas.world.blocks.defense.*;
import arc.util.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import arc.math.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.meta.*;
import gas.world.blocks.distribution.*;
import mindustry.entities.bullet.*;
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
import gas.world.blocks.campaign.*;
import mindustry.world.modules.*;
import gas.ui.*;
import mindustry.world.blocks.environment.*;
import gas.world.consumers.*;
import mindustry.world.blocks.payloads.*;
import gas.world.blocks.sandbox.*;
import mindustry.world.blocks.*;
import gas.world.blocks.production.GasGenericCrafter.*;
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
import gas.entities.bullets.*;
import mindustry.world.blocks.defense.*;
import gas.world.meta.values.*;
import mindustry.logic.*;
import mindustry.world.blocks.power.*;
import mindustry.type.*;
import mindustry.world.blocks.sandbox.*;
import gas.world.blocks.power.*;
import static mindustry.Vars.*;

public class GasLaserTurret extends GasPowerTurret {

    public float firingMoveFract = 0.25f;

    public float shootDuration = 100f;

    public GasLaserTurret(String name) {
        super(name);
        canOverdrive = false;
        consumes.add(new ConsumeCoolant(0.01f)).update(false);
        coolantMultiplier = 1f;
    }

    @Override
    public void init() {
        consumes.powerCond(powerUse, entity -> ((GasLaserTurretBuild) entity).bullet != null || ((GasLaserTurretBuild) entity).target != null);
        super.init();
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.remove(Stat.booster);
        stats.add(Stat.input, StatValues.boosters(reloadTime, consumes.<ConsumeLiquidBase>get(ConsumeType.liquid).amount, coolantMultiplier, false, l -> consumes.liquidfilters.get(l.id)));
    }

    public class GasLaserTurretBuild extends GasPowerTurretBuild {

        public Bullet bullet;

        public float bulletLife;

        @Override
        protected void updateCooling() {
            // do nothing, cooling is irrelevant here
        }

        @Override
        public void updateTile() {
            super.updateTile();
            if (bulletLife > 0 && bullet != null) {
                wasShooting = true;
                tr.trns(rotation, shootLength, 0f);
                bullet.rotation(rotation);
                bullet.set(x + tr.x, y + tr.y);
                bullet.time(0f);
                heat = 1f;
                recoil = recoilAmount;
                bulletLife -= Time.delta / Math.max(efficiency(), 0.00001f);
                if (bulletLife <= 0f) {
                    bullet = null;
                }
            } else if (reload > 0) {
                wasShooting = true;
                Liquid liquid = liquids.current();
                float maxUsed = consumes.<ConsumeLiquidBase>get(ConsumeType.liquid).amount;
                float used = (cheating() ? maxUsed : Math.min(liquids.get(liquid), maxUsed)) * Time.delta;
                reload -= used * liquid.heatCapacity * coolantMultiplier;
                liquids.remove(liquid, used);
                if (Mathf.chance(0.06 * used)) {
                    coolEffect.at(x + Mathf.range(size * tilesize / 2f), y + Mathf.range(size * tilesize / 2f));
                }
            }
        }

        @Override
        public double sense(LAccess sensor) {
            // reload reversed for laser turrets
            if (sensor == LAccess.progress)
                return Mathf.clamp(1f - reload / reloadTime);
            return super.sense(sensor);
        }

        @Override
        protected void updateShooting() {
            if (bulletLife > 0 && bullet != null) {
                return;
            }
            if (reload <= 0 && (consValid() || cheating()) && !charging) {
                BulletType type = peekAmmo();
                shoot(type);
                reload = reloadTime;
            }
        }

        @Override
        protected void turnToTarget(float targetRot) {
            rotation = Angles.moveToward(rotation, targetRot, efficiency() * rotateSpeed * delta() * (bulletLife > 0f ? firingMoveFract : 1f));
        }

        @Override
        protected void bullet(BulletType type, float angle) {
            bullet = type.create(tile.build, team, x + tr.x, y + tr.y, angle);
            bulletLife = shootDuration;
        }

        @Override
        public boolean shouldActiveSound() {
            return bulletLife > 0 && bullet != null;
        }
    }
}
