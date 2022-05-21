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
import mindustry.world.blocks.distribution.*;
import gas.world.blocks.power.*;
import mindustry.world.*;
import gas.world.blocks.sandbox.*;
import mindustry.world.blocks.storage.*;
import gas.world.blocks.liquid.*;
import gas.entities.*;
import mindustry.world.draw.DrawLiquidTile.*;
import gas.world.blocks.defense.turrets.*;
import gas.world.blocks.distribution.*;
import gas.world.*;
import mindustry.world.consumers.*;
import gas.world.blocks.gas.*;
import gas.world.blocks.campaign.*;
import mindustry.world.modules.*;
import gas.ui.*;
import mindustry.world.blocks.environment.*;
import gas.world.consumers.*;
import mindustry.world.blocks.payloads.*;
import mindustry.world.blocks.*;
import gas.world.blocks.production.GasGenericCrafter.*;
import mindustry.world.blocks.logic.*;
import gas.world.modules.*;
import gas.world.blocks.*;
import gas.*;
import gas.io.*;
import gas.world.draw.*;
import gas.gen.*;
import gas.world.blocks.storage.*;
import mindustry.world.blocks.units.*;
import mindustry.world.blocks.campaign.*;
import gas.world.blocks.production.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import mindustry.world.blocks.power.*;
import mindustry.type.*;
import mindustry.world.blocks.sandbox.*;

/**
 * Not standalone.
 */
public class GasDrawLiquidTile extends GasDrawBlock {

    public Liquid drawLiquid;

    public float padding;

    public float alpha = 1f;

    public GasDrawLiquidTile(Liquid drawLiquid, float padding) {
        this.drawLiquid = drawLiquid;
        this.padding = padding;
    }

    public GasDrawLiquidTile(Liquid drawLiquid) {
        this.drawLiquid = drawLiquid;
    }

    public GasDrawLiquidTile() {
    }

    @Override
    public void draw(GasBuilding build) {
        Liquid drawn = drawLiquid != null ? drawLiquid : build.liquids.current();
        GasLiquidBlock.drawTiledFrames(build.block.size, build.x, build.y, padding, drawn, build.liquids.get(drawn) / build.block.liquidCapacity * alpha);
    }
}
