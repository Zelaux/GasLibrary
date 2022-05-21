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
import gas.world.blocks.production.*;
import mindustry.world.blocks.liquid.LiquidRouter.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import mindustry.world.blocks.power.*;
import mindustry.type.*;
import mindustry.world.blocks.sandbox.*;

public class GasLiquidRouter extends GasLiquidBlock {

    public float liquidPadding = 0f;

    public GasLiquidRouter(String name) {
        super(name);
        underBullets = true;
        solid = false;
        noUpdateDisabled = true;
        canOverdrive = false;
    }

    @Override
    public TextureRegion[] icons() {
        return new TextureRegion[] { bottomRegion, region };
    }

    public class GasLiquidRouterBuild extends GasLiquidBuild {

        @Override
        public void updateTile() {
            if (liquids.currentAmount() > 0.01f) {
                dumpLiquid(liquids.current());
            }
        }

        @Override
        public void draw() {
            Draw.rect(bottomRegion, x, y);
            if (liquids.currentAmount() > 0.001f) {
                drawTiledFrames(size, x, y, liquidPadding, liquids.current(), liquids.currentAmount() / liquidCapacity);
            }
            Draw.rect(region, x, y);
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid) {
            return (liquids.current() == liquid || liquids.currentAmount() < 0.2f);
        }
    }
}
