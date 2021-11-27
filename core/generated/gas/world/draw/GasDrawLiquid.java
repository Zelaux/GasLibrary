package gas.world.draw;

import gas.entities.comp.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.experimental.*;
import arc.*;
import gas.world.meta.*;
import gas.world.blocks.units.*;
import mindustry.world.blocks.production.GenericCrafter.*;
import gas.world.blocks.defense.*;
import mindustry.world.blocks.legacy.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.meta.*;
import gas.world.blocks.distribution.*;
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
import mindustry.world.draw.DrawLiquid.*;
import mindustry.graphics.*;
import gas.world.blocks.production.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import gas.world.meta.values.*;
import mindustry.world.blocks.power.*;
import mindustry.type.*;
import mindustry.world.blocks.sandbox.*;

public class GasDrawLiquid extends GasDrawBlock {

    public TextureRegion inLiquid, liquid, top;

    public boolean useOutputSprite = false;

    public GasDrawLiquid() {
    }

    public GasDrawLiquid(boolean useOutputSprite) {
        this.useOutputSprite = useOutputSprite;
    }

    @Override
    public void draw(GasGenericCrafterBuild build) {
        Draw.rect(build.block.region, build.x, build.y);
        GasGenericCrafter type = (GasGenericCrafter) build.block;
        if ((inLiquid.found() || useOutputSprite) && type.consumes.has(ConsumeType.liquid)) {
            Liquid input = type.consumes.<ConsumeLiquid>get(ConsumeType.liquid).liquid;
            Drawf.liquid(useOutputSprite ? liquid : inLiquid, build.x, build.y, build.liquids.get(input) / type.liquidCapacity, input.color);
        }
        if (type.outputLiquid != null && build.liquids.get(type.outputLiquid.liquid) > 0) {
            Drawf.liquid(liquid, build.x, build.y, build.liquids.get(type.outputLiquid.liquid) / type.liquidCapacity, type.outputLiquid.liquid.color);
        }
        if (top.found())
            Draw.rect(top, build.x, build.y);
    }

    @Override
    public void load(GasBlock block) {
        top = Core.atlas.find(block.name + "-top");
        liquid = Core.atlas.find(block.name + "-liquid");
        inLiquid = Core.atlas.find(block.name + "-input-liquid");
    }

    @Override
    public TextureRegion[] icons(GasBlock block) {
        return top.found() ? new TextureRegion[] { block.region, top } : new TextureRegion[] { block.region };
    }
}
