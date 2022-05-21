package gas.world.blocks.heat;

import mindustry.entities.units.*;
import gas.entities.comp.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import gas.content.*;
import mindustry.world.blocks.defense.turrets.*;
import gas.world.blocks.payloads.*;
import gas.world.meta.*;
import mindustry.ui.*;
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
import mindustry.world.blocks.heat.HeatConductor.*;
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
import mindustry.graphics.*;
import gas.world.blocks.production.*;
import arc.struct.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;

public class GasHeatConductor extends GasBlock {

    public float visualMaxHeat = 15f;

    public DrawBlock drawer = new DrawDefault();

    public GasHeatConductor(String name) {
        super(name);
        update = solid = rotate = true;
        rotateDraw = false;
        size = 3;
    }

    @Override
    public void setBars() {
        super.setBars();
        // TODO show number
        addBar("heat", (GasHeatConductorBuild entity) -> new Bar(() -> Core.bundle.format("bar.heatamount", (int) entity.heat), () -> Pal.lightOrange, () -> entity.heat / visualMaxHeat));
    }

    @Override
    public void load() {
        super.load();
        drawer.load(this);
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
        drawer.drawPlan(this, plan, list);
    }

    @Override
    public TextureRegion[] icons() {
        return drawer.finalIcons(this);
    }

    public class GasHeatConductorBuild extends GasBuilding implements HeatBlock, HeatConsumer {

        public float heat = 0f;

        public float[] sideHeat = new float[4];

        public IntSet cameFrom = new IntSet();

        @Override
        public void draw() {
            drawer.draw(this);
        }

        @Override
        public void drawLight() {
            super.drawLight();
            drawer.drawLight(this);
        }

        @Override
        public float[] sideHeat() {
            return sideHeat;
        }

        @Override
        public float heatRequirement() {
            return visualMaxHeat;
        }

        @Override
        public void updateTile() {
            heat = calculateHeat(sideHeat, cameFrom);
        }

        @Override
        public float warmup() {
            return heat;
        }

        @Override
        public float heat() {
            return heat;
        }

        @Override
        public float heatFrac() {
            return heat / visualMaxHeat;
        }
    }
}
