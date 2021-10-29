package gas.world.blocks.defense.turrets;

import gas.entities.comp.*;
import mindustry.entities.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import mindustry.content.*;
import mindustry.type.*;
import mindustry.world.blocks.experimental.*;
import mindustry.core.*;
import gas.io.*;
import gas.world.meta.*;
import mindustry.annotations.Annotations.*;
import gas.world.blocks.units.*;
import gas.world.blocks.defense.*;
import arc.audio.*;
import mindustry.world.blocks.legacy.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import arc.math.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.meta.*;
import arc.graphics.*;
import gas.world.blocks.distribution.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import gas.world.blocks.payloads.*;
import mindustry.world.*;
import gas.world.blocks.production.*;
import mindustry.game.EventType.*;
import gas.world.blocks.liquid.*;
import mindustry.world.blocks.storage.*;
import mindustry.game.*;
import gas.entities.*;
import mindustry.world.blocks.campaign.*;
import gas.world.blocks.defense.turrets.*;
import gas.gen.*;
import gas.world.*;
import mindustry.world.consumers.*;
import mindustry.world.blocks.defense.turrets.*;
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
import arc.*;
import mindustry.world.blocks.logic.*;
import gas.world.modules.*;
import gas.world.blocks.*;
import gas.*;
import arc.graphics.g2d.*;
import gas.world.draw.*;
import arc.func.*;
import mindustry.world.blocks.units.*;
import mindustry.world.blocks.defense.turrets.Turret.*;
import gas.content.*;
import mindustry.entities.Units.*;
import gas.world.blocks.storage.*;
import mindustry.graphics.*;
import arc.util.*;
import arc.struct.*;
import arc.util.io.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import gas.world.meta.values.*;
import mindustry.logic.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;
import gas.world.blocks.power.*;
import static mindustry.Vars.*;

public class GasTurret extends GasReloadTurret {

    // after being logic-controlled and this amount of time passes, the turret will resume normal AI
    public final static float logicControlCooldown = 60 * 2;

    public final int timerTarget = timers++;

    public int targetInterval = 20;

    public Color heatColor = Pal.turretHeat;

    public Effect shootEffect = Fx.none;

    public Effect smokeEffect = Fx.none;

    public Effect ammoUseEffect = Fx.none;

    public Sound shootSound = Sounds.shoot;

    // general info
    public int maxAmmo = 30;

    public int ammoPerShot = 1;

    public float ammoEjectBack = 1f;

    public float inaccuracy = 0f;

    public float velocityInaccuracy = 0f;

    public int shots = 1;

    public float spread = 4f;

    public float recoilAmount = 1f;

    public float restitution = 0.02f;

    public float cooldown = 0.02f;

    public float coolantUsage = 0.2f;

    public float shootCone = 8f;

    public float shootShake = 0f;

    public float shootLength = -1;

    public float xRand = 0f;

    /**
     * Currently used for artillery only.
     */
    public float minRange = 0f;

    public float burstSpacing = 0;

    public boolean alternate = false;

    /**
     * If true, this turret will accurately target moving targets with respect to charge time.
     */
    public boolean accurateDelay = false;

    public boolean targetAir = true;

    public boolean targetGround = true;

    public boolean targetHealing = false;

    public boolean playerControllable = true;

    // charging
    public float chargeTime = -1f;

    public int chargeEffects = 5;

    public float chargeMaxDelay = 10f;

    public Effect chargeEffect = Fx.none;

    public Effect chargeBeginEffect = Fx.none;

    public Sound chargeSound = Sounds.none;

    public Sortf unitSort = Unit::dst2;

    protected Vec2 tr = new Vec2();

    protected Vec2 tr2 = new Vec2();

    @Load(value = "@-base", fallback = "block-@size")
    public TextureRegion baseRegion;

    @Load("@-heat")
    public TextureRegion heatRegion;

    public float elevation = -1f;

    public Cons<GasTurretBuild> drawer = tile -> Draw.rect(region, tile.x + tr2.x, tile.y + tr2.y, tile.rotation - 90);

    public Cons<GasTurretBuild> heatDrawer = tile -> {
        if (tile.heat <= 0.00001f)
            return;
        Draw.color(heatColor, tile.heat);
        Draw.blend(Blending.additive);
        Draw.rect(heatRegion, tile.x + tr2.x, tile.y + tr2.y, tile.rotation - 90);
        Draw.blend();
        Draw.color();
    };

