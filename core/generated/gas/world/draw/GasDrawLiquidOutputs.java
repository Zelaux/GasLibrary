package gas.world.draw;

import mindustry.entities.units.*;
import gas.entities.comp.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import gas.content.*;
import mindustry.world.blocks.defense.turrets.*;
import gas.world.blocks.payloads.*;
import gas.world.meta.*;
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
import gas.world.blocks.production.*;
import mindustry.world.draw.DrawLiquidOutputs.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;

/**
 * This must be used in conjunction with another DrawBlock; it only draws outputs.
 */
public class GasDrawLiquidOutputs extends GasDrawBlock {

    public TextureRegion[][] liquidOutputRegions;

    @Override
    public void draw(GasBuilding build) {
        GasGenericCrafter crafter = (GasGenericCrafter) build.block;
        if (crafter.outputLiquids == null)
            return;
        for (int i = 0; i < crafter.outputLiquids.length; i++) {
            int side = i < crafter.liquidOutputDirections.length ? crafter.liquidOutputDirections[i] : -1;
            if (side != -1) {
                int realRot = (side + build.rotation) % 4;
                Draw.rect(liquidOutputRegions[realRot > 1 ? 1 : 0][i], build.x, build.y, realRot * 90);
            }
        }
    }

    @Override
    public void drawPlan(GasBlock block, BuildPlan plan, Eachable<BuildPlan> list) {
        GasGenericCrafter crafter = (GasGenericCrafter) block;
        if (crafter.outputLiquids == null)
            return;
        for (int i = 0; i < crafter.outputLiquids.length; i++) {
            int side = i < crafter.liquidOutputDirections.length ? crafter.liquidOutputDirections[i] : -1;
            if (side != -1) {
                int realRot = (side + plan.rotation) % 4;
                Draw.rect(liquidOutputRegions[realRot > 1 ? 1 : 0][i], plan.drawx(), plan.drawy(), realRot * 90);
            }
        }
    }

    @Override
    public void load(GasBlock block) {
        var crafter = expectCrafter(block);
        if (crafter.outputLiquids == null)
            return;
        liquidOutputRegions = new TextureRegion[2][crafter.outputLiquids.length];
        for (int i = 0; i < crafter.outputLiquids.length; i++) {
            for (int j = 1; j <= 2; j++) {
                liquidOutputRegions[j - 1][i] = Core.atlas.find(block.name + "-" + crafter.outputLiquids[i].liquid.name + "-output" + j);
            }
        }
    }

    // can't display these properly
    @Override
    public TextureRegion[] icons(GasBlock block) {
        return new TextureRegion[] {};
    }
}
