package gas.world.blocks.defense;

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
import mindustry.world.blocks.defense.AutoDoor.*;
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
import mindustry.world.blocks.logic.*;
import gas.world.modules.*;
import gas.world.blocks.*;
import gas.*;
import gas.io.*;
import gas.world.draw.*;
import arc.func.*;
import gas.gen.*;
import gas.world.blocks.storage.*;
import mindustry.world.blocks.units.*;
import gas.world.blocks.production.*;
import arc.struct.*;
import arc.util.io.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import mindustry.logic.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;
import static mindustry.Vars.*;

public class GasAutoDoor extends GasWall {

    protected final static Rect rect = new Rect();

    protected final static Seq<Unit> units = new Seq<>();

    protected final static Boolf<Unit> groundCheck = u -> u.isGrounded() && !u.type.allowLegStep;

    public final int timerToggle = timers++;

    public float checkInterval = 20f;

    public Effect openfx = Fx.dooropen;

    public Effect closefx = Fx.doorclose;

    public Sound doorSound = Sounds.door;

    @Load("@-open")
    public TextureRegion openRegion;

    public float triggerMargin = 10f;

    public GasAutoDoor(String name) {
        super(name);
        solid = false;
        solidifes = true;
        update = true;
        teamPassable = true;
        noUpdateDisabled = true;
        drawDisabled = true;
    }

    @Remote(called = Loc.server)
    public static void autoDoorToggle(Tile tile, boolean open) {
        if (tile == null || !(tile.build instanceof GasAutoDoorBuild build))
            return;
        build.setOpen(open);
    }

    public class GasAutoDoorBuild extends GasBuilding {

        public boolean open = false;

        public GasAutoDoorBuild() {
            // make sure it is staggered
            timer.reset(timerToggle, Mathf.random(checkInterval));
        }

        @Override
        public void updateTile() {
            if (timer(timerToggle, checkInterval) && !net.client()) {
                units.clear();
                team.data().tree().intersect(rect.setSize(size * tilesize + triggerMargin * 2f).setCenter(x, y), units);
                boolean shouldOpen = units.contains(groundCheck);
                if (open != shouldOpen) {
                    Call.autoDoorToggle(tile, shouldOpen);
                }
            }
        }

        @Override
        public double sense(LAccess sensor) {
            if (sensor == LAccess.enabled)
                return open ? 1 : 0;
            return super.sense(sensor);
        }

        public void setOpen(boolean open) {
            this.open = open;
            pathfinder.updateTile(tile);
            if (wasVisible) {
                (!open ? closefx : openfx).at(this, size);
                doorSound.at(this);
            }
        }

        @Override
        public void draw() {
            Draw.rect(open ? openRegion : region, x, y);
        }

        @Override
        public boolean checkSolid() {
            return !open;
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
