package gas.world.blocks.power;

import mindustry.entities.units.*;
import gas.entities.comp.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import mindustry.world.blocks.defense.turrets.*;
import gas.world.blocks.payloads.*;
import mindustry.world.blocks.experimental.*;
import gas.world.meta.*;
import mindustry.annotations.Annotations.*;
import gas.world.blocks.units.*;
import gas.world.blocks.defense.*;
import arc.util.*;
import mindustry.world.blocks.legacy.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import arc.math.*;
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
import mindustry.world.blocks.power.PowerDiode.*;
import gas.*;
import gas.io.*;
import mindustry.ui.*;
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

public class GasPowerDiode extends GasBlock {

    @Load("@-arrow")
    public TextureRegion arrow;

    public GasPowerDiode(String name) {
        super(name);
        rotate = true;
        update = true;
        solid = true;
        insulated = true;
        group = BlockGroup.power;
        noUpdateDisabled = true;
        schematicPriority = 10;
        envEnabled |= Env.space;
    }

    @Override
    public void setBars() {
        super.setBars();
        bars.add("back", entity -> new Bar("bar.input", Pal.powerBar, () -> bar(entity.back())));
        bars.add("front", entity -> new Bar("bar.output", Pal.powerBar, () -> bar(entity.front())));
    }

    @Override
    public void drawRequestRegion(BuildPlan req, Eachable<BuildPlan> list) {
        Draw.rect(fullIcon, req.drawx(), req.drawy());
        Draw.rect(arrow, req.drawx(), req.drawy(), !rotate ? 0 : req.rotation * 90);
    }

    // battery % of the graph on either side, defaults to zero
    public float bar(Building tile) {
        return (tile != null && tile.block.hasPower) ? tile.power.graph.getLastPowerStored() / tile.power.graph.getTotalBatteryCapacity() : 0f;
    }

    public class GasPowerDiodeBuild extends GasBuilding {

        @Override
        public void draw() {
            Draw.rect(region, x, y, 0);
            Draw.rect(arrow, x, y, rotate ? rotdeg() : 0);
        }

        @Override
        public void updateTile() {
            super.updateTile();
            if (front() == null || back() == null || !back().block.hasPower || !front().block.hasPower || back().team != front().team)
                return;
            PowerGraph backGraph = back().power.graph;
            PowerGraph frontGraph = front().power.graph;
            if (backGraph == frontGraph)
                return;
            // 0f - 1f of battery capacity in use
            float backStored = backGraph.getBatteryStored() / backGraph.getTotalBatteryCapacity();
            float frontStored = frontGraph.getBatteryStored() / frontGraph.getTotalBatteryCapacity();
            // try to send if the back side has more % capacity stored than the front side
            if (backStored > frontStored) {
                // send half of the difference
                float amount = backGraph.getBatteryStored() * (backStored - frontStored) / 2;
                // prevent sending more than the front can handle
                amount = Mathf.clamp(amount, 0, frontGraph.getTotalBatteryCapacity() * (1 - frontStored));
                backGraph.transferPower(-amount);
                frontGraph.transferPower(amount);
            }
        }
    }
}
