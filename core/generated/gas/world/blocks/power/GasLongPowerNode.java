package gas.world.blocks.power;

import gas.entities.comp.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import gas.content.*;
import mindustry.world.blocks.defense.turrets.*;
import gas.world.blocks.payloads.*;
import arc.*;
import gas.world.meta.*;
import mindustry.annotations.Annotations.*;
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
import mindustry.world.blocks.power.LongPowerNode.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;

public class GasLongPowerNode extends GasPowerNode {

    @Load("@-glow")
    public TextureRegion glow;

    public Color glowColor = Color.valueOf("cbfd81").a(0.45f);

    public float glowScl = 16f, glowMag = 0.6f;

    public GasLongPowerNode(String name) {
        super(name);
        drawRange = false;
    }

    @Override
    public void load() {
        super.load();
        laser = Core.atlas.find("power-beam");
        laserEnd = Core.atlas.find("power-beam-end");
    }

    public class GasLongPowerNodeBuild extends GasPowerNodeBuild {

        public float warmup = 0f;

        @Override
        public void updateTile() {
            super.updateTile();
            warmup = Mathf.lerpDelta(warmup, power.links.size > 0 ? 1f : 0f, 0.05f);
        }

        @Override
        public void draw() {
            super.draw();
            if (warmup > 0.001f) {
                Drawf.additive(glow, Tmp.c1.set(glowColor).mula(warmup).mula(1f - glowMag + Mathf.absin(glowScl, glowMag)), x, y);
            }
        }
    }
}
