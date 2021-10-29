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
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import arc.math.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.meta.*;
import arc.graphics.*;
import gas.world.blocks.distribution.*;
import mindustry.gen.*;
import gas.world.blocks.payloads.*;
import mindustry.world.*;
import gas.world.blocks.production.*;
import mindustry.world.blocks.legacy.*;
import gas.world.blocks.liquid.*;
import mindustry.world.blocks.storage.*;
import gas.entities.*;
import mindustry.world.blocks.campaign.*;
import gas.world.blocks.defense.turrets.*;
import gas.gen.*;
import gas.world.*;
import mindustry.world.consumers.*;
import gas.world.blocks.gas.*;
import gas.world.blocks.campaign.*;
import mindustry.world.modules.*;
import gas.ui.*;
import mindustry.world.blocks.environment.*;
import gas.world.consumers.*;
import mindustry.world.blocks.payloads.*;
import gas.world.blocks.sandbox.*;
import mindustry.world.blocks.*;
import gas.world.blocks.production.GasGenericCrafter.*;
import arc.graphics.g2d.*;
import mindustry.world.blocks.logic.*;
import gas.world.modules.*;
import gas.world.blocks.*;
import gas.*;
import gas.io.*;
import gas.world.draw.*;
import mindustry.world.blocks.units.*;
import gas.content.*;
import gas.world.blocks.storage.*;
import mindustry.world.draw.DrawAnimation.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import gas.world.meta.values.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;
import gas.world.blocks.power.*;

public class GasDrawAnimation extends GasDrawBlock {

    public int frameCount = 3;

    public float frameSpeed = 5f;

    public boolean sine = true;

    public TextureRegion[] frames;

    public TextureRegion liquid, top;

    @Override
    public void draw(GasGenericCrafterBuild build) {
        Draw.rect(build.block.region, build.x, build.y);
        Draw.rect(sine ? frames[(int) Mathf.absin(build.totalProgress, frameSpeed, frameCount - 0.001f)] : frames[(int) ((build.totalProgress / frameSpeed) % frameCount)], build.x, build.y);
        if (build.liquids != null) {
            Draw.color(Color.clear, build.liquids.current().color, build.liquids.total() / build.block.liquidCapacity);
            Draw.rect(liquid, build.x, build.y);
            Draw.color();
        }
        if (top.found()) {
            Draw.rect(top, build.x, build.y);
        }
    }

    @Override
    public void load(GasBlock block) {
        frames = new TextureRegion[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = Core.atlas.find(block.name + "-frame" + i);
        }
        liquid = Core.atlas.find(block.name + "-liquid");
        top = Core.atlas.find(block.name + "-top");
    }

    @Override
    public TextureRegion[] icons(GasBlock block) {
        return top.found() ? new TextureRegion[] { block.region, top } : new TextureRegion[] { block.region };
    }
}
