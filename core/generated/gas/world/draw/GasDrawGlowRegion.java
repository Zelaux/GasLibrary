package gas.world.draw;

import gas.entities.comp.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import gas.content.*;
import mindustry.world.blocks.defense.turrets.*;
import gas.world.blocks.payloads.*;
import mindustry.world.draw.DrawGlowRegion.*;
import gas.io.*;
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

/**
 * Not standalone.
 */
public class GasDrawGlowRegion extends GasDrawBlock {

    public Blending blending = Blending.additive;

    public String suffix = "-glow";

    public float alpha = 0.9f, glowScale = 10f, glowIntensity = 0.5f;

    public float rotateSpeed = 0f;

    public float layer = Layer.blockAdditive;

    public boolean rotate = false;

    public Color color = Color.red.cpy();

    public TextureRegion region;

    public GasDrawGlowRegion() {
    }

    public GasDrawGlowRegion(float layer) {
        this.layer = layer;
    }

    public GasDrawGlowRegion(boolean rotate) {
        this.rotate = rotate;
    }

    public GasDrawGlowRegion(String suffix) {
        this.suffix = suffix;
    }

    @Override
    public void draw(GasBuilding build) {
        if (build.warmup() <= 0.001f)
            return;
        float z = Draw.z();
        if (layer > 0)
            Draw.z(layer);
        Draw.blend(blending);
        Draw.color(color);
        Draw.alpha((Mathf.absin(build.totalProgress(), glowScale, alpha) * glowIntensity + 1f - glowIntensity) * build.warmup() * alpha);
        Draw.rect(region, build.x, build.y, build.totalProgress() * rotateSpeed + (rotate ? build.rotdeg() : 0f));
        Draw.reset();
        Draw.blend();
        Draw.z(z);
    }

    @Override
    public void load(GasBlock block) {
        region = Core.atlas.find(block.name + suffix);
    }
}
