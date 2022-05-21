package gas.world.draw;

import gas.entities.comp.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import gas.content.*;
import mindustry.world.blocks.defense.turrets.*;
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
import mindustry.world.blocks.payloads.*;
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
import arc.graphics.g2d.*;
import mindustry.world.blocks.*;
import gas.world.blocks.production.GasGenericCrafter.*;
import arc.*;
import mindustry.world.blocks.logic.*;
import gas.world.modules.*;
import gas.world.blocks.*;
import arc.graphics.*;
import gas.*;
import mindustry.world.draw.DrawFlame.*;
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

// TODO remake/remove
public class GasDrawFlame extends GasDrawBlock {

    public Color flameColor = Color.valueOf("ffc999");

    public TextureRegion top;

    public float lightRadius = 60f, lightAlpha = 0.65f, lightSinScl = 10f, lightSinMag = 5;

    public float flameRadius = 3f, flameRadiusIn = 1.9f, flameRadiusScl = 5f, flameRadiusMag = 2f, flameRadiusInMag = 1f;

    public GasDrawFlame() {
    }

    public GasDrawFlame(Color flameColor) {
        this.flameColor = flameColor;
    }

    @Override
    public void load(GasBlock block) {
        top = Core.atlas.find(block.name + "-top");
        block.clipSize = Math.max(block.clipSize, (lightRadius + lightSinMag) * 2f * block.size);
    }

    @Override
    public void draw(GasBuilding build) {
        if (build.warmup() > 0f && flameColor.a > 0.001f) {
            float g = 0.3f;
            float r = 0.06f;
            float cr = Mathf.random(0.1f);
            Draw.z(Layer.block + 0.01f);
            Draw.alpha(build.warmup());
            Draw.rect(top, build.x, build.y);
            Draw.alpha(((1f - g) + Mathf.absin(Time.time, 8f, g) + Mathf.random(r) - r) * build.warmup());
            Draw.tint(flameColor);
            Fill.circle(build.x, build.y, flameRadius + Mathf.absin(Time.time, flameRadiusScl, flameRadiusMag) + cr);
            Draw.color(1f, 1f, 1f, build.warmup());
            Fill.circle(build.x, build.y, flameRadiusIn + Mathf.absin(Time.time, flameRadiusScl, flameRadiusInMag) + cr);
            Draw.color();
        }
    }

    @Override
    public void drawLight(GasBuilding build) {
        Drawf.light(build.x, build.y, (lightRadius + Mathf.absin(lightSinScl, lightSinMag)) * build.warmup() * build.block.size, flameColor, lightAlpha);
    }
}
