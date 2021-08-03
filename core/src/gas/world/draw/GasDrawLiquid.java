package gas.world.draw;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import gas.annotations.GasAnnotations;
import gas.world.blocks.production.GasGenericCrafter;
import mindustry.graphics.Drawf;
import mindustry.world.Block;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.blocks.production.GenericCrafter.GenericCrafterBuild;
import mindustry.world.draw.DrawBlock;

@GasAnnotations.GasAddition(analogue = "auto")
public class GasDrawLiquid extends GasDrawBlock {
    public TextureRegion liquid, top;

    @Override
    public void draw(GasGenericCrafter.GasGenericCrafterBuild build){
        Draw.rect(build.block.region, build.x, build.y);
        GasGenericCrafter type = (GasGenericCrafter)build.block;

        if(type.outputLiquid != null && build.liquids.get(type.outputLiquid.liquid) > 0){
            Drawf.liquid(liquid, build.x, build.y,
                build.liquids.get(type.outputLiquid.liquid) / type.liquidCapacity,
                type.outputLiquid.liquid.color
            );
        }

        if(top.found()) Draw.rect(top, build.x, build.y);
    }

    @Override
    public void load(Block block){
        top = Core.atlas.find(block.name + "-top");
        liquid = Core.atlas.find(block.name + "-liquid");
    }

    @Override
    public TextureRegion[] icons(Block block){
        return top.found() ? new TextureRegion[]{block.region, top} : new TextureRegion[]{block.region};
    }
}
