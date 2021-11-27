package gas.world.blocks.units;

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
import arc.util.*;
import mindustry.world.blocks.legacy.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import arc.math.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.meta.*;
import arc.graphics.*;
import gas.world.blocks.distribution.*;
import gas.world.draw.*;
import mindustry.world.blocks.logic.*;
import mindustry.gen.*;
import gas.world.blocks.power.*;
import mindustry.world.*;
import gas.world.blocks.sandbox.*;
import mindustry.world.blocks.storage.*;
import gas.world.blocks.liquid.*;
import mindustry.game.*;
import gas.entities.*;
import mindustry.world.blocks.units.RepairPoint.*;
import mindustry.world.blocks.campaign.*;
import gas.gen.*;
import gas.world.*;
import gas.world.blocks.defense.turrets.*;
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
import mindustry.world.consumers.*;
import gas.world.modules.*;
import gas.world.blocks.*;
import gas.*;
import gas.io.*;
import gas.world.blocks.payloads.*;
import mindustry.world.blocks.units.*;
import gas.content.*;
import gas.world.blocks.storage.*;
import mindustry.graphics.*;
import gas.world.blocks.production.*;
import arc.struct.*;
import arc.util.io.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import gas.world.meta.values.*;
import mindustry.logic.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;
import static mindustry.Vars.*;

public class GasRepairPoint extends GasBlock {

    static final Rect rect = new Rect();

    static final Rand rand = new Rand();

    public int timerTarget = timers++;

    public int timerEffect = timers++;

    public float repairRadius = 50f;

    public float repairSpeed = 0.3f;

    public float powerUse;

    public float length = 5f;

    public float beamWidth = 1f;

    public float pulseRadius = 6f;

    public float pulseStroke = 2f;

    public boolean acceptCoolant = false;

    public float coolantUse = 0.5f;

    /**
     * Effect displayed when coolant is used.
     */
    public Effect coolEffect = Fx.fuelburn;

    /**
     * How much healing is increased by with heat capacity.
     */
    public float coolantMultiplier = 1f;

    @Load(value = "@-base", fallback = "block-@size")
    public TextureRegion baseRegion;

    @Load("laser-white")
    public TextureRegion laser;

    @Load("laser-white-end")
    public TextureRegion laserEnd;

    @Load("laser-top")
    public TextureRegion laserTop;

    @Load("laser-top-end")
    public TextureRegion laserTopEnd;

    public Color laserColor = Color.valueOf("98ffa9"), laserTopColor = Color.white.cpy();

