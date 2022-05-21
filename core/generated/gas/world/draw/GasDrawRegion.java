package gas.world.draw;

import mindustry.entities.units.*;
import gas.entities.comp.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import gas.content.*;
import mindustry.world.blocks.defense.turrets.*;
import gas.world.blocks.payloads.*;
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
import mindustry.world.blocks.payloads.*;
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
import mindustry.world.draw.DrawRegion.*;
import arc.graphics.g2d.*;
import mindustry.world.blocks.*;
import gas.world.blocks.production.GasGenericCrafter.*;
import arc.*;
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
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;
import gas.world.consumers.*;

/**
 * Not standalone.
 */
public class GasDrawRegion extends GasDrawBlock {

    public TextureRegion region;

    public String suffix = "";

    public boolean spinSprite = false;

    public boolean drawPlan = true;

    public float rotateSpeed, x, y;

    /**
     * Any number <=0 disables layer changes.
     */
    public float layer = -1;

    public GasDrawRegion(String suffix) {
        this.suffix = suffix;
    }

    public GasDrawRegion() {
    }

    @Override
    public void draw(GasBuilding build) {
        float z = Draw.z();
        if (layer > 0)
            Draw.z(layer);
        if (spinSprite) {
            Drawf.spinSprite(region, build.x + x, build.y + y, build.totalProgress() * rotateSpeed);
        } else {
            Draw.rect(region, build.x + x, build.y + y, build.totalProgress() * rotateSpeed);
        }
        Draw.z(z);
    }

    @Override
    public void drawPlan(GasBlock block, BuildPlan plan, Eachable<BuildPlan> list) {
        if (!drawPlan)
            return;
        Draw.rect(region, plan.drawx(), plan.drawy());
    }

    @Override
    public TextureRegion[] icons(GasBlock block) {
        return new TextureRegion[] { region };
    }

    @Override
    public void load(GasBlock block) {
        region = Core.atlas.find(block.name + suffix);
    }
}
