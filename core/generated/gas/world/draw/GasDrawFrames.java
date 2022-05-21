package gas.world.draw;

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
import arc.math.*;
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
import gas.world.consumers.*;
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
import gas.world.blocks.production.*;
import mindustry.world.draw.DrawFrames.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;

public class GasDrawFrames extends GasDrawBlock {

    /**
     * Number of frames to draw.
     */
    public int frames = 3;

    /**
     * Ticks between frames.
     */
    public float interval = 5f;

    /**
     * If true, frames wil alternate back and forth in a sine wave.
     */
    public boolean sine = true;

    public TextureRegion[] regions;

    @Override
    public void draw(GasBuilding build) {
        Draw.rect(sine ? regions[(int) Mathf.absin(build.totalProgress(), interval, frames - 0.001f)] : regions[(int) ((build.totalProgress() / interval) % frames)], build.x, build.y);
    }

    @Override
    public TextureRegion[] icons(GasBlock block) {
        return new TextureRegion[] { regions[0] };
    }

    @Override
    public void load(GasBlock block) {
        regions = new TextureRegion[frames];
        for (int i = 0; i < frames; i++) {
            regions[i] = Core.atlas.find(block.name + "-frame" + i);
        }
    }
}
