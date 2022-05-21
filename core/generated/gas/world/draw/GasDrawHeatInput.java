package gas.world.draw;

import mindustry.entities.units.*;
import gas.entities.comp.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import gas.content.*;
import mindustry.world.blocks.defense.turrets.*;
import gas.world.blocks.payloads.*;
import arc.*;
import gas.world.meta.*;
import gas.world.blocks.units.*;
import mindustry.world.blocks.heat.*;
import arc.util.*;
import mindustry.world.blocks.legacy.*;
import mindustry.gen.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import arc.math.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.meta.*;
import gas.world.blocks.heat.*;
import gas.world.blocks.defense.*;
import mindustry.world.draw.DrawHeatInput.*;
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
import arc.graphics.*;
import gas.*;
import gas.io.*;
import gas.world.draw.*;
import gas.gen.*;
import gas.world.blocks.storage.*;
import mindustry.world.blocks.units.*;
import mindustry.graphics.*;
import gas.world.blocks.production.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;

/**
 * Not standalone.
 */
public class GasDrawHeatInput extends GasDrawBlock {

    public String suffix = "-heat";

    public Color heatColor = new Color(1f, 0.22f, 0.22f, 0.8f);

    public float heatPulse = 0.3f, heatPulseScl = 10f;

    public TextureRegion heat;

    public GasDrawHeatInput(String suffix) {
        this.suffix = suffix;
    }

    public GasDrawHeatInput() {
    }

    @Override
    public void drawPlan(GasBlock block, BuildPlan plan, Eachable<BuildPlan> list) {
    }

    @Override
    public void draw(GasBuilding build) {
        Draw.z(Layer.blockAdditive);
        if (build instanceof HeatConsumer hc) {
            float[] side = hc.sideHeat();
            for (int i = 0; i < 4; i++) {
                if (side[i] > 0) {
                    Draw.blend(Blending.additive);
                    Draw.color(heatColor, side[i] / hc.heatRequirement() * (heatColor.a * (1f - heatPulse + Mathf.absin(heatPulseScl, heatPulse))));
                    Draw.rect(heat, build.x, build.y, i * 90f);
                    Draw.blend();
                    Draw.color();
                }
            }
        }
        Draw.z(Layer.block);
    }

    @Override
    public void load(GasBlock block) {
        heat = Core.atlas.find(block.name + suffix);
    }
}
