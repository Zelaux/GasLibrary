package gas.world.blocks.logic;

import gas.entities.comp.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.experimental.*;
import gas.world.meta.*;
import gas.world.blocks.units.*;
import gas.world.blocks.defense.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.meta.*;
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
import mindustry.world.blocks.logic.MemoryBlock.*;
import mindustry.world.blocks.logic.*;
import gas.world.modules.*;
import gas.world.blocks.*;
import gas.*;
import gas.io.*;
import gas.world.draw.*;
import mindustry.world.blocks.units.*;
import gas.content.*;
import gas.world.blocks.storage.*;
import arc.util.io.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import gas.world.meta.values.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;
import gas.world.blocks.power.*;

public class GasMemoryBlock extends GasBlock {

    public int memoryCapacity = 32;

    public GasMemoryBlock(String name) {
        super(name);
        destructible = true;
        solid = true;
        group = BlockGroup.logic;
        drawDisabled = false;
        envEnabled = Env.any;
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.memoryCapacity, memoryCapacity, StatUnit.none);
    }

    public class GasMemoryBuild extends GasBuilding {

        public double[] memory = new double[memoryCapacity];

        // massive byte size means picking up causes sync issues
        @Override
        public boolean canPickup() {
            return false;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.i(memory.length);
            for (var v : memory) {
                write.d(v);
            }
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            int amount = read.i();
            for (int i = 0; i < amount; i++) {
                double val = read.d();
                if (i < memory.length)
                    memory[i] = val;
            }
        }
    }
}
