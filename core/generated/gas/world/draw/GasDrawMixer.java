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
import mindustry.world.draw.DrawMixer.*;
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
import mindustry.graphics.*;
import gas.world.blocks.production.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import gas.world.meta.values.*;
import mindustry.world.blocks.power.*;
import mindustry.type.*;
import mindustry.world.blocks.sandbox.*;

public class GasDrawMixer extends GasDrawBlock {

    public TextureRegion inLiquid, liquid, top, bottom;

    public boolean useOutputSprite;

    public GasDrawMixer() {
    }

    public GasDrawMixer(boolean useOutputSprite) {
        this.useOutputSprite = useOutputSprite;
    }

    @Override
    public void draw(GasGenericCrafterBuild build) {
        float rotation = build.block.rotate ? build.rotdeg() : 0;
        Draw.rect(bottom, build.x, build.y, rotation);
        if ((inLiquid.found() || useOutputSprite) && build.block.consumes.has(ConsumeType.liquid)) {
            Liquid input = build.block.consumes.<ConsumeLiquid>get(ConsumeType.liquid).liquid;
            Drawf.liquid(useOutputSprite ? liquid : inLiquid, build.x, build.y, build.liquids.get(input) / build.block.liquidCapacity, input.color);
        }
        if (build.liquids.total() > 0.001f) {
            Draw.color(((GasGenericCrafter) build.block).outputLiquid.liquid.color);
            Draw.alpha(build.liquids.get(((GasGenericCrafter) build.block).outputLiquid.liquid) / build.block.liquidCapacity);
            Draw.rect(liquid, build.x, build.y, rotation);
            Draw.color();
        }
        Draw.rect(top, build.x, build.y, rotation);
    }

    @Override
    public void load(GasBlock block) {
        inLiquid = Core.atlas.find(block.name + "-input-liquid");
        liquid = Core.atlas.find(block.name + "-liquid");
        top = Core.atlas.find(block.name + "-top");
        bottom = Core.atlas.find(block.name + "-bottom");
    }

    @Override
    public TextureRegion[] icons(GasBlock block) {
        return new TextureRegion[] { bottom, top };
    }
}
