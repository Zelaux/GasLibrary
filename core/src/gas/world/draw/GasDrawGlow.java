package gas.world.draw;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import gas.annotations.GasAnnotations;
import gas.world.blocks.production.GasGenericCrafter;
import mindustry.world.Block;
import mindustry.world.blocks.production.GenericCrafter.GenericCrafterBuild;
import mindustry.world.draw.DrawBlock;

@GasAnnotations.GasAddition(analogue = "auto")
public class GasDrawGlow extends GasDrawBlock {
    public float glowAmount = 0.9f, glowScale = 3f;
    public TextureRegion top;

    @Override
    public void draw(GasGenericCrafter.GasGenericCrafterBuild build){
        Draw.rect(build.block.region, build.x, build.y);
        Draw.alpha(Mathf.absin(build.totalProgress, glowScale, glowAmount) * build.warmup);
        Draw.rect(top, build.x, build.y);
        Draw.reset();
    }

    @Override
    public void load(Block block){
        top = Core.atlas.find(block.name + "-top");
    }
}
