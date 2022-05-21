package gas.world.blocks.units;

import gas.entities.comp.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import gas.content.*;
import mindustry.world.blocks.defense.turrets.*;
import gas.world.meta.*;
import mindustry.annotations.Annotations.*;
import gas.world.blocks.units.*;
import mindustry.world.blocks.heat.*;
import arc.util.*;
import mindustry.world.blocks.legacy.*;
import mindustry.gen.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.meta.*;
import gas.world.blocks.heat.*;
import gas.world.blocks.defense.*;
import gas.world.draw.*;
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
import mindustry.world.blocks.units.UnitCargoUnloadPoint.*;
import gas.world.blocks.gas.*;
import arc.scene.ui.layout.*;
import gas.world.blocks.campaign.*;
import mindustry.world.modules.*;
import gas.ui.*;
import mindustry.world.blocks.environment.*;
import mindustry.*;
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
import gas.world.blocks.payloads.*;
import gas.gen.*;
import gas.world.blocks.storage.*;
import mindustry.world.blocks.units.*;
import gas.world.blocks.production.*;
import arc.struct.*;
import arc.util.io.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import mindustry.world.blocks.power.*;
import mindustry.type.*;
import mindustry.world.blocks.sandbox.*;
import static mindustry.Vars.*;

public class GasUnitCargoUnloadPoint extends GasBlock {

    /**
     * If a block is full for this amount of time, it will not be flown to anymore.
     */
    public float staleTimeDuration = 60f * 6f;

    @Load("@-top")
    public TextureRegion topRegion;

    public GasUnitCargoUnloadPoint(String name) {
        super(name);
        update = solid = true;
        hasItems = true;
        configurable = true;
        saveConfig = true;
        clearOnDoubleTap = true;
        flags = EnumSet.of(BlockFlag.unitCargoUnloadPoint);
        config(Item.class, (GasUnitCargoUnloadPointBuild build, Item item) -> build.item = item);
        configClear((GasUnitCargoUnloadPointBuild build) -> build.item = null);
    }

    public class GasUnitCargoUnloadPointBuild extends GasBuilding {

        public Item item;

        public float staleTimer;

        public boolean stale;

        @Override
        public void draw() {
            super.draw();
            if (item != null) {
                Draw.color(item.color);
                Draw.rect(topRegion, x, y);
                Draw.color();
            }
        }

        @Override
        public void updateTile() {
            super.updateTile();
            if (items.total() < itemCapacity) {
                staleTimer = 0f;
                stale = false;
            }
            if (dumpAccumulate()) {
                staleTimer = 0f;
                stale = false;
            } else if (items.total() >= itemCapacity && (staleTimer += Time.delta) >= staleTimeDuration) {
                stale = true;
            }
        }

        @Override
        public int acceptStack(Item item, int amount, Teamc source) {
            return Math.min(itemCapacity - items.total(), amount);
        }

        @Override
        public void buildConfiguration(Table table) {
            ItemSelection.buildTable(GasUnitCargoUnloadPoint.this, table, content.items(), () -> item, this::configure);
        }

        @Override
        public Object config() {
            return item;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.s(item == null ? -1 : item.id);
            write.bool(stale);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            item = Vars.content.item(read.s());
            stale = read.bool();
        }
    }
}
