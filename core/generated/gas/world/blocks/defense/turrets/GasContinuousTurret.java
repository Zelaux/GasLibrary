package gas.world.blocks.defense.turrets;

import gas.entities.comp.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import mindustry.content.*;
import gas.content.*;
import mindustry.world.blocks.defense.turrets.*;
import gas.world.blocks.payloads.*;
import gas.world.meta.*;
import gas.world.blocks.units.*;
import mindustry.world.blocks.heat.*;
import arc.util.*;
import mindustry.world.blocks.legacy.*;
import mindustry.gen.*;
import mindustry.world.blocks.defense.turrets.ContinuousTurret.*;
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
import mindustry.world.blocks.sandbox.*;

/**
 * A turret that fires a continuous beam bullet with no reload or coolant necessary. The bullet only disappears when the turret stops shooting.
 */
public class GasContinuousTurret extends GasTurret {

    public BulletType shootType = Bullets.placeholder;

    public GasContinuousTurret(String name) {
        super(name);
        coolantMultiplier = 1f;
        envEnabled |= Env.space;
        displayAmmoMultiplier = false;
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.ammo, StatValues.ammo(ObjectMap.of(this, shootType)));
        stats.remove(Stat.reload);
        stats.remove(Stat.inaccuracy);
    }

    // TODO LaserTurret shared code
    public class GasContinuousTurretBuild extends GasTurretBuild {

        public Seq<BulletEntry> bullets = new Seq<>();

        @Override
        protected void updateCooling() {
            // TODO how does coolant work here, if at all?
        }

        @Override
        public BulletType useAmmo() {
            // nothing used directly
            return shootType;
        }

        @Override
        public boolean hasAmmo() {
            // TODO update ammo in unit so it corresponds to liquids
            return canConsume();
        }

        @Override
        public boolean shouldConsume() {
            return isShooting();
        }

        @Override
        public BulletType peekAmmo() {
            return shootType;
        }

        @Override
        public void updateTile() {
            super.updateTile();
            // TODO unclean way of calculating ammo fraction to display
            float ammoFract = efficiency;
            if (findConsumer(f -> f instanceof ConsumeLiquidBase) instanceof ConsumeLiquid cons) {
                ammoFract = Math.min(ammoFract, liquids.get(cons.liquid) / liquidCapacity);
            }
            unit.ammo(unit.type().ammoCapacity * ammoFract);
            bullets.removeAll(b -> !b.bullet.isAdded() || b.bullet.type == null || b.bullet.owner != this);
            if (bullets.any()) {
                for (var entry : bullets) {
                    float bulletX = x + Angles.trnsx(rotation - 90, shootX + entry.x, shootY + entry.y), bulletY = y + Angles.trnsy(rotation - 90, shootX + entry.x, shootY + entry.y), angle = rotation + entry.rotation;
                    entry.bullet.rotation(angle);
                    entry.bullet.set(bulletX, bulletY);
                    if (isShooting() && hasAmmo()) {
                        entry.bullet.time = entry.bullet.lifetime * entry.bullet.type.optimalLifeFract * shootWarmup;
                        entry.bullet.keepAlive = true;
                    }
                }
                wasShooting = true;
                heat = 1f;
                curRecoil = recoil;
            }
        }

        @Override
        protected void updateReload() {
            // continuous turrets don't have a concept of reload, they are always firing when possible
        }

        @Override
        protected void updateShooting() {
            if (bullets.any()) {
                return;
            }
            if (canConsume() && !charging() && shootWarmup >= minWarmup) {
                shoot(peekAmmo());
            }
        }

        @Override
        protected void turnToTarget(float targetRot) {
            rotation = Angles.moveToward(rotation, targetRot, efficiency * rotateSpeed * delta());
        }

        @Override
        protected void handleBullet(@Nullable Bullet bullet, float offsetX, float offsetY, float angleOffset) {
            if (bullet != null) {
                bullets.add(new BulletEntry(bullet, offsetX, offsetY, angleOffset, 0f));
            }
        }

        @Override
        public boolean shouldActiveSound() {
            return bullets.any();
        }
    }
}
