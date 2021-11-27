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
import mindustry.world.blocks.legacy.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import arc.math.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.meta.*;
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
import mindustry.*;
import gas.world.consumers.*;
import mindustry.world.blocks.payloads.*;
import mindustry.world.draw.DrawWeave.*;
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
import mindustry.graphics.*;
import gas.world.blocks.production.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import gas.world.meta.values.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;

public class GasDrawWeave extends GasDrawBlock {

    public TextureRegion weave, bottom;

    @Override
    public void draw(GasGenericCrafterBuild build) {
        Draw.rect(bottom, build.x, build.y);
        Draw.rect(weave, build.x, build.y, build.totalProgress);
        Draw.color(Pal.accent);
        Draw.alpha(build.warmup);
        Lines.lineAngleCenter(build.x + Mathf.sin(build.totalProgress, 6f, Vars.tilesize / 3f * build.block.size), build.y, 90, build.block.size * Vars.tilesize / 2f);
        Draw.reset();
        Draw.rect(build.block.region, build.x, build.y);
    }

    @Override
    public void load(GasBlock block) {
        weave = Core.atlas.find(block.name + "-weave");
        bottom = Core.atlas.find(block.name + "-bottom");
    }

    @Override
    public TextureRegion[] icons(GasBlock block) {
        return new TextureRegion[] { bottom, weave, block.region };
    }
}
