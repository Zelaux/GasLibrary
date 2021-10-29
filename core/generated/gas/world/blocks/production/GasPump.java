package gas.world.blocks.production;

import gas.entities.comp.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.experimental.*;
import gas.world.meta.*;
import gas.world.blocks.units.*;
import gas.world.blocks.defense.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.meta.*;
import arc.graphics.*;
import gas.world.blocks.distribution.*;
import mindustry.world.blocks.payloads.*;
import mindustry.gen.*;
import gas.world.blocks.payloads.*;
import mindustry.world.*;
import gas.world.blocks.production.*;
import mindustry.world.blocks.legacy.*;
import gas.world.blocks.liquid.*;
import mindustry.world.blocks.storage.*;
import mindustry.game.*;
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
import arc.graphics.g2d.*;
import gas.world.blocks.sandbox.*;
import mindustry.world.blocks.*;
import gas.world.blocks.production.GasGenericCrafter.*;
import arc.*;
import mindustry.world.blocks.production.Pump.*;
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
import mindustry.type.*;
import mindustry.world.blocks.sandbox.*;
import gas.world.blocks.power.*;
import static mindustry.Vars.*;

public class GasPump extends GasLiquidBlock {

    /**
     * Pump amount per tile.
     */
    public float pumpAmount = 0.2f;

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
    public TextureRegion[] icons() {
        return new TextureRegion[] { region };
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

    protected boolean canPump(Tile tile) {
        return tile != null && tile.floor().liquidDrop != null;
    }

    public class GasPumpBuild extends GasLiquidBuild {

        public float amount = 0f;

        public Liquid liquidDrop = null;

        @Override
        public void draw() {
            Draw.rect(name, x, y);
            Drawf.liquid(liquidRegion, x, y, liquids.currentAmount() / liquidCapacity, liquids.current().color);
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
            if (consValid() && liquidDrop != null) {
                float maxPump = Math.min(liquidCapacity - liquids.total(), amount * pumpAmount * edelta());
                liquids.add(liquidDrop, maxPump);
            }
            dumpLiquid(liquids.current());
        }
    }
}
