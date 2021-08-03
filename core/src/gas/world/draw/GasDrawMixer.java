package gas.world.draw;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import gas.annotations.GasAnnotations;
import gas.world.blocks.production.GasGenericCrafter;
import mindustry.world.Block;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.blocks.production.GenericCrafter.GenericCrafterBuild;
import mindustry.world.draw.DrawBlock;

@GasAnnotations.GasAddition(analogue = "auto")
public class GasDrawMixer extends GasDrawBlock {
    public TextureRegion liquid, top, bottom;

    @Override
    public void draw(GasGenericCrafter.GasGenericCrafterBuild build){
        float rotation = build.block.rotate ? build.rotdeg() : 0;

        Draw.rect(bottom, build.x, build.y, rotation);

        if(build.liquids.total() > 0.001f){
            Draw.color(((GasGenericCrafter)build.block).outputLiquid.liquid.color);
            Draw.alpha(build.liquids.get(((GasGenericCrafter)build.block).outputLiquid.liquid) / build.block.liquidCapacity);
            Draw.rect(liquid, build.x, build.y, rotation);
            Draw.color();
        }

        Draw.rect(top, build.x, build.y, rotation);
    }

    @Override
    public void load(Block block){
        liquid = Core.atlas.find(block.name + "-liquid");
        top = Core.atlas.find(block.name + "-top");
        bottom = Core.atlas.find(block.name + "-bottom");
    }

    @Override
    public TextureRegion[] icons(Block block){
        return new TextureRegion[]{bottom, top};
    }
}
