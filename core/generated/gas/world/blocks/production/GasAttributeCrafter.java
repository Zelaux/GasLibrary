package gas.world.blocks.production;

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
import mindustry.game.*;
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
import mindustry.world.blocks.production.AttributeCrafter.*;
import mindustry.world.blocks.sandbox.*;

/**
 * A crafter that gains efficiency from attribute tiles.
 */
public class GasAttributeCrafter extends GasGenericCrafter {

    public Attribute attribute = Attribute.heat;

    public float baseEfficiency = 1f;

    public float boostScale = 1f;

    public float maxBoost = 1f;

    public float minEfficiency = -1f;

    public float displayEfficiencyScale = 1f;

    public boolean displayEfficiency = true;

    public GasAttributeCrafter(String name) {
        super(name);
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        if (!displayEfficiency)
            return;
        drawPlaceText(Core.bundle.format("bar.efficiency", (int) ((baseEfficiency + Math.min(maxBoost, boostScale * sumAttribute(attribute, x, y))) * 100f)), x, y, valid);
    }

    @Override
    public void setBars() {
        super.setBars();
        if (!displayEfficiency)
            return;
        addBar("efficiency", (GasAttributeCrafterBuild entity) -> new Bar(() -> Core.bundle.format("bar.efficiency", (int) (entity.efficiencyScale() * 100 * displayEfficiencyScale)), () -> Pal.lightOrange, entity::efficiencyScale));
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation) {
        // make sure there's enough efficiency at this location
        return baseEfficiency + tile.getLinkedTilesAs(this, tempTiles).sumf(other -> other.floor().attributes.get(attribute)) >= minEfficiency;
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(baseEfficiency <= 0.0001f ? Stat.tiles : Stat.affinities, attribute, floating, boostScale * size * size, !displayEfficiency);
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
        public void pickedUp() {
            attrsum = 0f;
            warmup = 0f;
        }

        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
            attrsum = sumAttribute(attribute, tile.x, tile.y);
        }
    }
}
