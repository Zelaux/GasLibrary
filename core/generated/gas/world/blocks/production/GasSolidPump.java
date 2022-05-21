package gas.world.blocks.production;

import gas.entities.comp.*;
import mindustry.entities.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import mindustry.content.*;
import gas.content.*;
import mindustry.world.blocks.defense.turrets.*;
import gas.world.blocks.payloads.*;
import gas.io.*;
import gas.world.meta.*;
import mindustry.annotations.Annotations.*;
import gas.world.blocks.units.*;
import mindustry.world.blocks.heat.*;
import arc.util.*;
import mindustry.world.blocks.legacy.*;
import mindustry.gen.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import arc.math.*;
import mindustry.world.meta.*;
import gas.world.blocks.heat.*;
import gas.world.blocks.defense.*;
import gas.world.draw.*;
import mindustry.world.blocks.liquid.*;
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
import mindustry.world.modules.*;
import gas.world.blocks.gas.*;
import gas.world.blocks.campaign.*;
import mindustry.world.blocks.production.SolidPump.*;
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
import arc.graphics.g2d.*;
import mindustry.ui.*;
import gas.gen.*;
import gas.world.blocks.storage.*;
import mindustry.world.blocks.units.*;
import mindustry.graphics.*;
import gas.world.blocks.production.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import mindustry.world.blocks.power.*;
import mindustry.type.*;
import mindustry.world.blocks.sandbox.*;

/**
 * Pump that makes liquid from solids and takes in power. Only works on solid floor blocks.
 */
public class GasSolidPump extends GasPump {

    public Liquid result = Liquids.water;

    public Effect updateEffect = Fx.none;

    public float updateEffectChance = 0.02f;

    public float rotateSpeed = 1f;

    public float baseEfficiency = 1f;

    /**
     * Attribute that is checked when calculating output.
     */
    @Nullable
    public Attribute attribute;

    @Load("@-rotator")
    public TextureRegion rotatorRegion;

    public GasSolidPump(String name) {
        super(name);
        hasPower = true;
        // only supports ground by default
        envEnabled = Env.terrestrial;
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        drawPotentialLinks(x, y);
        if (attribute != null) {
            drawPlaceText(Core.bundle.format("bar.efficiency", Math.round(Math.max((sumAttribute(attribute, x, y)) / size / size + percentSolid(x, y) * baseEfficiency, 0f) * 100)), x, y, valid);
        }
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("efficiency", (GasSolidPumpBuild entity) -> new Bar(() -> Core.bundle.formatFloat("bar.pumpspeed", entity.lastPump * 60, 1), () -> Pal.ammo, () -> entity.warmup * entity.efficiency));
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.remove(Stat.output);
        stats.add(Stat.output, result, 60f * pumpAmount, true);
        if (attribute != null) {
            stats.add(baseEfficiency > 0.0001f ? Stat.affinities : Stat.tiles, attribute, floating, 1f, baseEfficiency <= 0.001f);
        }
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation) {
        float sum = tile.getLinkedTilesAs(this, tempTiles).sumf(t -> canPump(t) ? baseEfficiency + (attribute != null ? t.floor().attributes.get(attribute) : 0f) : 0f);
        return sum > 0.00001f;
    }

    @Override
    public boolean outputsItems() {
        return false;
    }

    @Override
    protected boolean canPump(Tile tile) {
        return tile != null && !tile.floor().isLiquid;
    }

    @Override
    public TextureRegion[] icons() {
        return new TextureRegion[] { region, rotatorRegion, topRegion };
    }

    public class GasSolidPumpBuild extends GasPumpBuild {

        public float warmup;

        public float pumpTime;

        public float boost;

        public float validTiles;

        public float lastPump;

        @Override
        public void drawCracks() {
        }

        @Override
        public void pickedUp() {
            boost = validTiles = 0f;
        }

        @Override
        public void draw() {
            Draw.rect(region, x, y);
            Draw.z(Layer.blockCracks);
            super.drawCracks();
            Draw.z(Layer.blockAfterCracks);
            Drawf.liquid(liquidRegion, x, y, liquids.get(result) / liquidCapacity, result.color);
            Drawf.spinSprite(rotatorRegion, x, y, pumpTime * rotateSpeed);
            Draw.rect(topRegion, x, y);
        }

        @Override
        public boolean shouldConsume() {
            return liquids.get(result) < liquidCapacity - 0.01f;
        }

        @Override
        public void updateTile() {
            liquidDrop = result;
            float fraction = Math.max(validTiles + boost + (attribute == null ? 0 : attribute.env()), 0);
            if (efficiency > 0 && typeLiquid() < liquidCapacity - 0.001f) {
                float maxPump = Math.min(liquidCapacity - typeLiquid(), pumpAmount * delta() * fraction * efficiency);
                liquids.add(result, maxPump);
                lastPump = maxPump / Time.delta;
                warmup = Mathf.lerpDelta(warmup, 1f, 0.02f);
                if (Mathf.chance(delta() * updateEffectChance))
                    updateEffect.at(x + Mathf.range(size * 2f), y + Mathf.range(size * 2f));
            } else {
                warmup = Mathf.lerpDelta(warmup, 0f, 0.02f);
                lastPump = 0f;
            }
            pumpTime += warmup * edelta();
            dumpLiquid(result);
        }

        @Override
        public void onProximityUpdate() {
            super.onProximityAdded();
            boost = sumAttribute(attribute, tile.x, tile.y) / size / size;
            validTiles = 0f;
            for (var other : tile.getLinkedTiles(tempTiles)) {
                if (canPump(other)) {
                    validTiles += baseEfficiency / (size * size);
                }
            }
        }

        public float typeLiquid() {
            return liquids.get(result);
        }
    }
}
