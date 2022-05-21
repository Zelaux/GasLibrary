package gas.world.blocks.production;

import gas.entities.comp.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import gas.content.*;
import mindustry.world.blocks.defense.turrets.*;
import gas.world.blocks.payloads.*;
import gas.io.*;
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
import mindustry.world.blocks.production.Pump.*;
import mindustry.world.blocks.logic.*;
import gas.world.modules.*;
import gas.world.blocks.*;
import arc.graphics.*;
import gas.*;
import arc.graphics.g2d.*;
import gas.world.draw.*;
import gas.gen.*;
import gas.world.blocks.storage.*;
import mindustry.world.blocks.units.*;
import gas.world.blocks.production.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import mindustry.logic.*;
import mindustry.world.blocks.power.*;
import mindustry.type.*;
import mindustry.world.blocks.sandbox.*;
import static mindustry.Vars.*;

public class GasPump extends GasLiquidBlock {

    /**
     * Pump amount per tile.
     */
    public float pumpAmount = 0.2f;

    /**
     * Interval in-between item consumptions, if applicable.
     */
    public float consumeTime = 60f * 5f;

    public DrawBlock drawer = new DrawMulti(new DrawDefault(), new DrawPumpLiquid());

    public GasPump(String name) {
        super(name);
        group = BlockGroup.liquids;
        floating = true;
        envEnabled = Env.terrestrial;
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.output, 60f * pumpAmount * size * size, StatUnit.liquidSecond);
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        Tile tile = world.tile(x, y);
        if (tile == null)
            return;
        float amount = 0f;
        Liquid liquidDrop = null;
        for (var other : tile.getLinkedTilesAs(this, tempTiles)) {
            if (canPump(other)) {
                if (liquidDrop != null && other.floor().liquidDrop != liquidDrop) {
                    liquidDrop = null;
                    break;
                }
                liquidDrop = other.floor().liquidDrop;
                amount += other.floor().liquidMultiplier;
            }
        }
        if (liquidDrop != null) {
            float width = drawPlaceText(Core.bundle.formatFloat("bar.pumpspeed", amount * pumpAmount * 60f, 0), x, y, valid);
            float dx = x * tilesize + offset - width / 2f - 4f, dy = y * tilesize + offset + size * tilesize / 2f + 5, s = iconSmall / 4f;
            Draw.mixcol(Color.darkGray, 1f);
            Draw.rect(liquidDrop.fullIcon, dx, dy - 1, s, s);
            Draw.reset();
            Draw.rect(liquidDrop.fullIcon, dx, dy, s, s);
        }
    }

    @Override
    public void load() {
        super.load();
        drawer.load(this);
    }

    @Override
    public TextureRegion[] icons() {
        return drawer.finalIcons(this);
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation) {
        if (isMultiblock()) {
            Liquid last = null;
            for (var other : tile.getLinkedTilesAs(this, tempTiles)) {
                if (other.floor().liquidDrop == null)
                    continue;
                if (other.floor().liquidDrop != last && last != null)
                    return false;
                last = other.floor().liquidDrop;
            }
            return last != null;
        } else {
            return canPump(tile);
        }
    }

    @Override
    public void setBars() {
        super.setBars();
        // replace dynamic output bar with own custom bar
        addLiquidBar((GasPumpBuild build) -> build.liquidDrop);
    }

    protected boolean canPump(Tile tile) {
        return tile != null && tile.floor().liquidDrop != null;
    }

    public class GasPumpBuild extends GasLiquidBuild {

        public float consTimer;

        public float amount = 0f;

        @Nullable
        public Liquid liquidDrop = null;

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
        public void pickedUp() {
            amount = 0f;
        }

        @Override
        public double sense(LAccess sensor) {
            if (sensor == LAccess.totalLiquids)
                return liquidDrop == null ? 0f : liquids.get(liquidDrop);
            return super.sense(sensor);
        }

        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
            amount = 0f;
            liquidDrop = null;
            for (var other : tile.getLinkedTiles(tempTiles)) {
                if (canPump(other)) {
                    liquidDrop = other.floor().liquidDrop;
                    amount += other.floor().liquidMultiplier;
                }
            }
        }

        @Override
        public boolean shouldConsume() {
            return liquidDrop != null && liquids.get(liquidDrop) < liquidCapacity - 0.01f && enabled;
        }

        @Override
        public void updateTile() {
            if (efficiency > 0 && liquidDrop != null) {
                float maxPump = Math.min(liquidCapacity - liquids.get(liquidDrop), amount * pumpAmount * edelta());
                liquids.add(liquidDrop, maxPump);
                // does nothing for most pumps, as those do not require items.
                if ((consTimer += delta()) >= consumeTime) {
                    consume();
                    consTimer = 0f;
                }
            }
            if (liquidDrop != null) {
                dumpLiquid(liquidDrop);
            }
        }
    }
}