    public GasTurret(String name) {
        super(name);
        liquidCapacity = 20f;
    }

    @Override
    public boolean outputsItems() {
        return false;
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.inaccuracy, (int) inaccuracy, StatUnit.degrees);
        stats.add(Stat.reload, 60f / (reloadTime) * (alternate ? 1 : shots), StatUnit.perSecond);
        stats.add(Stat.targetsAir, targetAir);
        stats.add(Stat.targetsGround, targetGround);
        if (ammoPerShot != 1)
            stats.add(Stat.ammoUse, ammoPerShot, StatUnit.perShot);
    }

    @Override
    public void init() {
        if (acceptCoolant && !consumes.has(ConsumeType.liquid)) {
            hasLiquids = true;
            consumes.add(new ConsumeCoolant(coolantUsage)).update(false).boost();
        }
        if (shootLength < 0)
            shootLength = size * tilesize / 2f;
        if (elevation < 0)
            elevation = size / 2f;
        super.init();
    }

    @Override
    public TextureRegion[] icons() {
        return new TextureRegion[] { baseRegion, region };
    }

    public class GasTurretBuild extends GasReloadTurretBuild implements ControlBlock {

        public Seq<AmmoEntry> ammo = new Seq<>();

        public int totalAmmo;

        public float recoil, heat, logicControlTime = -1;

        public int shotCounter;

        public boolean logicShooting = false;

        @Nullable
        public Posc target;

        public Vec2 targetPos = new Vec2();

        @Nullable
        public BlockUnitc unit;

        public boolean wasShooting, charging;

        @Override
        public void created() {
            unit = (BlockUnitc) UnitTypes.block.create(team);
            unit.tile(this);
        }

        @Override
        public boolean canControl() {
            return playerControllable;
        }

        @Override
        public void control(LAccess type, double p1, double p2, double p3, double p4) {
            if (type == LAccess.shoot && (unit == null || !unit.isPlayer())) {
                targetPos.set(World.unconv((float) p1), World.unconv((float) p2));
                logicControlTime = logicControlCooldown;
                logicShooting = !Mathf.zero(p3);
            }
            super.control(type, p1, p2, p3, p4);
        }

        @Override
        public void control(LAccess type, Object p1, double p2, double p3, double p4) {
            if (type == LAccess.shootp && (unit == null || !unit.isPlayer())) {
                logicControlTime = logicControlCooldown;
                logicShooting = !Mathf.zero(p2);
                if (p1 instanceof Posc) {
                    targetPosition((Posc) p1);
                }
            }
            super.control(type, p1, p2, p3, p4);
        }

        @Override
        public double sense(LAccess sensor) {
            switch(sensor) {
                case ammo:
                    return totalAmmo;
                case ammoCapacity:
                    return maxAmmo;
                case rotation:
                    return rotation;
                case shootX:
                    return World.conv(targetPos.x);
                case shootY:
                    return World.conv(targetPos.y);
                case shooting:
                    return isShooting() ? 1 : 0;
                case progress:
                    return Mathf.clamp(reload / reloadTime);
                default:
                    return super.sense(sensor);
            }
        }

        public boolean isShooting() {
            return (isControlled() ? (unit != null && unit.isShooting()) : logicControlled() ? logicShooting : target != null);
        }

        @Override
        public Unit unit() {
            if (unit == null) {
                unit = (BlockUnitc) UnitTypes.block.create(team);
                unit.tile(this);
            }
            return (Unit) unit;
        }

        public boolean logicControlled() {
            return logicControlTime > 0;
        }

        public boolean isActive() {
            return (target != null || wasShooting) && enabled;
        }

        public void targetPosition(Posc pos) {
            if (!hasAmmo() || pos == null)
                return;
            BulletType bullet = peekAmmo();
            var offset = Tmp.v1.setZero();
            // when delay is accurate, assume unit has moved by chargeTime already
            if (accurateDelay && pos instanceof Hitboxc h) {
                offset.set(h.deltaX(), h.deltaY()).scl(chargeTime / Time.delta);
            }
            targetPos.set(Predict.intercept(this, pos, offset.x, offset.y, bullet.speed <= 0.01f ? 99999999f : bullet.speed));
            if (targetPos.isZero()) {
                targetPos.set(pos);
            }
        }

        @Override
        public void draw() {
            Draw.rect(baseRegion, x, y);
            Draw.color();
            Draw.z(Layer.turret);
            tr2.trns(rotation, -recoil);
            Drawf.shadow(region, x + tr2.x - elevation, y + tr2.y - elevation, rotation - 90);
            drawer.get(this);
            if (heatRegion != Core.atlas.find("error")) {
                heatDrawer.get(this);
            }
        }

        @Override
        public void updateTile() {
            if (!validateTarget())
                target = null;
            wasShooting = false;
            recoil = Mathf.lerpDelta(recoil, 0f, restitution);
            heat = Mathf.lerpDelta(heat, 0f, cooldown);
            if (unit != null) {
                unit.health(health);
                unit.rotation(rotation);
                unit.team(team);
                unit.set(x, y);
            }
            if (logicControlTime > 0) {
                logicControlTime -= Time.delta;
            }
            if (hasAmmo()) {
                if (Float.isNaN(reload))
                    rotation = 0;
                if (timer(timerTarget, targetInterval)) {
                    findTarget();
                }
                if (validateTarget()) {
                    boolean canShoot = true;
                    if (isControlled()) {
                        // player behavior
                        targetPos.set(unit.aimX(), unit.aimY());
                        canShoot = unit.isShooting();
                    } else if (logicControlled()) {
                        // logic behavior
                        canShoot = logicShooting;
                    } else {
                        // default AI behavior
                        targetPosition(target);
                        if (Float.isNaN(rotation))
                            rotation = 0;
                    }
                    float targetRot = angleTo(targetPos);
                    if (shouldTurn()) {
                        turnToTarget(targetRot);
                    }
                    if (Angles.angleDist(rotation, targetRot) < shootCone && canShoot) {
                        wasShooting = true;
                        updateShooting();
                    }
                }
            }
            if (acceptCoolant) {
                updateCooling();
            }
        }

        @Override
        public void handleLiquid(Building source, Liquid liquid, float amount) {
            if (acceptCoolant && liquids.currentAmount() <= 0.001f) {
                Events.fire(Trigger.turretCool);
            }
            super.handleLiquid(source, liquid, amount);
        }

        protected boolean validateTarget() {
            return !Units.invalidateTarget(target, canHeal() ? Team.derelict : team, x, y) || isControlled() || logicControlled();
        }

        protected boolean canHeal() {
            return targetHealing && hasAmmo() && peekAmmo().collidesTeam && peekAmmo().healPercent > 0;
        }

        protected void findTarget() {
            if (targetAir && !targetGround) {
                target = Units.bestEnemy(team, x, y, range, e -> !e.dead() && !e.isGrounded(), unitSort);
            } else {
                target = Units.bestTarget(team, x, y, range, e -> !e.dead() && (e.isGrounded() || targetAir) && (!e.isGrounded() || targetGround), b -> true, unitSort);
                if (target == null && canHeal()) {
                    target = Units.findAllyTile(team, x, y, range, b -> b.damaged() && b != this);
                }
            }
        }

        protected void turnToTarget(float targetRot) {
            rotation = Angles.moveToward(rotation, targetRot, rotateSpeed * delta() * baseReloadSpeed());
        }

        public boolean shouldTurn() {
            return !charging;
        }

        /**
         * Consume ammo and return a type.
         */
        public BulletType useAmmo() {
            if (cheating())
                return peekAmmo();
            AmmoEntry entry = ammo.peek();
            entry.amount -= ammoPerShot;
            if (entry.amount <= 0)
                ammo.pop();
            totalAmmo -= ammoPerShot;
            totalAmmo = Math.max(totalAmmo, 0);
            ejectEffects();
            return entry.type();
        }

        /**
         * @return the ammo type that will be returned if useAmmo is called.
         */
        public BulletType peekAmmo() {
            return ammo.peek().type();
        }

        /**
         * @return  whether the turret has ammo.
         */
        public boolean hasAmmo() {
            // skip first entry if it has less than the required amount of ammo
            if (ammo.size >= 2 && ammo.peek().amount < ammoPerShot) {
                ammo.pop();
            }
            return ammo.size > 0 && ammo.peek().amount >= ammoPerShot;
        }

        protected void updateShooting() {
            reload += delta() * peekAmmo().reloadMultiplier * baseReloadSpeed();
            if (reload >= reloadTime && !charging) {
                BulletType type = peekAmmo();
                shoot(type);
                reload %= reloadTime;
            }
        }

        protected void shoot(BulletType type) {
            // when charging is enabled, use the charge shoot pattern
            if (chargeTime > 0) {
                useAmmo();
                tr.trns(rotation, shootLength);
                chargeBeginEffect.at(x + tr.x, y + tr.y, rotation);
                chargeSound.at(x + tr.x, y + tr.y, 1);
                for (int i = 0; i < chargeEffects; i++) {
                    Time.run(Mathf.random(chargeMaxDelay), () -> {
                        if (!isValid())
                            return;
                        tr.trns(rotation, shootLength);
                        chargeEffect.at(x + tr.x, y + tr.y, rotation);
                    });
                }
                charging = true;
                Time.run(chargeTime, () -> {
                    if (!isValid())
                        return;
                    tr.trns(rotation, shootLength);
                    recoil = recoilAmount;
                    heat = 1f;
                    bullet(type, rotation + Mathf.range(inaccuracy + type.inaccuracy));
                    effects();
                    charging = false;
                });
                // when burst spacing is enabled, use the burst pattern
            } else if (burstSpacing > 0.0001f) {
                for (int i = 0; i < shots; i++) {
                    int ii = i;
                    Time.run(burstSpacing * i, () -> {
                        if (!isValid() || !hasAmmo())
                            return;
                        recoil = recoilAmount;
                        tr.trns(rotation, shootLength, Mathf.range(xRand));
                        bullet(type, rotation + Mathf.range(inaccuracy + type.inaccuracy) + (ii - (int) (shots / 2f)) * spread);
                        effects();
                        useAmmo();
                        recoil = recoilAmount;
                        heat = 1f;
                    });
                }
            } else {
                // otherwise, use the normal shot pattern(s)
                if (alternate) {
                    float i = (shotCounter % shots) - (shots - 1) / 2f;
                    tr.trns(rotation - 90, spread * i + Mathf.range(xRand), shootLength);
                    bullet(type, rotation + Mathf.range(inaccuracy + type.inaccuracy));
                } else {
                    tr.trns(rotation, shootLength, Mathf.range(xRand));
                    for (int i = 0; i < shots; i++) {
                        bullet(type, rotation + Mathf.range(inaccuracy + type.inaccuracy) + (i - (int) (shots / 2f)) * spread);
                    }
                }
                shotCounter++;
                recoil = recoilAmount;
                heat = 1f;
                effects();
                useAmmo();
            }
        }

        protected void bullet(BulletType type, float angle) {
            float lifeScl = type.scaleVelocity ? Mathf.clamp(Mathf.dst(x + tr.x, y + tr.y, targetPos.x, targetPos.y) / type.range(), minRange / type.range(), range / type.range()) : 1f;
            type.create(this, team, x + tr.x, y + tr.y, angle, 1f + Mathf.range(velocityInaccuracy), lifeScl);
        }

        protected void effects() {
            Effect fshootEffect = shootEffect == Fx.none ? peekAmmo().shootEffect : shootEffect;
            Effect fsmokeEffect = smokeEffect == Fx.none ? peekAmmo().smokeEffect : smokeEffect;
            fshootEffect.at(x + tr.x, y + tr.y, rotation);
            fsmokeEffect.at(x + tr.x, y + tr.y, rotation);
            shootSound.at(x + tr.x, y + tr.y, Mathf.random(0.9f, 1.1f));
            if (shootShake > 0) {
                Effect.shake(shootShake, shootShake, this);
            }
            recoil = recoilAmount;
        }

        protected void ejectEffects() {
            if (!isValid())
                return;
            // alternate sides when using a double turret
            float scl = (shots == 2 && alternate && shotCounter % 2 == 1 ? -1f : 1f);
            ammoUseEffect.at(x - Angles.trnsx(rotation, ammoEjectBack), y - Angles.trnsy(rotation, ammoEjectBack), rotation * scl);
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(reload);
            write.f(rotation);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            if (revision >= 1) {
                reload = read.f();
                rotation = read.f();
            }
        }

        @Override
        public byte version() {
            return 1;
        }
    }
}
