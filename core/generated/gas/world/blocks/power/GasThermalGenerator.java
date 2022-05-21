package gas.world.blocks.power;

import gas.entities.comp.*;
import mindustry.entities.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import mindustry.content.*;
import gas.content.*;
import mindustry.world.blocks.defense.turrets.*;
import gas.world.blocks.payloads.*;
import gas.world.meta.*;
import mindustry.annotations.Annotations.*;
import gas.world.blocks.units.*;
import mindustry.world.blocks.heat.*;
import arc.util.*;
import mindustry.world.blocks.legacy.*;
import mindustry.gen.*;
import mindustry.world.blocks.power.ThermalGenerator.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import arc.math.*;
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
import mindustry.game.*;
import gas.entities.*;
import mindustry.world.blocks.campaign.*;
import gas.world.blocks.defense.turrets.*;
import gas.world.blocks.distribution.*;
import gas.world.*;
import mindustry.type.*;
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
import arc.graphics.*;
import gas.*;
import gas.io.*;
import gas.world.draw.*;
import gas.gen.*;
import gas.world.blocks.storage.*;
import mindustry.world.blocks.units.*;
import gas.entities.bullets.*;
import mindustry.graphics.*;
import gas.world.blocks.production.*;
import mindustry.world.blocks.defense.*;
import mindustry.world.consumers.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;

public class GasThermalGenerator extends GasPowerGenerator {

    public Effect generateEffect = Fx.none;

    public float effectChance = 0.05f;

    public float minEfficiency = 0f;

    public float spinSpeed = 1f;

    public float displayEfficiencyScale = 1f;

    public boolean spinners = false;

    public boolean displayEfficiency = true;

    @Nullable
    public LiquidStack outputLiquid;

    public Attribute attribute = Attribute.heat;

    @Load("@-rotator")
    public TextureRegion rotatorRegion;

    @Load("@-rotator-blur")
    public TextureRegion blurRegion;

    public GasThermalGenerator(String name) {
        super(name);
        noUpdateDisabled = true;
    }

    @Override
    public void init() {
        if (outputLiquid != null) {
            outputsLiquid = true;
            hasLiquids = true;
        }
        super.init();
        // proper light clipping
        clipSize = Math.max(clipSize, 45f * size * 2f * 2f);
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.tiles, attribute, floating, size * size * displayEfficiencyScale, !displayEfficiency);
        stats.remove(generationType);
        stats.add(generationType, powerProduction * 60.0f / displayEfficiencyScale, StatUnit.powerSecond);
        if (outputLiquid != null) {
            stats.add(Stat.output, StatValues.liquid(outputLiquid.liquid, outputLiquid.amount * size * size * 60f, true));
        }
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        if (displayEfficiency) {
            drawPlaceText(Core.bundle.formatFloat("bar.efficiency", sumAttribute(attribute, x, y) * 100, 1), x, y, valid);
        }
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation) {
        // make sure there's heat at this location
        return tile.getLinkedTilesAs(this, tempTiles).sumf(other -> other.floor().attributes.get(attribute)) > minEfficiency;
    }

    @Override
    public TextureRegion[] icons() {
        return spinners ? new TextureRegion[] { region, rotatorRegion } : super.icons();
    }

    public class GasThermalGeneratorBuild extends GasGeneratorBuild {

        public float sum, spinRotation;

        @Override
        public void updateTile() {
            productionEfficiency = sum + attribute.env();
            if (productionEfficiency > 0.1f && Mathf.chanceDelta(effectChance)) {
                generateEffect.at(x + Mathf.range(3f), y + Mathf.range(3f));
            }
            spinRotation += productionEfficiency * spinSpeed;
            if (outputLiquid != null) {
                float added = Math.min(productionEfficiency * delta() * outputLiquid.amount, liquidCapacity - liquids.get(outputLiquid.liquid));
                liquids.add(outputLiquid.liquid, added);
                dumpLiquid(outputLiquid.liquid);
            }
        }

        @Override
        public void draw() {
            super.draw();
            if (spinners) {
                Drawf.spinSprite(blurRegion.found() && enabled && productionEfficiency > 0 ? blurRegion : rotatorRegion, x, y, spinRotation);
            }
        }

        @Override
        public void drawLight() {
            Drawf.light(x, y, (40f + Mathf.absin(10f, 5f)) * Math.min(productionEfficiency, 2f) * size, Color.scarlet, 0.4f);
        }

        @Override
        public void onProximityAdded() {
            super.onProximityAdded();
            sum = sumAttribute(attribute, tile.x, tile.y);
        }
    }
}
