package gas.world.draw;

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
import mindustry.world.blocks.distribution.*;
import gas.world.blocks.power.*;
import mindustry.world.*;
import gas.world.blocks.sandbox.*;
import mindustry.world.blocks.storage.*;
import gas.world.blocks.liquid.*;
import gas.entities.*;
import mindustry.world.draw.DrawPulseShape.*;
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
import mindustry.graphics.*;
import gas.world.blocks.production.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;
import static mindustry.Vars.*;

public class GasDrawPulseShape extends GasDrawBlock {

    public Color color = Pal.accent.cpy();

    public float stroke = 2f, timeScl = 100f, minStroke = 0.2f;

    public float radiusScl = 1f;

    public float layer = -1f;

    public boolean square = true;

    public GasDrawPulseShape(boolean square) {
        this.square = square;
    }

    public GasDrawPulseShape() {
    }

    @Override
    public void draw(GasBuilding build) {
        float pz = Draw.z();
        if (layer > 0)
            Draw.z(layer);
        float f = 1f - (Time.time / timeScl) % 1f;
        float rad = build.block.size * tilesize / 2f * radiusScl;
        Draw.color(color);
        Lines.stroke((stroke * f + minStroke) * build.warmup());
        if (square) {
            Lines.square(build.x, build.y, Math.min(1f + (1f - f) * rad, rad));
        } else {
            float r = Math.max(0f, Mathf.clamp(2f - f * 2f) * rad - f - 0.2f), w = Mathf.clamp(0.5f - f) * rad * 2f;
            Lines.beginLine();
            for (int i = 0; i < 4; i++) {
                Lines.linePoint(build.x + Geometry.d4(i).x * r + Geometry.d4(i).y * w, build.y + Geometry.d4(i).y * r - Geometry.d4(i).x * w);
                if (f < 0.5f)
                    Lines.linePoint(build.x + Geometry.d4(i).x * r - Geometry.d4(i).y * w, build.y + Geometry.d4(i).y * r + Geometry.d4(i).x * w);
            }
            Lines.endLine(true);
        }
        Draw.reset();
        Draw.z(pz);
    }
}
