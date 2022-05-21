package gas.world.blocks.liquid;

import gas.entities.comp.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import gas.content.*;
import mindustry.world.blocks.defense.turrets.*;
import gas.world.blocks.payloads.*;
import gas.world.meta.*;
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
import arc.graphics.g2d.*;
import mindustry.world.blocks.logic.*;
import gas.world.modules.*;
import gas.world.blocks.*;
import gas.*;
import gas.io.*;
import gas.world.draw.*;
import gas.gen.*;
import gas.world.blocks.storage.*;
import mindustry.world.blocks.units.*;
import mindustry.world.blocks.liquid.LiquidJunction.*;
import gas.world.blocks.production.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import mindustry.world.blocks.power.*;
import mindustry.type.*;
import mindustry.world.blocks.sandbox.*;

// TODO should leak!
public class GasLiquidJunction extends GasLiquidBlock {

    public GasLiquidJunction(String name) {
        super(name);
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.remove(Stat.liquidCapacity);
    }

    @Override
    public void setBars() {
        super.setBars();
        removeBar("liquid");
    }

    @Override
    public TextureRegion[] icons() {
        return new TextureRegion[] { region };
    }

    public class GasLiquidJunctionBuild extends GasBuilding {

        @Override
        public void draw() {
            Draw.rect(region, x, y);
        }

        @Override
        public Building getLiquidDestination(Building source, Liquid liquid) {
            if (!enabled)
                return this;
            int dir = source.relativeTo(tile.x, tile.y);
            dir = (dir + 4) % 4;
            Building next = nearby(dir);
            if (next == null || (!next.acceptLiquid(this, liquid) && !(next.block instanceof GasLiquidJunction))) {
                return this;
            }
            return next.getLiquidDestination(this, liquid);
        }
    }
}
