package gas.world.blocks.production;

import gas.entities.comp.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.experimental.*;
import gas.world.meta.*;
import mindustry.annotations.Annotations.*;
import gas.world.blocks.units.*;
import gas.world.blocks.defense.*;
import mindustry.world.blocks.legacy.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import arc.math.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.meta.*;
import arc.graphics.*;
import gas.world.blocks.distribution.*;
import gas.world.draw.*;
import mindustry.world.blocks.logic.*;
import mindustry.gen.*;
import gas.world.blocks.power.*;
import mindustry.world.*;
import gas.world.blocks.sandbox.*;
import mindustry.world.blocks.storage.*;
import gas.world.blocks.liquid.*;
import gas.entities.*;
import mindustry.world.blocks.campaign.*;
import gas.gen.*;
import gas.world.*;
import gas.world.blocks.defense.turrets.*;
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
import mindustry.world.consumers.*;
import gas.world.modules.*;
import gas.world.blocks.*;
import gas.*;
import gas.io.*;
import gas.world.blocks.payloads.*;
import mindustry.world.blocks.units.*;
import gas.content.*;
import gas.world.blocks.storage.*;
import mindustry.world.blocks.production.Cultivator.*;
import gas.world.blocks.production.*;
import arc.util.io.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import gas.world.meta.values.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;

/**
 * @deprecated use GenericCrafter or AttributeCrafter with a DrawCultivator instead.
 * WARNING: If you switch from a class that used Cultivator to a GenericCrafter, make sure you set legacyReadWarmup to true! Failing to do so will break saves.
 * This class has been gutted of its behavior.
 */
@Deprecated
public class GasCultivator extends GasGenericCrafter {

    // fields are kept for compatibility
    public Color plantColor = Color.valueOf("5541b1");

    public Color plantColorLight = Color.valueOf("7457ce");

    public Color bottomColor = Color.valueOf("474747");

    @Load("@-middle")
    public TextureRegion middleRegion;

    @Load("@-top")
    public TextureRegion topRegion;

    public Rand random = new Rand(0);

    public float recurrence = 6f;

    public Attribute attribute = Attribute.spores;

    public GasCultivator(String name) {
        super(name);
    }

    public class GasCultivatorBuild extends GasGenericCrafterBuild {

        // compat
        public float warmup, boost;

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(warmup);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            warmup = read.f();
        }
    }
}
