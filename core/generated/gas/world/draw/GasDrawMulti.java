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
import mindustry.world.draw.DrawMulti.*;
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
import mindustry.world.blocks.payloads.*;
import mindustry.world.blocks.*;
import gas.world.blocks.production.GasGenericCrafter.*;
import arc.graphics.g2d.*;
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
import arc.struct.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;

/**
 * combined several DrawBlocks into one
 */
public class GasDrawMulti extends GasDrawBlock {

    public GasDrawBlock[] drawers = {};

    public GasDrawMulti() {
    }

    public GasDrawMulti(GasDrawBlock... drawers) {
        this.drawers = drawers;
    }

    public GasDrawMulti(Seq<GasDrawBlock> drawers) {
        this.drawers = drawers.toArray(GasDrawBlock.class);
    }

    @Override
    public void getRegionsToOutline(GasBlock block, Seq<TextureRegion> out) {
        for (var draw : drawers) {
            draw.getRegionsToOutline(block, out);
        }
    }

    @Override
    public void draw(GasBuilding build) {
        for (var draw : drawers) {
            draw.draw(build);
        }
    }

    @Override
    public void drawPlan(GasBlock block, BuildPlan plan, Eachable<BuildPlan> list) {
        for (var draw : drawers) {
            draw.drawPlan(block, plan, list);
        }
    }

    @Override
    public void drawLight(GasBuilding build) {
        for (var draw : drawers) {
            draw.drawLight(build);
        }
    }

    @Override
    public void load(GasBlock block) {
        for (var draw : drawers) {
            draw.load(block);
        }
    }

    @Override
    public TextureRegion[] icons(GasBlock block) {
        var result = new Seq<TextureRegion>();
        for (var draw : drawers) {
            result.addAll(draw.icons(block));
        }
        return result.toArray(TextureRegion.class);
    }
}
