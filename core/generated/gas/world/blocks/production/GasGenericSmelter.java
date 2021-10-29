package gas.world.blocks.production;

import gas.entities.comp.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.experimental.*;
import mindustry.world.blocks.production.GenericSmelter.*;
import gas.world.meta.*;
import mindustry.annotations.Annotations.*;
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
import gas.io.*;
import gas.world.draw.*;
import mindustry.world.blocks.units.*;
import gas.content.*;
import gas.world.blocks.storage.*;
import mindustry.graphics.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import gas.world.meta.values.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;
import gas.world.blocks.power.*;

/**
 * @deprecated this class has no new functionality over GenericCrafter, use GenericCrafter with a DrawSmelter drawer instead. See vanilla smelter blocks.
 */
@Deprecated
public class GasGenericSmelter extends GasGenericCrafter {

    public Color flameColor = Color.valueOf("ffc999");

    @Load("@-top")
    public TextureRegion topRegion;

    public float flameRadius = 3f, flameRadiusIn = 1.9f, flameRadiusScl = 5f, flameRadiusMag = 2f, flameRadiusInMag = 1f;

    public GasGenericSmelter(String name) {
        super(name);
        ambientSound = Sounds.smelter;
        ambientSoundVolume = 0.07f;
    }

    public class GasSmelterBuild extends GasGenericCrafterBuild {

        @Override
        public void draw() {
            super.draw();
            // draw glowing center
            if (warmup > 0f && flameColor.a > 0.001f) {
                float g = 0.3f;
                float r = 0.06f;
                float cr = Mathf.random(0.1f);
                Draw.z(Layer.block + 0.01f);
                Draw.alpha(((1f - g) + Mathf.absin(Time.time, 8f, g) + Mathf.random(r) - r) * warmup);
                Draw.tint(flameColor);
                Fill.circle(x, y, flameRadius + Mathf.absin(Time.time, flameRadiusScl, flameRadiusMag) + cr);
                Draw.color(1f, 1f, 1f, warmup);
                Draw.rect(topRegion, x, y);
                Fill.circle(x, y, flameRadiusIn + Mathf.absin(Time.time, flameRadiusScl, flameRadiusInMag) + cr);
                Draw.color();
            }
        }

        @Override
        public void drawLight() {
            Drawf.light(team, x, y, (60f + Mathf.absin(10f, 5f)) * warmup * size, flameColor, 0.65f);
        }
    }
}
