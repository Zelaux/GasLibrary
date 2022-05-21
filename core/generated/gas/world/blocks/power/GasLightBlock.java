package gas.world.blocks.power;

import gas.entities.comp.*;
import arc.scene.ui.layout.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import gas.content.*;
import mindustry.world.blocks.defense.turrets.*;
import gas.world.blocks.payloads.*;
import gas.world.meta.*;
import mindustry.annotations.Annotations.*;
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
import arc.graphics.*;
import gas.*;
import gas.io.*;
import gas.world.draw.*;
import gas.gen.*;
import gas.world.blocks.storage.*;
import mindustry.world.blocks.units.*;
import mindustry.input.*;
import mindustry.world.blocks.power.LightBlock.*;
import mindustry.graphics.*;
import gas.world.blocks.production.*;
import arc.struct.*;
import arc.util.io.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import mindustry.logic.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;
import static mindustry.Vars.*;

public class GasLightBlock extends GasBlock {

    public float brightness = 0.9f;

    public float radius = 200f;

    @Load("@-top")
    public TextureRegion topRegion;

    public GasLightBlock(String name) {
        super(name);
        hasPower = true;
        update = true;
        configurable = true;
        saveConfig = true;
        envEnabled |= Env.space;
        swapDiagonalPlacement = true;
        config(Integer.class, (GasLightBuild tile, Integer value) -> tile.color = value);
    }

    @Override
    public void init() {
        // double needed for some reason
        lightRadius = radius * 2f;
        emitLight = true;
        super.init();
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, radius * 0.75f, Pal.placing);
    }

    @Override
    public void changePlacementPath(Seq<Point2> points, int rotation) {
        var placeRadius2 = Mathf.pow(radius * 0.7f / tilesize, 2f) * 3;
        Placement.calculateNodes(points, this, rotation, (point, other) -> point.dst2(other) <= placeRadius2);
    }

    public class GasLightBuild extends GasBuilding {

        public int color = Pal.accent.rgba();

        public float smoothTime = 1f;

        @Override
        public void control(LAccess type, double p1, double p2, double p3, double p4) {
            if (type == LAccess.color) {
                color = Tmp.c1.fromDouble(p1).rgba8888();
            }
            super.control(type, p1, p2, p3, p4);
        }

        @Override
        public void draw() {
            super.draw();
            Draw.blend(Blending.additive);
            Draw.color(Tmp.c1.set(color), efficiency * 0.3f);
            Draw.rect(topRegion, x, y);
            Draw.color();
            Draw.blend();
        }

        @Override
        public void updateTile() {
            smoothTime = Mathf.lerpDelta(smoothTime, timeScale, 0.1f);
        }

        @Override
        public void buildConfiguration(Table table) {
            table.button(Icon.pencil, () -> {
                ui.picker.show(Tmp.c1.set(color).a(0.5f), false, res -> configure(res.rgba()));
                deselect();
            }).size(40f);
        }

        @Override
        public void drawLight() {
            Drawf.light(x, y, lightRadius * Math.min(smoothTime, 2f), Tmp.c1.set(color), brightness * efficiency);
        }

        @Override
        public Integer config() {
            return color;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.i(color);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            color = read.i();
        }
    }
}
