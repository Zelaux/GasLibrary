package gas.world.draw;

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
import mindustry.world.draw.DrawTurbines.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;

public class GasDrawTurbines extends GasDrawBlock {

    public TextureRegion[] turbines = new TextureRegion[2];

    public TextureRegion cap;

    public float turbineSpeed = 2f;

    @Override
    public void draw(GasBuilding build) {
        float totalTime = build.totalProgress();
        Draw.rect(turbines[0], build.x, build.y, totalTime * turbineSpeed);
        Draw.rect(turbines[1], build.x, build.y, -totalTime * turbineSpeed);
        if (cap.found()) {
            Draw.rect(cap, build.x, build.y);
        }
    }

    @Override
    public void load(GasBlock block) {
        super.load(block);
        cap = Core.atlas.find(block.name + "-cap");
        for (int i = 0; i < 2; i++) {
            turbines[i] = Core.atlas.find(block.name + "-turbine" + i);
        }
    }

    @Override
    public TextureRegion[] icons(GasBlock block) {
        return new TextureRegion[] { turbines[0], turbines[1], cap };
    }
}