    public GasRepairPoint(String name) {
        super(name);
        update = true;
        solid = true;
        flags = EnumSet.of(BlockFlag.repair);
        hasPower = true;
        outlineIcon = true;
        // yeah, this isn't the same thing, but it's close enough
        group = BlockGroup.projectors;
        envEnabled |= Env.space;
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.range, repairRadius / tilesize, StatUnit.blocks);
        stats.add(Stat.repairSpeed, repairSpeed * 60f, StatUnit.perSecond);
        if (acceptCoolant) {
            stats.add(Stat.booster, StatValues.strengthBoosters(coolantMultiplier, l -> consumes.liquidfilters.get(l.id)));
        }
    }

    @Override
    public void init() {
        if (acceptCoolant) {
            hasLiquids = true;
            consumes.add(new ConsumeCoolant(coolantUse)).optional(true, true);
        }
        consumes.powerCond(powerUse, (GasRepairPointBuild entity) -> entity.target != null);
        clipSize = Math.max(clipSize, (repairRadius + tilesize) * 2);
        super.init();
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, repairRadius, Pal.accent);
    }

    @Override
    public TextureRegion[] icons() {
        return new TextureRegion[] { baseRegion, region };
    }

    public static void drawBeam(float x, float y, float rotation, float length, int id, Sized target, Team team, float strength, float pulseStroke, float pulseRadius, float beamWidth, Vec2 lastEnd, Vec2 offset, Color laserColor, Color laserTopColor, TextureRegion laser, TextureRegion laserEnd, TextureRegion laserTop, TextureRegion laserTopEnd) {
        if (target != null) {
            float originX = x + Angles.trnsx(rotation, length), originY = y + Angles.trnsy(rotation, length);
            rand.setSeed(id + (target instanceof Entityc e ? e.id() : 0));
            lastEnd.set(target).sub(originX, originY);
            lastEnd.setLength(Math.max(2f, lastEnd.len()));
            lastEnd.add(offset.trns(rand.random(360f) + Time.time / 2f, Mathf.sin(Time.time + rand.random(200f), 55f, rand.random(target.hitSize() * 0.2f, target.hitSize() * 0.45f))).rotate(target instanceof Rotc rot ? rot.rotation() : 0f));
            lastEnd.add(originX, originY);
        }
        if (strength > 0.01f) {
            float originX = x + Angles.trnsx(rotation, length), originY = y + Angles.trnsy(rotation, length);
            // above all units
            Draw.z(Layer.flyingUnit + 1);
            Draw.color(laserColor);
            float f = (Time.time / 85f + rand.random(1f)) % 1f;
            Draw.alpha(1f - Interp.pow5In.apply(f));
            Lines.stroke(strength * pulseStroke);
            Lines.circle(lastEnd.x, lastEnd.y, 1f + f * pulseRadius);
            Draw.color(laserColor);
            Drawf.laser(team, laser, laserEnd, originX, originY, lastEnd.x, lastEnd.y, strength * beamWidth);
            Draw.z(Layer.flyingUnit + 1.1f);
            Draw.color(laserTopColor);
            Drawf.laser(team, laserTop, laserTopEnd, originX, originY, lastEnd.x, lastEnd.y, strength * beamWidth);
            Draw.color();
        }
    }

    public class GasRepairPointBuild extends GasBuilding implements Ranged {

        public Unit target;

        public Vec2 offset = new Vec2(), lastEnd = new Vec2();

        public float strength, rotation = 90;

        @Override
        public void draw() {
            Draw.rect(baseRegion, x, y);
            Draw.z(Layer.turret);
            Drawf.shadow(region, x - (size / 2f), y - (size / 2f), rotation - 90);
            Draw.rect(region, x, y, rotation - 90);
            drawBeam(x, y, rotation, length, id, target, team, strength, pulseStroke, pulseRadius, beamWidth, lastEnd, offset, laserColor, laserTopColor, laser, laserEnd, laserTop, laserTopEnd);
        }

        @Override
        public void drawSelect() {
            Drawf.dashCircle(x, y, repairRadius, Pal.accent);
        }

        @Override
        public void updateTile() {
            float multiplier = 1f;
            if (acceptCoolant) {
                var liq = consumes.<ConsumeLiquidBase>get(ConsumeType.liquid);
                multiplier = liq.valid(this) ? 1f + liquids.current().heatCapacity * coolantMultiplier : 1f;
            }
            if (target != null && (target.dead() || target.dst(tile) - target.hitSize / 2f > repairRadius || target.health() >= target.maxHealth())) {
                target = null;
            }
            if (target == null) {
                offset.setZero();
            }
            boolean healed = false;
            if (target != null && consValid()) {
                float angle = Angles.angle(x, y, target.x + offset.x, target.y + offset.y);
                if (Angles.angleDist(angle, rotation) < 30f) {
                    healed = true;
                    target.heal(repairSpeed * strength * edelta() * multiplier);
                }
                rotation = Mathf.slerpDelta(rotation, angle, 0.5f * efficiency() * timeScale);
            }
            strength = Mathf.lerpDelta(strength, healed ? 1f : 0f, 0.08f * Time.delta);
            if (timer(timerTarget, 20)) {
                rect.setSize(repairRadius * 2).setCenter(x, y);
                target = Units.closest(team, x, y, repairRadius, Unit::damaged);
            }
        }

        @Override
        public boolean shouldConsume() {
            return target != null && enabled;
        }

        @Override
        public BlockStatus status() {
            return Mathf.equal(efficiency(), 0f, 0.01f) ? BlockStatus.noInput : cons.status();
        }

        @Override
        public float range() {
            return repairRadius;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(rotation);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            if (revision >= 1) {
                rotation = read.f();
            }
        }

        @Override
        public byte version() {
            return 1;
        }
    }
}
