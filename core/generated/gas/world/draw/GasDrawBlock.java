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
import mindustry.world.draw.DrawBlock.*;
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
import arc.struct.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;

/**
 * An implementation of custom rendering behavior for a crafter block.
 * This is used mostly for mods.
 */
public abstract class GasDrawBlock {

    protected static final Rand rand = new Rand();

    /**
     * If set, the icon is overridden to be these strings, in order. Each string is a suffix.
     */
    @Nullable
    public String[] iconOverride = null;

    public void getRegionsToOutline(GasBlock block, Seq<TextureRegion> out) {
    }

    /**
     * Draws the block itself.
     */
    public void draw(GasBuilding build) {
    }

    /**
     * Draws any extra light for the block.
     */
    public void drawLight(GasBuilding build) {
    }

    /**
     * Draws the planned version of this block.
     */
    public void drawPlan(GasBlock block, BuildPlan plan, Eachable<BuildPlan> list) {
    }

    /**
     * Load any relevant texture regions.
     */
    public void load(GasBlock block) {
    }

    /**
     * @return the generated icons to be used for this block.
     */
    public TextureRegion[] icons(GasBlock block) {
        return new TextureRegion[] { block.region };
    }

    public final TextureRegion[] finalIcons(GasBlock block) {
        if (iconOverride != null) {
            var out = new TextureRegion[iconOverride.length];
            for (int i = 0; i < out.length; i++) {
                out[i] = Core.atlas.find(block.name + iconOverride[i]);
            }
            return out;
        }
        return icons(block);
    }

    public GasGenericCrafter expectCrafter(GasBlock block) {
        if (!(block instanceof GasGenericCrafter crafter))
            throw new ClassCastException("This drawer requires the block to be a GenericCrafter. Use a different drawer.");
        return crafter;
    }
}
