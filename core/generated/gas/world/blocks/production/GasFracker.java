package gas.world.blocks.production;

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
import mindustry.world.blocks.logic.*;
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
import mindustry.world.blocks.production.Fracker.*;
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

public class GasFracker extends GasSolidPump {

    public float itemUseTime = 100f;

    public GasFracker(String name) {
        super(name);
        hasItems = true;
        ambientSound = Sounds.drill;
        ambientSoundVolume = 0.03f;
        envRequired |= Env.groundOil;
    }

    @Override
    public void setStats() {
        stats.timePeriod = itemUseTime;
        super.setStats();
        stats.add(Stat.productionTime, itemUseTime / 60f, StatUnit.seconds);
    }

    public class GasFrackerBuild extends GasSolidPumpBuild {

        public float accumulator;

        @Override
        public void updateTile() {
            if (efficiency > 0) {
                if (accumulator >= itemUseTime) {
                    consume();
                    accumulator -= itemUseTime;
                }
                super.updateTile();
                accumulator += delta() * efficiency;
            } else {
                warmup = Mathf.lerpDelta(warmup, 0f, 0.02f);
                lastPump = 0f;
                dumpLiquid(result);
            }
        }
    }
}
