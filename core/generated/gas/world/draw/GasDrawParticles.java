package gas.world.draw;

import arc.math.Interp.*;
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
import gas.world.draw.*;
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
import gas.entities.comp.*;
import gas.gen.*;
import gas.world.blocks.storage.*;
import mindustry.world.blocks.units.*;
import mindustry.world.draw.DrawParticles.*;
import gas.world.blocks.production.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;

/**
 * Not standalone.
 */
public class GasDrawParticles extends GasDrawBlock {

    public Color color = Color.valueOf("f2d585");

    public float alpha = 0.5f;

    public int particles = 30;

    public float particleLife = 70f, particleRad = 7f, particleSize = 3f, fadeMargin = 0.4f, rotateScl = 3f;

    public boolean reverse = false;

    public Interp particleInterp = new PowIn(1.5f);

    public Interp particleSizeInterp = Interp.slope;

    public Blending blending = Blending.normal;

    @Override
    public void draw(GasBuilding build) {
        if (build.warmup() > 0f) {
            float a = alpha * build.warmup();
            Draw.blend(blending);
            Draw.color(color);
            float base = (Time.time / particleLife);
            rand.setSeed(build.id);
            for (int i = 0; i < particles; i++) {
                float fin = (rand.random(2f) + base) % 1f;
                if (reverse)
                    fin = 1f - fin;
                float fout = 1f - fin;
                float angle = rand.random(360f) + (Time.time / rotateScl) % 360f;
                float len = particleRad * particleInterp.apply(fout);
                Draw.alpha(a * (1f - Mathf.curve(fin, 1f - fadeMargin)));
                Fill.circle(build.x + Angles.trnsx(angle, len), build.y + Angles.trnsy(angle, len), particleSize * particleSizeInterp.apply(fin) * build.warmup());
            }
            Draw.blend();
            Draw.reset();
        }
    }
}
