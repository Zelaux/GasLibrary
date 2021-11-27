package gas.world.blocks.liquid;

import gas.entities.comp.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.experimental.*;
import gas.world.meta.*;
import mindustry.annotations.Annotations.*;
import gas.world.blocks.units.*;
import gas.world.blocks.defense.*;
import mindustry.world.blocks.legacy.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.meta.*;
import gas.world.blocks.distribution.*;
import mindustry.world.blocks.liquid.LiquidBlock.*;
import gas.world.draw.*;
import mindustry.world.blocks.logic.*;
import mindustry.gen.*;
import gas.world.blocks.power.*;
import mindustry.world.*;
import gas.world.blocks.sandbox.*;
import mindustry.world.blocks.storage.*;
import gas.world.blocks.liquid.*;
import gas.entities.*;
import mindustry.world.blocks.campaign.*;
import gas.gen.*;
import gas.world.*;
import gas.world.blocks.defense.turrets.*;
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
import mindustry.world.consumers.*;
import gas.world.modules.*;
import gas.world.blocks.*;
import gas.*;
import gas.io.*;
import gas.world.blocks.payloads.*;
import mindustry.world.blocks.units.*;
import gas.content.*;
import gas.world.blocks.storage.*;
import mindustry.graphics.*;
import gas.world.blocks.production.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import gas.world.meta.values.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;

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

    public class GasLiquidBuild extends GasBuilding {

        @Override
        public void draw() {
            float rotation = rotate ? rotdeg() : 0;
            Draw.rect(bottomRegion, x, y, rotation);
            if (liquids.total() > 0.001f) {
                Drawf.liquid(liquidRegion, x, y, liquids.total() / liquidCapacity, liquids.current().color);
            }
            Draw.rect(topRegion, x, y, rotation);
        }
    }
}
