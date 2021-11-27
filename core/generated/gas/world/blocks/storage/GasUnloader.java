package gas.world.blocks.storage;

import mindustry.entities.units.*;
import gas.entities.comp.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import gas.content.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.experimental.*;
import gas.world.meta.*;
import gas.world.blocks.units.*;
import gas.world.*;
import arc.util.*;
import mindustry.world.blocks.legacy.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
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
import mindustry.world.blocks.storage.Unloader.*;
import gas.world.blocks.defense.turrets.*;
import gas.world.blocks.gas.*;
import arc.scene.ui.layout.*;
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
import gas.world.blocks.defense.*;
import gas.world.blocks.storage.*;
import gas.world.blocks.production.*;
import arc.util.io.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import gas.world.meta.values.*;
import mindustry.world.blocks.power.*;
import mindustry.type.*;
import mindustry.world.blocks.sandbox.*;
import static mindustry.Vars.*;

public class GasUnloader extends GasBlock {

    public float speed = 1f;

    public GasUnloader(String name) {
        super(name);
        update = true;
        solid = true;
        health = 70;
        hasItems = true;
        configurable = true;
        saveConfig = true;
        itemCapacity = 0;
        noUpdateDisabled = true;
        unloadable = false;
        envEnabled = Env.any;
        config(Item.class, (GasUnloaderBuild tile, Item item) -> tile.sortItem = item);
        configClear((GasUnloaderBuild tile) -> tile.sortItem = null);
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.speed, 60f / speed, StatUnit.itemsSecond);
    }

    @Override
    public void drawRequestConfig(BuildPlan req, Eachable<BuildPlan> list) {
        drawRequestConfigCenter(req, req.config, "unloader-center");
    }

    @Override
    public void setBars() {
        super.setBars();
        bars.remove("items");
    }

    public class GasUnloaderBuild extends GasBuilding {

        public float unloadTimer = 0f;

        public Item sortItem = null;

        public Building dumpingTo;

        public int offset = 0;

        public int[] rotations;

        @Override
        public void updateTile() {
            if ((unloadTimer += delta()) >= speed) {
                boolean any = false;
                if (rotations == null || rotations.length != proximity.size) {
                    rotations = new int[proximity.size];
                }
                for (int i = 0; i < proximity.size; i++) {
                    int pos = (offset + i) % proximity.size;
                    var other = proximity.get(pos);
                    if (other.interactable(team) && other.block.unloadable && other.canUnload() && other.block.hasItems && ((sortItem == null && other.items.total() > 0) || (sortItem != null && other.items.has(sortItem)))) {
                        // make sure the item can't be dumped back into this block
                        dumpingTo = other;
                        // get item to be taken
                        Item item = sortItem == null ? other.items.takeIndex(rotations[pos]) : sortItem;
                        // remove item if it's dumped correctly
                        if (put(item)) {
                            other.items.remove(item, 1);
                            any = true;
                            if (sortItem == null) {
                                rotations[pos] = item.id + 1;
                            }
                            other.itemTaken(item);
                        } else if (sortItem == null) {
                            rotations[pos] = other.items.nextIndex(rotations[pos]);
                        }
                    }
                }
                if (any) {
                    unloadTimer %= speed;
                } else {
                    unloadTimer = Math.min(unloadTimer, speed);
                }
                if (proximity.size > 0) {
                    offset++;
                    offset %= proximity.size;
                }
            }
        }

        @Override
        public void draw() {
            super.draw();
            Draw.color(sortItem == null ? Color.clear : sortItem.color);
            Draw.rect("unloader-center", x, y);
            Draw.color();
        }

        @Override
        public void buildConfiguration(Table table) {
            ItemSelection.buildTable(table, content.items(), () -> sortItem, this::configure);
        }

        @Override
        public boolean onConfigureTileTapped(Building other) {
            if (this == other) {
                deselect();
                configure(null);
                return false;
            }
            return true;
        }

        @Override
        public boolean canDump(Building to, Item item) {
            return !(to.block instanceof StorageBlock) && to != dumpingTo;
        }

        @Override
        public Item config() {
            return sortItem;
        }

        @Override
        public byte version() {
            return 1;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.s(sortItem == null ? -1 : sortItem.id);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            int id = revision == 1 ? read.s() : read.b();
            sortItem = id == -1 ? null : content.items().get(id);
        }
    }
}
