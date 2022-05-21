package gas.world.blocks.defense;

import gas.entities.comp.*;
import mindustry.entities.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import mindustry.content.*;
import gas.content.*;
import mindustry.world.blocks.defense.turrets.*;
import gas.world.blocks.payloads.*;
import mindustry.world.blocks.defense.BaseShield.*;
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
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;
import static mindustry.Vars.*;

public class GasBaseShield extends GasBlock {

    // TODO game rule? or field? should vary by base.
    public float radius = 200f;

    public int sides = 24;

    protected static GasBaseShieldBuild paramBuild;

    // protected static Effect paramEffect;
    protected static final Cons<Bullet> bulletConsumer = bullet -> {
        if (bullet.team != paramBuild.team && bullet.type.absorbable && bullet.within(paramBuild, paramBuild.radius())) {
            bullet.absorb();
            // paramEffect.at(bullet);
            // TODO effect, shield health go down?
            // paramBuild.hit = 1f;
            // paramBuild.buildup += bullet.damage;
        }
    };

    protected static final Cons<Unit> unitConsumer = unit -> {
        // if this is positive, repel the unit; if it exceeds the unit radius * 2, it's inside the forcefield and must be killed
        float overlapDst = (unit.hitSize / 2f + paramBuild.radius()) - unit.dst(paramBuild);
        if (overlapDst > 0) {
            if (overlapDst > unit.hitSize * 1.5f) {
                // instakill units that are stuck inside the shield (TODO or maybe damage them instead?)
                unit.kill();
            } else {
                // stop
                unit.vel.setZero();
                // get out
                unit.move(Tmp.v1.set(unit).sub(paramBuild).setLength(overlapDst + 0.01f));
                if (Mathf.chanceDelta(0.12f * Time.delta)) {
                    Fx.circleColorSpark.at(unit.x, unit.y, paramBuild.team.color);
                }
            }
        }
    };

    public GasBaseShield(String name) {
        super(name);
        hasPower = true;
        update = solid = true;
        rebuildable = false;
    }

    @Override
    public void init() {
        super.init();
        updateClipRadius(radius);
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, radius, player.team().color);
    }

    public class GasBaseShieldBuild extends GasBuilding {

        // TODO
        public boolean broken = false;

        public float hit = 0f;

        public float smoothRadius;

        @Override
        public void updateTile() {
            smoothRadius = Mathf.lerpDelta(smoothRadius, radius * efficiency, 0.05f);
            float rad = radius();
            if (rad > 1) {
                paramBuild = this;
                // paramEffect = absorbEffect;
                Groups.bullet.intersect(x - rad, y - rad, rad * 2f, rad * 2f, bulletConsumer);
                Units.nearbyEnemies(team, x, y, rad + 10f, unitConsumer);
            }
        }

        public float radius() {
            return smoothRadius;
        }

        @Override
        public void drawSelect() {
            super.drawSelect();
            Drawf.dashCircle(x, y, radius, team.color);
        }

        @Override
        public void draw() {
            super.draw();
            drawShield();
        }

        // always visible due to their shield nature
        @Override
        public boolean inFogTo(Team viewer) {
            return false;
        }

        public void drawShield() {
            if (!broken) {
                float radius = radius();
                Draw.z(Layer.shields);
                Draw.color(team.color, Color.white, Mathf.clamp(hit));
                if (renderer.animateShields) {
                    Fill.poly(x, y, sides, radius);
                } else {
                    Lines.stroke(1.5f);
                    Draw.alpha(0.09f + Mathf.clamp(0.08f * hit));
                    Fill.poly(x, y, sides, radius);
                    Draw.alpha(1f);
                    Lines.poly(x, y, sides, radius);
                    Draw.reset();
                }
            }
            Draw.reset();
        }

        @Override
        public byte version() {
            return 1;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(smoothRadius);
            write.bool(broken);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read);
            if (revision >= 1) {
                smoothRadius = read.f();
                broken = read.bool();
            }
        }
    }
}
