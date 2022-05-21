package gas.world.draw;

import gas.entities.comp.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import gas.content.*;
import mindustry.world.blocks.defense.turrets.*;
import gas.world.blocks.payloads.*;
import arc.*;
import gas.world.meta.*;
import gas.world.blocks.units.*;
import mindustry.world.blocks.heat.*;
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
import mindustry.world.draw.DrawMultiWeave.*;
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

/**
 * Not standalone.
 */
public class GasDrawMultiWeave extends GasDrawBlock {

    public TextureRegion weave, glow;

    public float rotateSpeed = 1f, rotateSpeed2 = -0.9f;

    public Color glowColor = new Color(1f, 0.4f, 0.4f, 0.8f);

    public float pulse = 0.3f, pulseScl = 10f;

    @Override
    public void draw(GasBuilding build) {
        Draw.rect(weave, build.x, build.y, build.totalProgress() * rotateSpeed);
        Draw.rect(weave, build.x, build.y, build.totalProgress() * rotateSpeed * rotateSpeed2);
        Draw.blend(Blending.additive);
        Draw.color(glowColor, build.warmup() * (glowColor.a * (1f - pulse + Mathf.absin(pulseScl, pulse))));
        Draw.rect(glow, build.x, build.y, build.totalProgress() * rotateSpeed);
        Draw.rect(glow, build.x, build.y, build.totalProgress() * rotateSpeed * rotateSpeed2);
        Draw.blend();
        Draw.reset();
    }

    @Override
    public TextureRegion[] icons(GasBlock block) {
        return new TextureRegion[] { weave };
    }

    @Override
    public void load(GasBlock block) {
        weave = Core.atlas.find(block.name + "-weave");
        glow = Core.atlas.find(block.name + "-weave-glow");
    }
}
