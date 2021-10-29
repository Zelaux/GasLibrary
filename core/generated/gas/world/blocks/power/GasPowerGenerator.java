package gas.world.blocks.power;

import gas.entities.comp.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import mindustry.world.blocks.defense.turrets.*;
import gas.world.blocks.payloads.*;
import mindustry.world.blocks.experimental.*;
import gas.world.meta.*;
import mindustry.ui.*;
import gas.world.blocks.units.*;
import gas.world.blocks.defense.*;
import arc.util.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import arc.math.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.meta.*;
import gas.world.blocks.distribution.*;
import mindustry.gen.*;
import mindustry.world.blocks.power.PowerGenerator.*;
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
import arc.*;
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
import arc.struct.*;
import arc.util.io.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import gas.world.meta.values.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;
import gas.world.blocks.power.*;

public class GasPowerGenerator extends GasPowerDistributor {

    /**
     * The amount of power produced per tick in case of an efficiency of 1.0, which represents 100%.
     */
    public float powerProduction;

    public Stat generationType = Stat.basePowerGeneration;

    public GasPowerGenerator(String name) {
        super(name);
        sync = true;
        baseExplosiveness = 5f;
        flags = EnumSet.of(BlockFlag.generator);
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(generationType, powerProduction * 60.0f, StatUnit.powerSecond);
    }

    @Override
    public void setBars() {
        super.setBars();
        if (hasPower && outputsPower && !consumes.hasPower()) {
            bars.add("power", (GasGeneratorBuild entity) -> new Bar(() -> Core.bundle.format("bar.poweroutput", Strings.fixed(entity.getPowerProduction() * 60 * entity.timeScale(), 1)), () -> Pal.powerBar, () -> entity.productionEfficiency));
        }
    }

    @Override
    public boolean outputsItems() {
        return false;
    }

    public class GasGeneratorBuild extends GasBuilding {

        public float generateTime;

        /**
         * The efficiency of the producer. An efficiency of 1.0 means 100%
         */
        public float productionEfficiency = 0.0f;

        @Override
        public float ambientVolume() {
            return Mathf.clamp(productionEfficiency);
        }

        @Override
        public float getPowerProduction() {
            return powerProduction * productionEfficiency;
        }

        @Override
        public byte version() {
            return 1;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(productionEfficiency);
            write.f(generateTime);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            productionEfficiency = read.f();
            if (revision >= 1) {
                generateTime = read.f();
            }
        }
    }
}
