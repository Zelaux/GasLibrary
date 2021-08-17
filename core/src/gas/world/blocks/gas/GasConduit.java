package gas.world.blocks.gas;

import gas.type.Gas;
import gas.world.GasBlock;
import gas.annotations.GasAnnotations;
import arc.func.Boolf;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.struct.Seq;
import arc.util.Eachable;
import arc.util.Nullable;
import mindustry.*;
import mindustry.annotations.Annotations.*;
import mindustry.content.*;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.Autotiler;
import mindustry.world.blocks.distribution.*;

@GasAnnotations.GasAddition()
public class GasConduit extends GasGasBlock implements Autotiler {
    public final int timerFlow = timers++;

    public Color botColor = Color.valueOf("565656");

    public @Load(value = "@-top-#", length = 5) TextureRegion[] topRegions;
    public @Load(value = "@-bottom-#", length = 5, fallback = "conduit-bottom-#") TextureRegion[] botRegions;
    public @Load("@-cap") TextureRegion capRegion;

    public boolean leaks = true;
    public @Nullable Block junctionReplacement, bridgeReplacement;

    public GasConduit(String name){
        super(name);
        rotate = true;
        solid = false;
        floating = true;
        conveyorPlacement = true;
        noUpdateDisabled = true;
    }

    @Override
    public void init() {
        super.init();

        if(junctionReplacement == null) junctionReplacement = Vars.content.blocks().find(block -> block instanceof GasJunction);
        if(bridgeReplacement == null || !(bridgeReplacement instanceof ItemBridge)) bridgeReplacement = Blocks.bridgeConduit;
    }


    public void drawRequestRegion(BuildPlan req, Eachable<BuildPlan> list) {
        int[] bits = getTiling(req, list);

        if(bits == null) return;

        Draw.scl(bits[1], bits[2]);
        Draw.color(botColor);
        Draw.alpha(0.5f);
        Draw.rect(botRegions[bits[0]], req.drawx(), req.drawy(), req.rotation * 90);
        Draw.color();
        Draw.rect(topRegions[bits[0]], req.drawx(), req.drawy(), req.rotation * 90);
        Draw.scl();
    }

    public Block getReplacement(BuildPlan req, Seq<BuildPlan> requests) {
        if(junctionReplacement == null) return this;

        Boolf<Point2> cont = p -> requests.contains(o -> o.x == req.x + p.x && o.y == req.y + p.y && o.rotation == req.rotation && (req.block instanceof Conduit || req.block instanceof LiquidJunction));
        return cont.get(Geometry.d4(req.rotation)) &&
        cont.get(Geometry.d4(req.rotation - 2)) &&
        req.tile() != null &&
        req.tile().block() instanceof GasConduit &&
        Mathf.mod(req.build().rotation - req.rotation, 2) == 1 ? Blocks.liquidJunction : this;
        Mathf.mod(req.build().rotation - req.rotation, 2) == 1 ? junctionReplacement : this;
//        return (Block)(cont.get(Geometry.d4(req.rotation)) && cont.get(Geometry.d4(req.rotation - 2)) && req.tile() != null &&
//                req.tile().block() instanceof Conduit && Mathf.mod(req.build().rotation - req.rotation, 2) == 1 ? Blocks.liquidJunction : this);
    }
    @Override
    public TextureRegion[] icons(){
        return new TextureRegion[]{bottomRegion, topRegions[0]};
    }
    public boolean blends(Tile tile, int rotation, int otherx, int othery, int otherrot, Block o) {
        if (!(o instanceof GasBlock)) return false;
        GasBlock otherblock = (GasBlock) o;
        return otherblock.hasGas &&
                (otherblock.outputsGas || this.lookingAt(tile, rotation, otherx, othery, otherblock)) &&
                this.lookingAtEither(tile, rotation, otherx, othery, otherrot, otherblock);
    }

    public class GasConduitBuild extends GasBuild  implements ChainedBuilding {
        public float smoothGas;
        public int blendbits;
        public int xscl;
        public int yscl;
        public int blending;

        public void draw() {
            float rotation = this.rotdeg();
            int r = this.rotation;
            Draw.z(29.5F);

            for (int i = 0; i < 4; ++i) {
                if ((this.blending & 1 << i) != 0) {
                    int dir = r - i;
                    float rot = i == 0 ? rotation : (float) (dir * 90);
                    this.drawAt(this.x + (float) (Geometry.d4x(dir) * 8) * 0.75F, this.y + (float) (Geometry.d4y(dir) * 8) * 0.75F, 0, rot, i != 0 ? SliceMode.bottom : SliceMode.top);
                }
            }

            Draw.z(30.0F);
            Draw.scl((float) this.xscl, (float) this.yscl);
            this.drawAt(this.x, this.y, this.blendbits, rotation, SliceMode.none);
            Draw.reset();
        }

        protected void drawAt(float x, float y, int bits, float rotation, SliceMode slice) {
            if (GasConduit.this.drawBottom) {
                Draw.color(GasConduit.this.botColor);
                Draw.rect(GasConduit.this.sliced(GasConduit.this.botRegions[bits], slice), x, y, rotation);
                Drawf.liquid(GasConduit.this.sliced(GasConduit.this.botRegions[bits], slice), x, y, this.smoothGas, this.gasses.current().color, rotation);
            }
            Draw.rect(GasConduit.this.sliced(GasConduit.this.topRegions[bits], slice), x, y, rotation);
        }

        public void onProximityUpdate() {
            super.onProximityUpdate();
            int[] bits = GasConduit.this.buildBlending(this.tile, this.rotation, (BuildPlan[]) null, true);
            this.blendbits = bits[0];
            this.xscl = bits[1];
            this.yscl = bits[2];
            this.blending = bits[4];
        }

        public boolean acceptGas(Building source, Gas gas) {
            this.noSleep();
            return (this.gasses.current() == gas || this.gasses.currentAmount() < 0.2F) && (source.relativeTo(this.tile.x, this.tile.y) + 2) % 4 != this.rotation;
        }

        public void updateTile() {
            this.smoothGas = Mathf.lerpDelta(this.smoothGas, this.gasses.currentAmount() / GasConduit.this.gasCapacity, 0.05F);
            if (this.gasses.total() > 0.001F && this.timer(GasConduit.this.timerFlow, 1.0F)) {
                this.moveGasForward(GasConduit.this.leaks, this.gasses.current());
                this.noSleep();
            } else {
                this.sleep();
            }

        }

        @Nullable
        @Override
        public Building next(){
            Tile next = tile.nearby(rotation);
            if(next != null && next.build instanceof GasConduitBuild){
                return next.build;
            }
            return null;
        }
    }
}
