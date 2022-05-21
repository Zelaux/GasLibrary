package gas.world.blocks.power;

import gas.entities.comp.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import gas.content.*;
import mindustry.world.blocks.defense.turrets.*;
import gas.world.blocks.payloads.*;
import mindustry.world.blocks.power.SolarGenerator.*;
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
import static mindustry.Vars.*;

public class GasSolarGenerator extends GasPowerGenerator {

    public GasSolarGenerator(String name) {
        super(name);
        // remove the BlockFlag.generator flag to make this a lower priority target than other generators.
        flags = EnumSet.of();
        envEnabled = Env.any;
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.remove(generationType);
        stats.add(generationType, powerProduction * 60.0f, StatUnit.powerSecond);
    }

    public class GasSolarGeneratorBuild extends GasGeneratorBuild {

        @Override
        public void updateTile() {
            productionEfficiency = enabled ? Mathf.maxZero(Attribute.light.env() + (state.rules.lighting ? 1f - state.rules.ambientLight.a : 1f)) : 0f;
        }
    }
}
