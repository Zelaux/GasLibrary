package gas.world.blocks.power;

import gas.entities.comp.*;
import mindustry.entities.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import mindustry.content.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.experimental.*;
import gas.world.meta.*;
import gas.world.blocks.units.*;
import gas.world.blocks.defense.*;
import mindustry.world.blocks.legacy.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.power.ThermalGenerator.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import arc.math.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.meta.*;
import arc.graphics.*;
import gas.world.blocks.distribution.*;
import gas.world.draw.*;
import mindustry.world.blocks.logic.*;
import mindustry.gen.*;
import gas.world.blocks.power.*;
import mindustry.world.*;
import gas.world.blocks.sandbox.*;
import mindustry.world.blocks.storage.*;
import gas.world.blocks.liquid.*;
import mindustry.game.*;
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
import arc.*;
import mindustry.world.consumers.*;
import gas.world.modules.*;
import gas.world.blocks.*;
import gas.*;
import gas.io.*;
import gas.world.blocks.payloads.*;
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

public class GasThermalGenerator extends GasPowerGenerator {

    public Effect generateEffect = Fx.none;

    public float effectChance = 0.05f;

    public Attribute attribute = Attribute.heat;

    public GasThermalGenerator(String name) {
        super(name);
    }

    @Override
    public void init() {
        super.init();
        // proper light clipping
        clipSize = Math.max(clipSize, 45f * size * 2f * 2f);
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.tiles, attribute, floating, size * size, false);
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        drawPlaceText(Core.bundle.formatFloat("bar.efficiency", sumAttribute(attribute, x, y) * 100, 1), x, y, valid);
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation) {
        // make sure there's heat at this location
        return tile.getLinkedTilesAs(this, tempTiles).sumf(other -> other.floor().attributes.get(attribute)) > 0.01f;
    }

    public class GasThermalGeneratorBuild extends GasGeneratorBuild {

        public float sum;

        @Override
        public void updateTile() {
            productionEfficiency = sum + attribute.env();
            if (productionEfficiency > 0.1f && Mathf.chanceDelta(effectChance)) {
                generateEffect.at(x + Mathf.range(3f), y + Mathf.range(3f));
            }
        }

        @Override
        public void drawLight() {
            Drawf.light(team, x, y, (40f + Mathf.absin(10f, 5f)) * Math.min(productionEfficiency, 2f) * size, Color.scarlet, 0.4f);
        }

        @Override
        public void onProximityAdded() {
            super.onProximityAdded();
            sum = sumAttribute(attribute, tile.x, tile.y);
        }
    }
}
