package gas.world.draw;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.util.Time;
import gas.annotations.GasAnnotations;
import gas.world.blocks.production.GasGenericCrafter;
import mindustry.graphics.Drawf;
import mindustry.world.Block;
import mindustry.world.blocks.production.GenericCrafter.GenericCrafterBuild;
import mindustry.world.draw.DrawBlock;

@GasAnnotations.GasAddition(analogue = "auto")
public class GasDrawCultivator extends GasDrawBlock {
    public Color plantColor = Color.valueOf("5541b1");
    public Color plantColorLight = Color.valueOf("7457ce");
    public Color bottomColor = Color.valueOf("474747");

    public int bubbles = 12, sides = 8;
    public float strokeMin = 0.2f, spread = 3f, timeScl = 70f;
    public float recurrence = 6f, radius = 3f;

    public TextureRegion middle;
    public TextureRegion top;

    @Override
    public void draw(GasGenericCrafter.GasGenericCrafterBuild build){
        Draw.rect(build.block.region, build.x, build.y);

        Drawf.liquid(middle, build.x, build.y, build.warmup, plantColor);

        Draw.color(bottomColor, plantColorLight, build.warmup);

        rand.setSeed(build.pos());
        for(int i = 0; i < bubbles; i++){
            float x = rand.range(spread), y = rand.range(spread);
            float life = 1f - ((Time.time / timeScl + rand.random(recurrence)) % recurrence);

            if(life > 0){
                Lines.stroke(build.warmup * (life + strokeMin));
                Lines.poly(build.x + x, build.y + y, sides, (1f - life) * radius);
            }
        }

        Draw.color();
        Draw.rect(top, build.x, build.y);
    }

    @Override
    public void load(Block block){
        middle = Core.atlas.find(block.name + "-middle");
        top = Core.atlas.find(block.name + "-top");
    }

    @Override
    public TextureRegion[] icons(Block block){
        return new TextureRegion[]{block.region, top};
    }
}
