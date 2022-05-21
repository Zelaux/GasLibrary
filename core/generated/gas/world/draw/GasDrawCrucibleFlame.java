package gas.world.draw;

import gas.world.blocks.logic.*;
import gas.entities.comp.*;
import gas.type.*;
import gas.world.blocks.campaign.*;
import mindustry.world.*;
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
import mindustry.world.draw.DrawCrucibleFlame.*;
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
import arc.math.Interp.*;
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

public class GasDrawCrucibleFlame extends GasDrawBlock {

    public Color flameColor = Color.valueOf("f58349"), midColor = Color.valueOf("f2d585");

    public float flameRad = 1f, circleSpace = 2f, flameRadiusScl = 10f, flameRadiusMag = 0.6f, circleStroke = 1.5f;

    public float alpha = 0.5f;

    public int particles = 30;

    public float particleLife = 70f, particleRad = 7f, particleSize = 3f, fadeMargin = 0.4f, rotateScl = 1.5f;

    public Interp particleInterp = new PowIn(1.5f);

    @Override
    public void draw(GasBuilding build) {
        if (build.warmup() > 0f && flameColor.a > 0.001f) {
            Lines.stroke(circleStroke * build.warmup());
            float si = Mathf.absin(flameRadiusScl, flameRadiusMag);
            float a = alpha * build.warmup();
            Draw.blend(Blending.additive);
            Draw.color(midColor, a);
            Fill.circle(build.x, build.y, flameRad + si);
            Draw.color(flameColor, a);
            Lines.circle(build.x, build.y, (flameRad + circleSpace + si) * build.warmup());
            float base = (Time.time / particleLife);
            rand.setSeed(build.id);
            for (int i = 0; i < particles; i++) {
                float fin = (rand.random(1f) + base) % 1f, fout = 1f - fin;
                float angle = rand.random(360f) + (Time.time / rotateScl) % 360f;
                float len = particleRad * particleInterp.apply(fout);
                Draw.alpha(a * (1f - Mathf.curve(fin, 1f - fadeMargin)));
                Fill.circle(build.x + Angles.trnsx(angle, len), build.y + Angles.trnsy(angle, len), particleSize * fin * build.warmup());
            }
            Draw.blend();
            Draw.reset();
        }
    }
}
