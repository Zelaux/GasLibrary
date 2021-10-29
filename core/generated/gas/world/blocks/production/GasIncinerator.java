package gas.world.blocks.production;

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
import arc.util.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import arc.math.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.meta.*;
import arc.graphics.*;
import gas.world.blocks.distribution.*;
import mindustry.gen.*;
import gas.world.blocks.payloads.*;
import mindustry.world.*;
import gas.world.blocks.production.*;
import mindustry.world.blocks.legacy.*;
import gas.world.blocks.liquid.*;
import mindustry.world.blocks.storage.*;
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
import mindustry.world.blocks.payloads.*;
import gas.world.blocks.sandbox.*;
import mindustry.world.blocks.*;
import gas.world.blocks.production.GasGenericCrafter.*;
import arc.graphics.g2d.*;
import mindustry.world.blocks.logic.*;
import gas.world.modules.*;
import gas.world.blocks.*;
import gas.*;
import mindustry.world.blocks.production.Incinerator.*;
import gas.io.*;
import gas.world.draw.*;
import mindustry.world.blocks.units.*;
import gas.content.*;
import gas.world.blocks.storage.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import gas.world.meta.values.*;
import mindustry.world.blocks.power.*;
import mindustry.type.*;
import mindustry.world.blocks.sandbox.*;
import gas.world.blocks.power.*;

public class GasIncinerator extends GasBlock {

    public Effect effect = Fx.fuelburn;

    public Color flameColor = Color.valueOf("ffad9d");

    public GasIncinerator(String name) {
        super(name);
        hasPower = true;
        hasLiquids = true;
        update = true;
        solid = true;
    }

    public class GasIncineratorBuild extends GasBuilding {

        public float heat;

        @Override
        public void updateTile() {
            heat = Mathf.approachDelta(heat, consValid() && efficiency() > 0.9f ? 1f : 0f, 0.04f);
        }

        @Override
        public BlockStatus status() {
            return heat > 0.5f ? BlockStatus.active : BlockStatus.noInput;
        }

        @Override
        public void draw() {
            super.draw();
            if (heat > 0f) {
                float g = 0.3f;
                float r = 0.06f;
                Draw.alpha(((1f - g) + Mathf.absin(Time.time, 8f, g) + Mathf.random(r) - r) * heat);
                Draw.tint(flameColor);
                Fill.circle(x, y, 2f);
                Draw.color(1f, 1f, 1f, heat);
                Fill.circle(x, y, 1f);
                Draw.color();
            }
        }

        @Override
        public void handleItem(Building source, Item item) {
            if (Mathf.chance(0.3)) {
                effect.at(x, y);
            }
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            return heat > 0.5f;
        }

        @Override
        public void handleLiquid(Building source, Liquid liquid, float amount) {
            if (Mathf.chance(0.02)) {
                effect.at(x, y);
            }
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid) {
            return heat > 0.5f;
        }
    }
}
