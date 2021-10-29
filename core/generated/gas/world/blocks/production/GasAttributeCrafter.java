package gas.world.blocks.production;

import gas.entities.comp.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.experimental.*;
import gas.world.meta.*;
import mindustry.ui.*;
import gas.world.blocks.units.*;
import gas.world.blocks.defense.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.meta.*;
import gas.world.blocks.distribution.*;
import mindustry.gen.*;
import gas.world.blocks.payloads.*;
import mindustry.world.*;
import gas.world.blocks.production.*;
import mindustry.world.blocks.legacy.*;
import gas.world.blocks.liquid.*;
import mindustry.world.blocks.storage.*;
import gas.entities.*;
import mindustry.world.blocks.campaign.*;
import gas.world.blocks.defense.turrets.*;
import gas.gen.*;
import gas.world.*;
import mindustry.world.consumers.*;
import gas.world.blocks.gas.*;
import gas.world.blocks.campaign.*;
import mindustry.world.modules.*;
import gas.ui.*;
import mindustry.world.blocks.environment.*;
import gas.world.consumers.*;
import mindustry.world.blocks.payloads.*;
import gas.world.blocks.sandbox.*;
import mindustry.world.blocks.*;
import gas.world.blocks.production.GasGenericCrafter.*;
import arc.*;
import mindustry.world.blocks.logic.*;
import gas.world.modules.*;
import gas.world.blocks.*;
import gas.*;
import gas.io.*;
import gas.world.draw.*;
import mindustry.world.blocks.units.*;
import gas.content.*;
import gas.world.blocks.storage.*;
import mindustry.graphics.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import gas.world.meta.values.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.production.AttributeCrafter.*;
import mindustry.world.blocks.sandbox.*;
import gas.world.blocks.power.*;

/**
 * A crafter that gains efficiency from attribute tiles.
 */
public class GasAttributeCrafter extends GasGenericCrafter {

    public Attribute attribute = Attribute.heat;

    public float baseEfficiency = 1f;

    public float boostScale = 1f;

    public float maxBoost = 1f;

    public GasAttributeCrafter(String name) {
        super(name);
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        drawPlaceText(Core.bundle.format("bar.efficiency", (int) ((baseEfficiency + Math.min(maxBoost, boostScale * sumAttribute(attribute, x, y))) * 100f)), x, y, valid);
    }

    @Override
    public void setBars() {
        super.setBars();
        bars.add("efficiency", (GasAttributeCrafterBuild entity) -> new Bar(() -> Core.bundle.format("bar.efficiency", (int) (entity.efficiencyScale() * 100)), () -> Pal.lightOrange, entity::efficiencyScale));
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.affinities, attribute, boostScale * size * size);
    }

    public class GasAttributeCrafterBuild extends GasGenericCrafterBuild {

        public float attrsum;

        @Override
        public float getProgressIncrease(float base) {
            return super.getProgressIncrease(base) * efficiencyScale();
        }

        public float efficiencyScale() {
            return baseEfficiency + Math.min(maxBoost, boostScale * attrsum) + attribute.env();
        }

        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
            attrsum = sumAttribute(attribute, tile.x, tile.y);
        }
    }
}
