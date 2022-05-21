package gas.world.blocks.production;

import gas.entities.comp.*;
import mindustry.entities.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import mindustry.content.*;
import gas.content.*;
import mindustry.type.*;
import gas.world.blocks.payloads.*;
import gas.world.meta.*;
import mindustry.annotations.Annotations.*;
import gas.world.blocks.units.*;
import mindustry.world.blocks.heat.*;
import mindustry.world.blocks.legacy.*;
import mindustry.gen.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import arc.math.*;
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
import mindustry.world.blocks.defense.turrets.*;
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
import mindustry.graphics.*;
import gas.world.blocks.production.*;
import mindustry.world.blocks.production.ItemIncinerator.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;

/**
 * Incinerator that accepts only items and optionally requires a liquid, e.g. slag.
 */
public class GasItemIncinerator extends GasBlock {

    public Effect effect = Fx.incinerateSlag;

    public float effectChance = 0.2f;

    @Load("@-liquid")
    public TextureRegion liquidRegion;

    @Load("@-top")
    public TextureRegion topRegion;

    public GasItemIncinerator(String name) {
        super(name);
        update = true;
        solid = true;
    }

    @Override
    public TextureRegion[] icons() {
        return new TextureRegion[] { region, topRegion };
    }

    public class GasItemIncineratorBuild extends GasBuilding {

        @Override
        public void updateTile() {
        }

        @Override
        public BlockStatus status() {
            return efficiency > 0 ? BlockStatus.active : BlockStatus.noInput;
        }

        @Override
        public void draw() {
            super.draw();
            if (liquidRegion.found()) {
                Drawf.liquid(liquidRegion, x, y, liquids.currentAmount() / liquidCapacity, liquids.current().color);
            }
            if (topRegion.found()) {
                Draw.rect(topRegion, x, y);
            }
        }

        @Override
        public void handleItem(Building source, Item item) {
            if (Mathf.chance(effectChance)) {
                effect.at(x, y);
            }
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            return efficiency > 0;
        }
    }
}
