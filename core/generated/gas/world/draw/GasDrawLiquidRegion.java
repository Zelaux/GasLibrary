package gas.world.draw;

import gas.entities.comp.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import gas.content.*;
import mindustry.world.blocks.defense.turrets.*;
import gas.world.blocks.payloads.*;
import gas.world.meta.*;
import gas.world.blocks.units.*;
import mindustry.world.blocks.heat.*;
import mindustry.world.blocks.legacy.*;
import mindustry.gen.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.meta.*;
import gas.world.blocks.heat.*;
import gas.world.blocks.defense.*;
import mindustry.world.blocks.payloads.*;
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
import mindustry.world.consumers.*;
import mindustry.world.draw.DrawLiquidRegion.*;
import gas.world.blocks.gas.*;
import gas.world.blocks.campaign.*;
import mindustry.world.modules.*;
import gas.ui.*;
import mindustry.world.blocks.environment.*;
import gas.world.consumers.*;
import arc.graphics.g2d.*;
import mindustry.world.blocks.*;
import gas.world.blocks.production.GasGenericCrafter.*;
import arc.*;
import mindustry.world.blocks.logic.*;
import gas.world.modules.*;
import gas.world.blocks.*;
import gas.*;
import gas.io.*;
import gas.world.draw.*;
import gas.gen.*;
import gas.world.blocks.storage.*;
import mindustry.world.blocks.units.*;
import mindustry.graphics.*;
import gas.world.blocks.production.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import mindustry.world.blocks.power.*;
import mindustry.type.*;
import mindustry.world.blocks.sandbox.*;

/**
 * Not standalone.
 */
public class GasDrawLiquidRegion extends GasDrawBlock {

    public Liquid drawLiquid;

    public TextureRegion liquid;

    public String suffix = "-liquid";

    public float alpha = 1f;

    public GasDrawLiquidRegion(Liquid drawLiquid) {
        this.drawLiquid = drawLiquid;
    }

    public GasDrawLiquidRegion() {
    }

    @Override
    public void draw(GasBuilding build) {
        Liquid drawn = drawLiquid != null ? drawLiquid : build.liquids.current();
        Drawf.liquid(liquid, build.x, build.y, build.liquids.get(drawn) / build.block.liquidCapacity * alpha, drawn.color);
    }

    @Override
    public void load(GasBlock block) {
        if (!block.hasLiquids) {
            throw new RuntimeException("Block '" + block + "' has a DrawLiquidRegion, but hasLiquids is false! Make sure it is true.");
        }
        liquid = Core.atlas.find(block.name + suffix);
    }
}
