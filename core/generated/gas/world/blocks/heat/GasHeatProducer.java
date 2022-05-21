package gas.world.blocks.heat;

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
import mindustry.world.blocks.legacy.*;
import mindustry.gen.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import arc.math.*;
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
import mindustry.world.blocks.campaign.*;
import gas.world.blocks.defense.turrets.*;
import gas.world.blocks.distribution.*;
import gas.world.*;
import mindustry.world.consumers.*;
import mindustry.world.blocks.heat.HeatProducer.*;
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
import mindustry.graphics.*;
import gas.world.blocks.production.*;
import arc.util.io.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;

public class GasHeatProducer extends GasGenericCrafter {

    public float heatOutput = 10f;

    public float warmupRate = 0.15f;

    public GasHeatProducer(String name) {
        super(name);
        drawer = new GasDrawMulti(new GasDrawDefault(), new GasDrawHeatOutput());
        rotateDraw = false;
        rotate = true;
        canOverdrive = false;
        drawArrow = true;
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.output, heatOutput, StatUnit.heatUnits);
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("heat", (GasHeatProducerBuild entity) -> new Bar("bar.heat", Pal.lightOrange, () -> entity.heat / heatOutput));
    }

    public class GasHeatProducerBuild extends GasGenericCrafterBuild implements HeatBlock {

        public float heat;

        @Override
        public void updateTile() {
            super.updateTile();
            // heat approaches target at the same speed regardless of efficiency
            heat = Mathf.approachDelta(heat, heatOutput * efficiency, warmupRate * delta());
        }

        @Override
        public float heatFrac() {
            return heat / heatOutput;
        }

        @Override
        public float heat() {
            return heat;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(heat);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            heat = read.f();
        }
    }
}
