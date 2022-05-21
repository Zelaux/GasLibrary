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
import mindustry.world.blocks.liquid.*;
import mindustry.world.meta.*;
import gas.world.blocks.heat.*;
import gas.world.blocks.defense.*;
import mindustry.world.draw.DrawBubbles.*;
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

public class GasDrawBubbles extends GasDrawBlock {

    public Color color = Color.valueOf("7457ce");

    public int amount = 12, sides = 8;

    public float strokeMin = 0.2f, spread = 3f, timeScl = 30f;

    public float recurrence = 6f, radius = 3f;

    public boolean fill = false;

    public GasDrawBubbles(Color color) {
        this.color = color;
    }

    public GasDrawBubbles() {
    }

    @Override
    public void draw(GasBuilding build) {
        if (build.warmup() <= 0.001f)
            return;
        Draw.color(color);
        Draw.alpha(build.warmup());
        rand.setSeed(build.id);
        for (int i = 0; i < amount; i++) {
            float x = rand.range(spread), y = rand.range(spread);
            float life = 1f - ((Time.time / timeScl + rand.random(recurrence)) % recurrence);
            if (life > 0) {
                float rad = (1f - life) * radius;
                if (fill) {
                    Fill.circle(build.x + x, build.y + y, rad);
                } else {
                    Lines.stroke(build.warmup() * (life + strokeMin));
                    Lines.poly(build.x + x, build.y + y, sides, rad);
                }
            }
        }
        Draw.color();
    }
}
