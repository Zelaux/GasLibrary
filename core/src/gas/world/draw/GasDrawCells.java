package gas.world.draw;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.util.Time;
import gas.annotations.GasAnnotations;
import gas.world.blocks.production.GasGenericCrafter;
import mindustry.graphics.Drawf;
import mindustry.world.Block;
import mindustry.world.blocks.production.GenericCrafter.GenericCrafterBuild;
import mindustry.world.draw.DrawBlock;

@GasAnnotations.GasAddition(analogue = "auto")
public class GasDrawCells extends GasDrawBlock {
    public TextureRegion bottom, middle;
    public Color color = Color.white.cpy(), particleColorFrom = Color.black.cpy(), particleColorTo = Color.black.cpy();
    public int particles = 12;
    public float range = 4f, recurrence = 6f, radius = 3f, lifetime = 60f;

    @Override
    public void draw(GasGenericCrafter.GasGenericCrafterBuild build){

        Draw.rect(bottom, build.x, build.y);

        Drawf.liquid(middle, build.x, build.y, build.warmup, color);

        if(build.warmup > 0.001f){
            rand.setSeed(build.id);
            for(int i = 0; i < particles; i++){
                float offset = rand.nextFloat() * 999999f;
                float x = rand.range(range), y = rand.range(range);
                float fin = 1f - (((Time.time + offset) / lifetime) % recurrence);
                float ca = rand.random(0.1f, 1f);
                float fslope = Mathf.slope(fin);

                if(fin > 0){
                    Draw.color(particleColorFrom, particleColorTo, ca);
                    Draw.alpha(build.warmup);

                    Fill.circle(build.x + x, build.y + y, fslope * radius);
                }
            }
        }

        Draw.color();
        Draw.rect(build.block.region, build.x, build.y);
    }

    @Override
    public void load(Block block){
        bottom = Core.atlas.find(block.name + "-bottom");
        middle = Core.atlas.find(block.name + "-middle");
    }

    @Override
    public TextureRegion[] icons(Block block){
        return new TextureRegion[]{bottom, block.region};
    }
}
