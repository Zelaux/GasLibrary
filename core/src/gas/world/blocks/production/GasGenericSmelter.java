package gas.world.blocks.production;

import gas.annotations.GasAnnotations;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.util.Time;
import mindustry.gen.Sounds;
import mindustry.graphics.Drawf;

public class GasGenericSmelter extends GasGenericCrafter{
    public Color flameColor = Color.valueOf("ffc999");
    public @GasAnnotations.Load("@-top")
    TextureRegion topRegion;
    public GasGenericSmelter(String name) {
        super(name);
        ambientSound = Sounds.smelter;
        ambientSoundVolume = 0.07f;
    }
    public class GasSmelterBuild extends GasGenericCrafterBuild {
        @Override
        public void draw(){
            super.draw();

            //draw glowing center
            if(warmup > 0f && flameColor.a > 0.001f){
                float g = 0.3f;
                float r = 0.06f;
                float cr = Mathf.random(0.1f);

                Draw.alpha(((1f - g) + Mathf.absin(Time.time, 8f, g) + Mathf.random(r) - r) * warmup);

                Draw.tint(flameColor);
                Fill.circle(x, y, 3f + Mathf.absin(Time.time, 5f, 2f) + cr);
                Draw.color(1f, 1f, 1f, warmup);
                Draw.rect(topRegion, x, y);
                Fill.circle(x, y, 1.9f + Mathf.absin(Time.time, 5f, 1f) + cr);

                Draw.color();
            }
        }

        @Override
        public void drawLight(){
            Drawf.light(team, x, y, (60f + Mathf.absin(10f, 5f)) * warmup * size, flameColor, 0.65f);
        }
    }
}
