package gas.world.draw;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import gas.annotations.GasAnnotations;
import gas.world.blocks.production.GasGenericCrafter;
import mindustry.graphics.Drawf;
import mindustry.world.Block;
import mindustry.world.blocks.production.GenericCrafter.GenericCrafterBuild;
import mindustry.world.draw.DrawBlock;

@GasAnnotations.GasAddition(analogue = "auto")
public class GasDrawRotator extends GasDrawBlock {
    public TextureRegion rotator, top;
    public boolean drawSpinSprite = false;

    @Override
    public void draw(GasGenericCrafter.GasGenericCrafterBuild build){
        Draw.rect(build.block.region, build.x, build.y);
        if(drawSpinSprite){
            Drawf.spinSprite(rotator, build.x, build.y, build.totalProgress * 2f);
        }else{
            Draw.rect(rotator, build.x, build.y, build.totalProgress * 2f);
        }
        if(top.found()) Draw.rect(top, build.x, build.y);
    }

    @Override
    public void load(Block block){
        rotator = Core.atlas.find(block.name + "-rotator");
        top = Core.atlas.find(block.name + "-top");
    }

    @Override
    public TextureRegion[] icons(Block block){
        return top.found() ? new TextureRegion[]{block.region, rotator, top} : new TextureRegion[]{block.region, rotator};
    }
}
