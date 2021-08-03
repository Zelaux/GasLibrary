package gas.world.draw;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import gas.annotations.GasAnnotations;
import gas.world.blocks.production.GasGenericCrafter;
import mindustry.Vars;
import mindustry.graphics.Pal;
import mindustry.world.Block;
import mindustry.world.blocks.production.GenericCrafter.GenericCrafterBuild;
import mindustry.world.draw.DrawBlock;

@GasAnnotations.GasAddition(analogue = "auto")
public class GasDrawWeave extends GasDrawBlock {
    public TextureRegion weave, bottom;

    @Override
    public void draw(GasGenericCrafter.GasGenericCrafterBuild build){
        Draw.rect(bottom, build.x, build.y);
        Draw.rect(weave, build.x, build.y, build.totalProgress);

        Draw.color(Pal.accent);
        Draw.alpha(build.warmup);

        Lines.lineAngleCenter(
        build.x + Mathf.sin(build.totalProgress, 6f, Vars.tilesize / 3f * build.block.size),
        build.y,
        90,
        build.block.size * Vars.tilesize / 2f);

        Draw.reset();

        Draw.rect(build.block.region, build.x, build.y);
    }

    @Override
    public void load(Block block){
        weave = Core.atlas.find(block.name + "-weave");
        bottom = Core.atlas.find(block.name + "-bottom");
    }

    @Override
    public TextureRegion[] icons(Block block){
        return new TextureRegion[]{bottom, weave, block.region};
    }
}
