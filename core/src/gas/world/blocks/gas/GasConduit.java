package gas.world.blocks.gas;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import gas.gen.*;
import gas.type.*;
import gas.world.*;
import mindustry.*;
import mindustry.annotations.Annotations.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.input.*;
import mindustry.world.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.distribution.*;

import static mindustry.Vars.tilesize;

public class GasConduit extends GasGasBlock implements Autotiler {

    public final int timerFlow = timers++;

    public Color botColor = Color.valueOf("565656");

    @Load(value = "@-top-#", length = 5)
    public TextureRegion[] topRegions;

    @Load(value = "@-bottom-#", length = 5, fallback = "conduit-bottom-#")
    public TextureRegion[] botRegions;

    @Load("@-cap")
    public TextureRegion capRegion;

    public boolean leaks = true;

    @Nullable
    public Block junctionReplacement, bridgeReplacement;

    public GasConduit(String name) {
        super(name);
        rotate = true;
        solid = false;
        floating = true;
        conveyorPlacement = true;
        noUpdateDisabled = true;
        canOverdrive = false;
    }

    @Override
    public void init() {
        super.init();
        if (junctionReplacement == null)
            junctionReplacement = Vars.content.blocks().find(b -> b instanceof GasJunction);
        if (bridgeReplacement == null || !(bridgeReplacement instanceof ItemBridge))
            bridgeReplacement = Vars.content.blocks().find(b -> b instanceof GasBridge);
    }

    @Override
    public void drawPlanRegion(BuildPlan req, Eachable<BuildPlan> list) {
        var bits = getTiling(req, list);
        if (bits == null)
            return;
        Draw.scl(bits[1], bits[2]);
        Draw.color(botColor);
        Draw.alpha(0.5f);
        Draw.rect(botRegions[bits[0]], req.drawx(), req.drawy(), req.rotation * 90);
        Draw.color();
        Draw.rect(topRegions[bits[0]], req.drawx(), req.drawy(), req.rotation * 90);
        Draw.scl();
    }

    @Override
    public Block getReplacement(BuildPlan req, Seq<BuildPlan> requests) {
        if (junctionReplacement == null)
            return this;
        Boolf<Point2> cont = p -> requests.contains(o -> o.x == req.x + p.x && o.y == req.y + p.y && o.rotation == req.rotation && (req.block instanceof GasConduit || req.block instanceof GasJunction));
        return cont.get(Geometry.d4(req.rotation)) && cont.get(Geometry.d4(req.rotation - 2)) && req.tile() != null && req.tile().block() instanceof GasConduit && Mathf.mod(req.build().rotation - req.rotation, 2) == 1 ? junctionReplacement : this;
    }

    @Override
    public boolean blends(Tile tile, int rotation, int otherx, int othery, int otherrot, Block otherblock_) {
        if(!(otherblock_ instanceof GasBlock otherblock))return false;
        return otherblock.hasGasses && (otherblock.outputsGas || (lookingAt(tile, rotation, otherx, othery, otherblock))) && lookingAtEither(tile, rotation, otherx, othery, otherrot, otherblock);
    }

    @Override
    public void setBars(){
        super.setBars();
    }

    @Override
    public void handlePlacementLine(Seq<BuildPlan> plans) {
        if (bridgeReplacement == null)
            return;
        GasPlacement.calculateBridges(plans, (GasBridge) bridgeReplacement);
    }

    @Override
    public TextureRegion[] icons() {
        return new TextureRegion[] { Core.atlas.find("conduit-bottom"), topRegions[0] };
    }

    public class GasConduitBuild extends GasGasBlock.GasBuild implements ChainedBuilding {

        public int blendbits, xscl, yscl, blending;

        public boolean capped;

        @Override
        public void draw() {
            float rotation = rotdeg();
            int r = this.rotation;
            // draw extra conduits facing this one for tiling purposes
            Draw.z(Layer.blockUnder);
            for (int i = 0; i < 4; i++) {
                if ((blending & (1 << i)) != 0) {
                    int dir = r - i;
                    float rot = i == 0 ? rotation : (dir) * 90;
                    drawAt(x + Geometry.d4x(dir) * tilesize * 0.75f, y + Geometry.d4y(dir) * tilesize * 0.75f, 0, rot, i != 0 ? SliceMode.bottom : SliceMode.top);
                }
            }
            Draw.z(Layer.block);
            Draw.scl(xscl, yscl);
            drawAt(x, y, blendbits, rotation, SliceMode.none);
            Draw.reset();
            if (capped && capRegion.found())
                Draw.rect(capRegion, x, y, rotdeg());
        }

        protected void drawAt(float x, float y, int bits, float rotation, SliceMode slice) {
            Draw.color(botColor);
            Draw.rect(sliced(botRegions[bits], slice), x, y, rotation);
            Drawf.liquid(sliced(botRegions[bits], slice), x, y, smoothGas, gasses.current().color, rotation);
            Draw.rect(sliced(topRegions[bits], slice), x, y, rotation);
        }

        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
            var bits = buildBlending(tile, rotation, null, true);
            blendbits = bits[0];
            xscl = bits[1];
            yscl = bits[2];
            blending = bits[4];
            Building next = front();
            capped = next == null || next.team != team || !(next.block instanceof GasBlock gasBuild && gasBuild.hasGasses);
        }

        @Override
        public boolean acceptGas(Building source, Gas gas) {
            noSleep();
            return (gasses.current() == gas || gasses.currentAmount() < 0.2f) && (tile == null||source==null || (source.relativeTo(tile.x, tile.y) + 2) % 4 != rotation);
        }

        @Override
        public void updateTile() {
            smoothGas = Mathf.lerpDelta(smoothGas, gasses.currentAmount() / gasCapacity, 0.05f);
            if (gasses.currentAmount() > 0.001f && timer(timerFlow, 1)) {
                moveGasForward(leaks, gasses.current());
                noSleep();
            } else {
                sleep();
            }
        }

        @Nullable
        @Override
        public Building next() {
            var next = tile.nearby(rotation);
            if (next != null && next.build instanceof GasConduitBuild) {
                return next.build;
            }
            return null;
        }
    }
}
