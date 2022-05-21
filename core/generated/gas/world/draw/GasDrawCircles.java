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
import mindustry.world.draw.DrawCircles.*;
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
import gas.world.blocks.production.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;

public class GasDrawCircles extends GasDrawBlock {

    public Color color = Color.valueOf("7457ce");

    public int amount = 5, sides = 15;

    public float strokeMin = 0.2f, strokeMax = 2f, timeScl = 160f;

    public float radius = 12f, radiusOffset = 0f, x = 0f, y = 0f;

    public Interp strokeInterp = Interp.pow3In;

    public GasDrawCircles(Color color) {
        this.color = color;
    }

    public GasDrawCircles() {
    }

    @Override
    public void draw(GasBuilding build) {
        if (build.warmup() <= 0.001f)
            return;
        Draw.color(color, build.warmup() * color.a);
        for (int i = 0; i < amount; i++) {
            float life = ((Time.time / timeScl + i / (float) amount) % 1f);
            Lines.stroke(build.warmup() * strokeInterp.apply(strokeMax, strokeMin, life));
            Lines.poly(build.x + x, build.y + y, sides, radiusOffset + life * radius);
        }
        Draw.reset();
    }
}
