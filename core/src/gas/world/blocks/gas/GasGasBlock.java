package gas.world.blocks.gas;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.util.*;
import gas.gen.*;
import gas.type.*;
import gas.world.*;
import mindustry.annotations.Annotations.*;
import mindustry.graphics.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class GasGasBlock extends GasBlock{

    public @Load("@-gas") TextureRegion gasRegion;
    public @Load("@-top") TextureRegion topRegion;
    public @Load("@-bottom") TextureRegion bottomRegion;

    public GasGasBlock(String name){
        super(name);
        update = true;
        solid = true;
        hasGasses = true;
        group = BlockGroup.liquids;
        outputsGas = true;
        envEnabled |= Env.space | Env.underwater;
    }

    @Override
    public TextureRegion[] icons(){
        return new TextureRegion[]{bottomRegion, topRegion};
    }
    public static void drawTiledFrames(int size, float x, float y, float padding, Gas gas, float alpha){
        TextureRegion region = renderer.fluidFrames[true ? 1 : 0][gas.getAnimationFrame()];
        TextureRegion toDraw = Tmp.tr1;

        float bounds = size / 2f * tilesize - padding;
        Color color = Tmp.c1.set(gas.color).a(1f);

        for(int sx = 0; sx < size; sx++){
            for(int sy = 0; sy < size; sy++){
                float relx = sx - (size - 1) / 2f, rely = sy - (size - 1) / 2f;

                toDraw.set(region);

                //truncate region if at border
                float rightBorder = relx * tilesize + padding, topBorder = rely * tilesize + padding;
                float squishX = rightBorder + tilesize / 2f - bounds, squishY = topBorder + tilesize / 2f - bounds;
                float ox = 0f, oy = 0f;

                if(squishX >= 8 || squishY >= 8) continue;

                //cut out the parts that don't fit inside the padding
                if(squishX > 0){
                    toDraw.setWidth(toDraw.width - squishX * 4f);
                    ox = -squishX / 2f;
                }

                if(squishY > 0){
                    toDraw.setY(toDraw.getY() + squishY * 4f);
                    oy = -squishY / 2f;
                }

                Drawf.liquid(toDraw, x + rightBorder + ox, y + topBorder + oy, alpha, color);
            }
        }
    }


    public class GasGasBuild extends GasBuilding{

        @Override
        public void draw(){
            float rotation = rotate ? rotdeg() : 0;
            Draw.rect(bottomRegion, x, y, rotation);
//            liquids.currentAmount()
            if(gasses.currentAmount() > 0.001f){
                Drawf.liquid(gasRegion, x, y, gasses.currentAmount() / gasCapacity, gasses.current().color);
            }
            Draw.rect(topRegion, x, y, rotation);
        }
    }
}
