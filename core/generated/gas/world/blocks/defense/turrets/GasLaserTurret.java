package gas.world.blocks.defense.turrets;

import gas.entities.comp.*;
import gas.type.*;
import gas.world.blocks.logic.*;
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
import mindustry.entities.bullet.*;
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
import mindustry.world.blocks.defense.turrets.LaserTurret.*;
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
import gas.*;
import gas.io.*;
import gas.world.draw.*;
import gas.gen.*;
import mindustry.world.blocks.defense.turrets.Turret.*;
import gas.world.blocks.storage.*;
import mindustry.world.blocks.units.*;
import gas.world.blocks.production.*;
import arc.struct.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import mindustry.world.blocks.power.*;
import mindustry.type.*;
import mindustry.world.blocks.sandbox.*;
import static mindustry.Vars.*;

/**
 * A turret that fires a continuous beam with a delay between shots. Liquid coolant is required. Yes, this class name is awful. NEEDS RENAME
 */
public class GasLaserTurret extends GasPowerTurret {

    public float firingMoveFract = 0.25f;

    public float shootDuration = 100f;

    public GasLaserTurret(String name) {
        super(name);
        coolantMultiplier = 1f;
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.remove(Stat.booster);
        stats.add(Stat.input, StatValues.boosters(reload, coolant.amount, coolantMultiplier, false, this::consumesLiquid));
    }

    @Override
    public void init() {
        super.init();
        if (coolant == null) {
            coolant = findConsumer(c -> c instanceof ConsumeLiquidBase);
        }
    }

    public class GasLaserTurretBuild extends GasPowerTurretBuild {

        public Seq<BulletEntry> bullets = new Seq<>();

        @Override
        protected void updateCooling() {
            // do nothing, cooling is irrelevant here
        }

        @Override
        public boolean shouldConsume() {
            // still consumes power when bullet is around
            return bullets.any() || isActive();
        }

        @Override
        public void updateTile() {
            super.updateTile();
            bullets.removeAll(b -> !b.bullet.isAdded() || b.bullet.type == null || b.life <= 0f || b.bullet.owner != this);
            if (bullets.any()) {
                for (var entry : bullets) {
                    float bulletX = x + Angles.trnsx(rotation - 90, shootX + entry.x, shootY + entry.y), bulletY = y + Angles.trnsy(rotation - 90, shootX + entry.x, shootY + entry.y), angle = rotation + entry.rotation;
                    entry.bullet.rotation(angle);
                    entry.bullet.set(bulletX, bulletY);
                    entry.bullet.time = entry.bullet.type.lifetime * entry.bullet.type.optimalLifeFract;
                    entry.bullet.keepAlive = true;
                    entry.life -= Time.delta / Math.max(efficiency, 0.00001f);
                }
                wasShooting = true;
                heat = 1f;
                curRecoil = 1f;
            } else if (reloadCounter > 0) {
                wasShooting = true;
                if (coolant != null) {
                    // TODO does not handle multi liquid req?
                    Liquid liquid = liquids.current();
                    float maxUsed = coolant.amount;
                    float used = (cheating() ? maxUsed : Math.min(liquids.get(liquid), maxUsed)) * delta();
                    reloadCounter -= used * liquid.heatCapacity * coolantMultiplier;
                    liquids.remove(liquid, used);
                    if (Mathf.chance(0.06 * used)) {
                        coolEffect.at(x + Mathf.range(size * tilesize / 2f), y + Mathf.range(size * tilesize / 2f));
                    }
                } else {
                    reloadCounter -= edelta();
                }
            }
        }

        @Override
        public float progress() {
            return 1f - Mathf.clamp(reloadCounter / reload);
        }

        @Override
        protected void updateReload() {
            // updated in updateTile() depending on coolant
        }

        @Override
        protected void updateShooting() {
            if (bullets.any()) {
                return;
            }
            if (reloadCounter <= 0 && efficiency > 0 && !charging() && shootWarmup >= minWarmup) {
                BulletType type = peekAmmo();
                shoot(type);
                reloadCounter = reload;
            }
        }

        @Override
        protected void turnToTarget(float targetRot) {
            rotation = Angles.moveToward(rotation, targetRot, efficiency * rotateSpeed * delta() * (bullets.any() ? firingMoveFract : 1f));
        }

        @Override
        protected void handleBullet(@Nullable Bullet bullet, float offsetX, float offsetY, float angleOffset) {
            if (bullet != null) {
                bullets.add(new BulletEntry(bullet, offsetX, offsetY, angleOffset, shootDuration));
            }
        }

        @Override
        public boolean shouldActiveSound() {
            return bullets.any();
        }
    }
}
