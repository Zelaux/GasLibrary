package gas.world.blocks.liquid;

import gas.entities.comp.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import gas.content.*;
import mindustry.world.blocks.defense.turrets.*;
import gas.world.blocks.payloads.*;
import gas.world.meta.*;
import mindustry.annotations.Annotations.*;
import gas.world.blocks.units.*;
import mindustry.world.blocks.heat.*;
import arc.util.*;
import mindustry.world.blocks.legacy.*;
import mindustry.gen.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.meta.*;
import gas.world.blocks.heat.*;
import gas.world.blocks.defense.*;
import mindustry.world.blocks.liquid.LiquidBlock.*;
import mindustry.world.blocks.distribution.*;
import gas.world.blocks.power.*;
import mindustry.world.*;
import gas.world.blocks.sandbox.*;
import mindustry.world.blocks.storage.*;
import gas.world.blocks.liquid.*;
import gas.entities.*;
import mindustry.world.blocks.campaign.*;
import gas.world.blocks.defense.turrets.*;
import gas.world.blocks.distribution.*;
import gas.world.*;
import mindustry.type.*;
import gas.world.blocks.gas.*;
import gas.world.blocks.campaign.*;
import mindustry.world.modules.*;
import gas.ui.*;
import mindustry.world.blocks.environment.*;
import gas.world.consumers.*;
import mindustry.world.blocks.payloads.*;
import mindustry.world.blocks.*;
import gas.world.blocks.production.GasGenericCrafter.*;
import arc.graphics.g2d.*;
import mindustry.world.blocks.logic.*;
import gas.world.modules.*;
import gas.world.blocks.*;
import arc.graphics.*;
import gas.*;
import gas.io.*;
import gas.world.draw.*;
import gas.gen.*;
import gas.world.blocks.storage.*;
import mindustry.world.blocks.units.*;
import gas.entities.bullets.*;
import mindustry.graphics.*;
import gas.world.blocks.production.*;
import mindustry.world.blocks.defense.*;
import mindustry.world.consumers.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;
import static mindustry.Vars.*;

public class GasLiquidBlock extends GasBlock {

    @Load("@-liquid")
    public TextureRegion liquidRegion;

    @Load("@-top")
    public TextureRegion topRegion;

    @Load("@-bottom")
    public TextureRegion bottomRegion;

    public GasLiquidBlock(String name) {
        super(name);
        update = true;
        solid = true;
        hasLiquids = true;
        group = BlockGroup.liquids;
        outputsLiquid = true;
        envEnabled |= Env.space | Env.underwater;
    }

    @Override
    public TextureRegion[] icons() {
        return new TextureRegion[] { bottomRegion, topRegion };
    }

    public static void drawTiledFrames(int size, float x, float y, float padding, Liquid liquid, float alpha) {
        TextureRegion region = renderer.fluidFrames[liquid.gas ? 1 : 0][liquid.getAnimationFrame()];
        TextureRegion toDraw = Tmp.tr1;
        float bounds = size / 2f * tilesize - padding;
        Color color = Tmp.c1.set(liquid.color).a(1f);
        for (int sx = 0; sx < size; sx++) {
            for (int sy = 0; sy < size; sy++) {
                float relx = sx - (size - 1) / 2f, rely = sy - (size - 1) / 2f;
                toDraw.set(region);
                // truncate region if at border
                float rightBorder = relx * tilesize + padding, topBorder = rely * tilesize + padding;
                float squishX = rightBorder + tilesize / 2f - bounds, squishY = topBorder + tilesize / 2f - bounds;
                float ox = 0f, oy = 0f;
                if (squishX >= 8 || squishY >= 8)
                    continue;
                // cut out the parts that don't fit inside the padding
                if (squishX > 0) {
                    toDraw.setWidth(toDraw.width - squishX * 4f);
                    ox = -squishX / 2f;
                }
                if (squishY > 0) {
                    toDraw.setY(toDraw.getY() + squishY * 4f);
                    oy = -squishY / 2f;
                }
                Drawf.liquid(toDraw, x + rightBorder + ox, y + topBorder + oy, alpha, color);
            }
        }
    }

    public class GasLiquidBuild extends GasBuilding {

        @Override
        public void draw() {
            float rotation = rotate ? rotdeg() : 0;
            Draw.rect(bottomRegion, x, y, rotation);
            if (liquids.currentAmount() > 0.001f) {
                Drawf.liquid(liquidRegion, x, y, liquids.currentAmount() / liquidCapacity, liquids.current().color);
            }
            Draw.rect(topRegion, x, y, rotation);
        }
    }
}
