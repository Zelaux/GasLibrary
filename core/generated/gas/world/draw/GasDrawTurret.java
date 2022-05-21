package gas.world.draw;

import gas.entities.comp.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import gas.content.*;
import mindustry.world.blocks.defense.turrets.*;
import gas.world.blocks.payloads.*;
import gas.io.*;
import gas.world.meta.*;
import gas.world.blocks.units.*;
import mindustry.world.blocks.heat.*;
import mindustry.entities.part.*;
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
import gas.world.blocks.defense.turrets.GasTurret.*;
import gas.world.blocks.gas.*;
import gas.world.blocks.campaign.*;
import mindustry.world.modules.*;
import gas.ui.*;
import mindustry.world.blocks.environment.*;
import mindustry.world.draw.DrawTurret.*;
import gas.world.consumers.*;
import mindustry.world.blocks.payloads.*;
import mindustry.world.blocks.*;
import gas.world.blocks.production.GasGenericCrafter.*;
import arc.*;
import mindustry.world.blocks.logic.*;
import gas.world.modules.*;
import gas.world.blocks.*;
import gas.*;
import arc.graphics.g2d.*;
import gas.world.draw.*;
import gas.gen.*;
import mindustry.world.blocks.defense.turrets.Turret.*;
import gas.world.blocks.storage.*;
import mindustry.world.blocks.units.*;
import mindustry.graphics.*;
import gas.world.blocks.production.*;
import arc.struct.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import mindustry.world.blocks.power.*;
import mindustry.type.*;
import mindustry.world.blocks.sandbox.*;

/**
 * Extend to implement custom drawing behavior for a turret.
 */
public class GasDrawTurret extends GasDrawBlock {

    protected static final Rand rand = new Rand();

    public Seq<DrawPart> parts = new Seq<>();

    /**
     * Prefix to use when loading base region.
     */
    public String basePrefix = "";

    /**
     * Overrides the liquid to draw in the liquid region.
     */
    @Nullable
    public Liquid liquidDraw;

    public TextureRegion base, liquid, top, heat, preview, outline;

    public GasDrawTurret(String basePrefix) {
        this.basePrefix = basePrefix;
    }

    public GasDrawTurret() {
    }

    @Override
    public void getRegionsToOutline(GasBlock block, Seq<TextureRegion> out) {
        for (var part : parts) {
            part.getOutlines(out);
        }
        if (block.region.found()) {
            out.add(block.region);
        }
    }

    @Override
    public void draw(GasBuilding build) {
        GasTurret turret = (GasTurret) build.block;
        GasTurretBuild tb = (GasTurretBuild) build;
        Draw.rect(base, build.x, build.y);
        Draw.color();
        Draw.z(Layer.turret - 0.02f);
        Drawf.shadow(preview, build.x + tb.recoilOffset.x - turret.elevation, build.y + tb.recoilOffset.y - turret.elevation, tb.drawrot());
        Draw.z(Layer.turret);
        drawTurret(turret, tb);
        drawHeat(turret, tb);
        if (parts.size > 0) {
            if (outline.found()) {
                // draw outline under everything when parts are involved
                Draw.z(Layer.turret - 0.01f);
                Draw.rect(outline, build.x + tb.recoilOffset.x, build.y + tb.recoilOffset.y, tb.drawrot());
                Draw.z(Layer.turret);
            }
            float progress = tb.visualReloadValid ? tb.progress() : 1f;
            // TODO no smooth reload
            var params = DrawPart.params.set(build.warmup(), 1f - progress, 1f - progress, tb.heat, tb.x + tb.recoilOffset.x, tb.y + tb.recoilOffset.y, tb.rotation);
            for (var part : parts) {
                part.draw(params);
            }
        }
    }

    public void drawTurret(GasTurret block, GasTurretBuild build) {
        if (block.region.found()) {
            Draw.rect(block.region, build.x + build.recoilOffset.x, build.y + build.recoilOffset.y, build.drawrot());
        }
        if (liquid.found()) {
            Liquid toDraw = liquidDraw == null ? build.liquids.current() : liquidDraw;
            Drawf.liquid(liquid, build.x + build.recoilOffset.x, build.y + build.recoilOffset.y, build.liquids.get(toDraw) / block.liquidCapacity, toDraw.color.write(Tmp.c1).a(1f), build.drawrot());
        }
        if (top.found()) {
            Draw.rect(top, build.x + build.recoilOffset.x, build.y + build.recoilOffset.y, build.drawrot());
        }
    }

    public void drawHeat(GasTurret block, GasTurretBuild build) {
        if (build.heat <= 0.00001f || !heat.found())
            return;
        Drawf.additive(heat, block.heatColor.write(Tmp.c1).a(build.heat), build.x + build.recoilOffset.x, build.y + build.recoilOffset.y, build.drawrot(), Layer.turretHeat);
    }

    /**
     * Load any relevant texture regions.
     */
    @Override
    public void load(GasBlock block) {
        if (!(block instanceof GasTurret))
            throw new ClassCastException("This drawer can only be used on turrets.");
        preview = Core.atlas.find(block.name + "-preview", block.region);
        outline = Core.atlas.find(block.name + "-outline");
        liquid = Core.atlas.find(block.name + "-liquid");
        top = Core.atlas.find(block.name + "-top");
        heat = Core.atlas.find(block.name + "-heat");
        base = Core.atlas.find(block.name + "-base");
        for (var part : parts) {
            part.turretShading = true;
            part.load(block.name);
        }
        // TODO test this for mods, e.g. exotic
        if (!base.found() && block.minfo.mod != null)
            base = Core.atlas.find(block.minfo.mod.name + "-" + basePrefix + "block-" + block.size);
        if (!base.found())
            base = Core.atlas.find(basePrefix + "block-" + block.size);
    }

    /**
     * @return the generated icons to be used for this block.
     */
    @Override
    public TextureRegion[] icons(GasBlock block) {
        return top.found() ? new TextureRegion[] { base, preview, top } : new TextureRegion[] { base, preview };
    }
}
