package gas.world.blocks.production;

import gas.entities.comp.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import mindustry.world.blocks.production.HeatCrafter.*;
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
import gas.world.blocks.gas.*;
import gas.world.blocks.campaign.*;
import mindustry.world.modules.*;
import gas.ui.*;
import mindustry.world.blocks.environment.*;
import gas.world.consumers.*;
import mindustry.world.blocks.payloads.*;
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
import mindustry.world.blocks.sandbox.*;

/**
 * A crafter that requires contact from heater blocks to craft.
 */
public class GasHeatCrafter extends GasGenericCrafter {

    /**
     * Base heat requirement for 100% efficiency.
     */
    public float heatRequirement = 10f;

    /**
     * After heat meets this requirement, excess heat will be scaled by this number.
     */
    public float overheatScale = 1f;

    /**
     * Maximum possible efficiency after overheat.
     */
    public float maxEfficiency = 4f;

    public GasHeatCrafter(String name) {
        super(name);
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("heat", (GasHeatCrafterBuild entity) -> new Bar(() -> Core.bundle.format("bar.heatpercent", (int) entity.heat, (int) (entity.efficiencyScale() * 100)), () -> Pal.lightOrange, () -> entity.heat / heatRequirement));
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.input, heatRequirement, StatUnit.heatUnits);
        stats.add(Stat.maxEfficiency, (int) (maxEfficiency * 100f), StatUnit.percent);
    }

    public class GasHeatCrafterBuild extends GasGenericCrafterBuild implements HeatConsumer {

        // TODO sideHeat could be smooth
        public float[] sideHeat = new float[4];

        public float heat = 0f;

        @Override
        public void updateTile() {
            heat = calculateHeat(sideHeat);
            super.updateTile();
        }

        @Override
        public float heatRequirement() {
            return heatRequirement;
        }

        @Override
        public float[] sideHeat() {
            return sideHeat;
        }

        @Override
        public float warmupTarget() {
            return Mathf.clamp(heat / heatRequirement);
        }

        @Override
        public void updateEfficiencyMultiplier() {
            efficiency *= efficiencyScale();
            potentialEfficiency *= efficiencyScale();
        }

        public float efficiencyScale() {
            float over = Math.max(heat - heatRequirement, 0f);
            return Math.min(Mathf.clamp(heat / heatRequirement) + over / heatRequirement * overheatScale, maxEfficiency);
        }
    }
}
