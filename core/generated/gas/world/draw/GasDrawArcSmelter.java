package gas.world.draw;

import gas.entities.comp.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.experimental.*;
import arc.*;
import gas.world.meta.*;
import gas.world.blocks.units.*;
import mindustry.world.blocks.production.GenericCrafter.*;
import gas.world.blocks.defense.*;
import arc.util.*;
import mindustry.world.blocks.legacy.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import arc.math.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.meta.*;
import arc.graphics.*;
import gas.world.blocks.distribution.*;
import gas.world.draw.*;
import mindustry.world.blocks.logic.*;
import mindustry.gen.*;
import gas.world.blocks.power.*;
import mindustry.world.*;
import gas.world.blocks.sandbox.*;
import mindustry.world.blocks.storage.*;
import gas.world.blocks.liquid.*;
import gas.entities.*;
import mindustry.world.blocks.campaign.*;
import gas.gen.*;
import gas.world.*;
import gas.world.blocks.defense.turrets.*;
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
import mindustry.world.consumers.*;
import gas.world.modules.*;
import gas.world.blocks.*;
import gas.*;
import gas.io.*;
import gas.world.blocks.payloads.*;
import mindustry.world.blocks.units.*;
import gas.content.*;
import gas.world.blocks.storage.*;
import gas.world.blocks.production.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import gas.world.meta.values.*;
import mindustry.world.blocks.power.*;
import mindustry.world.draw.DrawArcSmelter.*;
import mindustry.world.blocks.sandbox.*;

public class GasDrawArcSmelter extends GasDrawBlock {

    public TextureRegion top, bottom;

    public Color flameColor = Color.valueOf("f58349"), midColor = Color.valueOf("f2d585");

    public float flameRad = 1f, circleSpace = 2f, flameRadiusScl = 3f, flameRadiusMag = 0.3f, circleStroke = 1.5f;

    public float alpha = 0.68f;

    public int particles = 25;

    public float particleLife = 40f, particleRad = 7f, particleStroke = 1.1f, particleLen = 3f;

    @Override
    public void draw(GasGenericCrafterBuild build) {
        Draw.rect(bottom, build.x, build.y);
        if (build.warmup > 0f && flameColor.a > 0.001f) {
            Lines.stroke(circleStroke * build.warmup);
            float si = Mathf.absin(flameRadiusScl, flameRadiusMag);
            float a = alpha * build.warmup;
            Draw.blend(Blending.additive);
            Draw.color(midColor, a);
            Fill.circle(build.x, build.y, flameRad + si);
            Draw.color(flameColor, a);
            Lines.circle(build.x, build.y, (flameRad + circleSpace + si) * build.warmup);
            Lines.stroke(particleStroke * build.warmup);
            float base = (Time.time / particleLife);
            rand.setSeed(build.id);
            for (int i = 0; i < particles; i++) {
                float fin = (rand.random(1f) + base) % 1f, fout = 1f - fin;
                float angle = rand.random(360f);
                float len = particleRad * Interp.pow2Out.apply(fin);
                Lines.lineAngle(build.x + Angles.trnsx(angle, len), build.y + Angles.trnsy(angle, len), angle, particleLen * fout * build.warmup);
            }
            Draw.blend();
            Draw.reset();
        }
        Draw.rect(top, build.x, build.y);
        Draw.rect(build.block.region, build.x, build.y);
    }

    @Override
    public void load(GasBlock block) {
        top = Core.atlas.find(block.name + "-top");
        bottom = Core.atlas.find(block.name + "-bottom");
    }

    @Override
    public TextureRegion[] icons(GasBlock block) {
        return new TextureRegion[] { bottom, block.region, top };
    }
}
