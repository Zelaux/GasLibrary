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
import mindustry.world.draw.DrawPistons.*;
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
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;

public class GasDrawPistons extends GasDrawBlock {

    public float sinMag = 4f, sinScl = 6f, sinOffset = 50f, sideOffset = 0f, lenOffset = -1f;

    public int sides = 4;

    public TextureRegion region1, region2, regiont;

    @Override
    public void drawPlan(GasBlock block, BuildPlan plan, Eachable<BuildPlan> list) {
    }

    @Override
    public void draw(GasBuilding build) {
        for (int i = 0; i < sides; i++) {
            float len = Mathf.absin(build.totalProgress() + sinOffset + sideOffset * sinScl * i, sinScl, sinMag) + lenOffset;
            float angle = i * 360f / sides;
            TextureRegion reg = regiont.found() && (Mathf.equal(angle, 315) || Mathf.equal(angle, 135)) ? regiont : angle >= 135 && angle < 315 ? region2 : region1;
            if (Mathf.equal(angle, 315)) {
                Draw.yscl = -1f;
            }
            Draw.rect(reg, build.x + Angles.trnsx(angle, len), build.y + Angles.trnsy(angle, len), angle);
            Draw.yscl = 1f;
        }
    }

    @Override
    public void load(GasBlock block) {
        super.load(block);
        region1 = Core.atlas.find(block.name + "-piston0", block.name + "-piston");
        region2 = Core.atlas.find(block.name + "-piston1", block.name + "-piston");
        regiont = Core.atlas.find(block.name + "-piston-t");
    }
}
