package gas.world.draw;

import arc.math.Rand;
import gas.annotations.GasAnnotations;
import gas.world.blocks.production.GasGenericCrafter;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import mindustry.world.Block;
import mindustry.world.blocks.production.GenericCrafter;

@GasAnnotations.GasAddition(analogue = "auto")
public class GasDrawBlock {
    protected static final Rand rand = new Rand();

    /** Draws the block. */
    public void draw(GasGenericCrafter.GasGenericCrafterBuild build){
        Draw.rect(build.block.region, build.x, build.y, build.block.rotate ? build.rotdeg() : 0);
    }

    /** Draws any extra light for the block. */
    public void drawLight(GasGenericCrafter.GasGenericCrafterBuild  build){

    }

    /** Load any relevant texture regions. */
    public void load(Block block){

    }

    /** @return the generated icons to be used for this block. */
    public TextureRegion[] icons(Block block){
        return new TextureRegion[]{block.region};
    }
}
