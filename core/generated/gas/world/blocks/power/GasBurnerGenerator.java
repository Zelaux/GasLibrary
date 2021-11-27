package gas.world.blocks.power;

import gas.entities.comp.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.experimental.*;
import gas.world.meta.*;
import mindustry.annotations.Annotations.*;
import gas.world.blocks.units.*;
import gas.world.blocks.defense.*;
import mindustry.world.blocks.legacy.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.meta.*;
import gas.world.blocks.distribution.*;
import gas.world.draw.*;
import mindustry.world.blocks.logic.*;
import mindustry.gen.*;
import gas.world.blocks.power.*;
import mindustry.world.*;
import gas.world.blocks.sandbox.*;
import mindustry.world.blocks.storage.*;
import gas.world.blocks.liquid.*;
import gas.entities.*;
import mindustry.world.blocks.campaign.*;
import gas.world.blocks.defense.turrets.*;
import gas.gen.*;
import gas.world.*;
import mindustry.type.*;
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
import mindustry.world.consumers.*;
import gas.world.modules.*;
import gas.world.blocks.*;
import gas.*;
import gas.io.*;
import gas.world.blocks.payloads.*;
import mindustry.world.blocks.units.*;
import gas.content.*;
import mindustry.world.blocks.power.BurnerGenerator.*;
import gas.world.blocks.storage.*;
import mindustry.graphics.*;
import gas.world.blocks.production.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import gas.world.meta.values.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;

public class GasBurnerGenerator extends GasItemLiquidGenerator {

    @Load(value = "@-turbine#", length = 2)
    public TextureRegion[] turbineRegions;

    @Load("@-cap")
    public TextureRegion capRegion;

    public float turbineSpeed = 2f;

    public GasBurnerGenerator(String name) {
        super(true, false, name);
    }

    @Override
    protected float getLiquidEfficiency(Liquid liquid) {
        return liquid.flammability;
    }

    @Override
    protected float getItemEfficiency(Item item) {
        return item.flammability;
    }

    @Override
    public TextureRegion[] icons() {
        return turbineRegions[0].found() ? new TextureRegion[] { region, turbineRegions[0], turbineRegions[1], capRegion } : super.icons();
    }

    public class GasBurnerGeneratorBuild extends GasItemLiquidGeneratorBuild {

        @Override
        public void draw() {
            super.draw();
            if (turbineRegions[0].found()) {
                Draw.rect(turbineRegions[0], x, y, totalTime * turbineSpeed);
                Draw.rect(turbineRegions[1], x, y, -totalTime * turbineSpeed);
                Draw.rect(capRegion, x, y);
            }
            if (hasLiquids && liquidRegion.found()) {
                Drawf.liquid(liquidRegion, x, y, liquids.total() / liquidCapacity, liquids.current().color);
            }
        }
    }
}
