package gas.world.blocks.defense;

import mindustry.entities.units.*;
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
import arc.Graphics.*;
import gas.world.blocks.defense.*;
import arc.audio.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import arc.math.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.meta.*;
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
import mindustry.world.blocks.logic.*;
import gas.world.modules.*;
import gas.world.blocks.*;
import gas.*;
import gas.io.*;
import gas.world.draw.*;
import mindustry.world.blocks.units.*;
import gas.content.*;
import mindustry.world.blocks.defense.Door.*;
import gas.world.blocks.storage.*;
import arc.util.*;
import arc.struct.*;
import arc.util.io.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import gas.world.meta.values.*;
import mindustry.logic.*;
import mindustry.world.blocks.power.*;
import arc.Graphics.Cursor.*;
import mindustry.world.blocks.sandbox.*;
import gas.world.blocks.power.*;
import static mindustry.Vars.*;

public class GasDoor extends GasWall {

    protected final static Rect rect = new Rect();

    protected final static Queue<GasDoorBuild> doorQueue = new Queue<>();

    public final int timerToggle = timers++;

    public Effect openfx = Fx.dooropen;

    public Effect closefx = Fx.doorclose;

    public Sound doorSound = Sounds.door;

    @Load("@-open")
    public TextureRegion openRegion;

    public GasDoor(String name) {
        super(name);
        solid = false;
        solidifes = true;
        consumesTap = true;
        config(Boolean.class, (GasDoorBuild base, Boolean open) -> {
            doorSound.at(base);
            for (var entity : base.chained) {
                // skip doors with things in them
                if ((Units.anyEntities(entity.tile) && !open) || entity.open == open) {
                    continue;
                }
                entity.open = open;
                pathfinder.updateTile(entity.tile());
                entity.effect();
            }
        });
    }

    @Override
    public TextureRegion getRequestRegion(BuildPlan req, Eachable<BuildPlan> list) {
        return req.config == Boolean.TRUE ? openRegion : region;
    }

    public class GasDoorBuild extends GasBuilding {

        public boolean open = false;

        public Seq<GasDoorBuild> chained = new Seq<>();

        @Override
        public void onProximityAdded() {
            super.onProximityAdded();
            updateChained();
        }

        @Override
        public void onProximityRemoved() {
            super.onProximityRemoved();
            for (var b : proximity) {
                if (b instanceof GasDoorBuild d) {
                    d.updateChained();
                }
            }
        }

        @Override
        public double sense(LAccess sensor) {
            if (sensor == LAccess.enabled)
                return open ? 1 : 0;
            return super.sense(sensor);
        }

        @Override
        public void control(LAccess type, double p1, double p2, double p3, double p4) {
            if (type == LAccess.enabled) {
                boolean shouldOpen = !Mathf.zero(p1);
                if (net.client() || open == shouldOpen || (Units.anyEntities(tile) && !shouldOpen) || !origin().timer(timerToggle, 80f)) {
                    return;
                }
                configureAny(shouldOpen);
            }
        }

        public GasDoorBuild origin() {
            return chained.isEmpty() ? this : chained.first();
        }

        public void effect() {
            (open ? closefx : openfx).at(this);
        }

        public void updateChained() {
            chained = new Seq<>();
            doorQueue.clear();
            doorQueue.add(this);
            while (!doorQueue.isEmpty()) {
                var next = doorQueue.removeLast();
                chained.add(next);
                for (var b : next.proximity) {
                    if (b instanceof GasDoorBuild d && d.chained != chained) {
                        d.chained = chained;
                        doorQueue.addFirst(d);
                    }
                }
            }
        }

        @Override
        public void draw() {
            Draw.rect(open ? openRegion : region, x, y);
        }

        @Override
        public Cursor getCursor() {
            return interactable(player.team()) ? SystemCursor.hand : SystemCursor.arrow;
        }

        @Override
        public boolean checkSolid() {
            return !open;
        }

        @Override
        public void tapped() {
            if ((Units.anyEntities(tile) && open) || !origin().timer(timerToggle, 60f)) {
                return;
            }
            configure(!open);
        }

        @Override
        public Boolean config() {
            return open;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.bool(open);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            open = read.bool();
        }
    }
}
