package gas.world.draw;

import gas.entities.comp.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import gas.content.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.draw.DrawCells.*;
import gas.world.blocks.payloads.*;
import gas.io.*;
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
import arc.*;
import mindustry.world.blocks.logic.*;
import gas.world.modules.*;
import gas.world.blocks.*;
import arc.graphics.*;
import gas.*;
import arc.graphics.g2d.*;
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

public class GasDrawCells extends GasDrawBlock {

    public TextureRegion middle;

    public Color color = Color.white.cpy(), particleColorFrom = Color.black.cpy(), particleColorTo = Color.black.cpy();

    public int particles = 12;

    public float range = 4f, recurrence = 6f, radius = 3f, lifetime = 60f;

    @Override
    public void draw(GasBuilding build) {
        Drawf.liquid(middle, build.x, build.y, build.warmup(), color);
        if (build.warmup() > 0.001f) {
            rand.setSeed(build.id);
            for (int i = 0; i < particles; i++) {
                float offset = rand.nextFloat() * 999999f;
                float x = rand.range(range), y = rand.range(range);
                float fin = 1f - (((Time.time + offset) / lifetime) % recurrence);
                float ca = rand.random(0.1f, 1f);
                float fslope = Mathf.slope(fin);
                if (fin > 0) {
                    Draw.color(particleColorFrom, particleColorTo, ca);
                    Draw.alpha(build.warmup());
                    Fill.circle(build.x + x, build.y + y, fslope * radius);
                }
            }
        }
        Draw.color();
        Draw.rect(build.block.region, build.x, build.y);
    }

    @Override
    public void load(GasBlock block) {
        middle = Core.atlas.find(block.name + "-middle");
    }
}
