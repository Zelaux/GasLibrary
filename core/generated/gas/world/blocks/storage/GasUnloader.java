package gas.world.blocks.storage;

import mindustry.entities.units.*;
import gas.entities.comp.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import gas.content.*;
import mindustry.type.*;
import gas.world.blocks.payloads.*;
import gas.world.meta.*;
import mindustry.annotations.Annotations.*;
import gas.world.blocks.units.*;
import mindustry.world.blocks.heat.*;
import java.util.*;
import arc.util.*;
import mindustry.world.blocks.legacy.*;
import mindustry.gen.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
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
import mindustry.world.blocks.defense.turrets.*;
import gas.world.blocks.gas.*;
import arc.scene.ui.layout.*;
import gas.world.blocks.campaign.*;
import mindustry.world.blocks.storage.Unloader.*;
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
import gas.world.blocks.production.*;
import arc.struct.*;
import arc.util.io.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;
import mindustry.world.modules.*;
import static mindustry.Vars.*;

public class GasUnloader extends GasBlock {

    @Load(value = "@-center", fallback = "unloader-center")
    public TextureRegion centerRegion;

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
        clearOnDoubleTap = true;
        unloadable = false;
        config(Item.class, (GasUnloaderBuild tile, Item item) -> tile.sortItem = item);
        configClear((GasUnloaderBuild tile) -> tile.sortItem = null);
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.speed, 60f / speed, StatUnit.itemsSecond);
    }

    @Override
    public void drawPlanConfig(BuildPlan plan, Eachable<BuildPlan> list) {
        drawPlanConfigCenter(plan, plan.config, "unloader-center");
    }

    @Override
    public void setBars() {
        super.setBars();
        removeBar("items");
    }

    public static class GasContainerStat{
        Building building;
        float loadFactor;
        boolean canLoad;
        boolean canUnload;
        int index;

        @Override
        public String toString(){
            return "ContainerStat{" +
            "building=" + building.block + "#" + building.id +
            ", loadFactor=" + loadFactor +
            ", canLoad=" + canLoad +
            ", canUnload=" + canUnload +
            ", index=" + index +
            '}';
        }
    }
    public class GasUnloaderBuild extends GasBuilding {

        public float unloadTimer = 0f;

        public Item sortItem = null;

        public int offset = 0;

        public int rotations = 0;

        public Seq<GasContainerStat> possibleBlocks = new Seq<>();

        public int[] lastUsed;

        protected final Comparator<GasContainerStat> comparator = Structs.comps(// sort so it gives priority for blocks that can only either recieve or give (not both), and then by load, and then by last use
        // highest = unload from, lowest = unload to
        Structs.comps(// stackConveyors and Storage
        Structs.comparingBool(e -> e.building.block.highUnloadPriority && !e.canLoad), Structs.comps(// priority to give
        Structs.comparingBool(e -> e.canUnload && !e.canLoad), // priority to receive
        Structs.comparingBool(e -> e.canUnload || !e.canLoad))), Structs.comps(Structs.comparingFloat(e -> e.loadFactor), Structs.comparingInt(e -> -lastUsed[e.index])));

        @Override
        public void updateTile() {
            if (((unloadTimer += delta()) < speed) || (proximity.size < 2))
                return;
            Item item = null;
            boolean any = false;
            int itemslength = content.items().size;
            // initialize possibleBlocks only if the new size is bigger than the previous, to avoid unnecessary allocations
            if (possibleBlocks.size != proximity.size) {
                int tmp = possibleBlocks.size;
                possibleBlocks.setSize(proximity.size);
                for (int i = tmp; i < proximity.size; i++) {
                    possibleBlocks.set(i, new GasContainerStat());
                }
                lastUsed = new int[proximity.size];
            }
            if (sortItem != null) {
                item = sortItem;
                for (int pos = 0; pos < proximity.size; pos++) {
                    var other = proximity.get(pos);
                    boolean interactable = other.interactable(team);
                    // set the stats of all buildings in possibleBlocks
                    GasContainerStat pb = possibleBlocks.get(pos);
                    pb.building = other;
                    pb.canUnload = interactable && other.canUnload() && other.items != null && other.items.has(sortItem);
                    pb.canLoad = interactable && !(other.block instanceof StorageBlock) && other.acceptItem(this, sortItem);
                    pb.index = pos;
                }
            } else {
                // select the next item for nulloaders
                // inspired of nextIndex() but for all proximity at once, and also way more powerful
                for (int i = 0; i < itemslength; i++) {
                    int total = (rotations + i + 1) % itemslength;
                    boolean hasProvider = false;
                    boolean hasReceiver = false;
                    boolean isDistinct = false;
                    Item possibleItem = content.item(total);
                    for (int pos = 0; pos < proximity.size; pos++) {
                        var other = proximity.get(pos);
                        boolean interactable = other.interactable(team);
                        // set the stats of all buildings in possibleBlocks while we are at it
                        GasContainerStat pb = possibleBlocks.get(pos);
                        pb.building = other;
                        pb.canUnload = interactable && other.canUnload() && other.items != null && other.items.has(possibleItem);
                        pb.canLoad = interactable && !(other.block instanceof StorageBlock) && other.acceptItem(this, possibleItem);
                        pb.index = pos;
                        // the part handling framerate issues and slow conveyor belts, to avoid skipping items
                        if (hasProvider && pb.canLoad)
                            isDistinct = true;
                        if (hasReceiver && pb.canUnload)
                            isDistinct = true;
                        hasProvider = hasProvider || pb.canUnload;
                        hasReceiver = hasReceiver || pb.canLoad;
                    }
                    if (isDistinct) {
                        item = possibleItem;
                        break;
                    }
                }
            }
            if (item != null) {
                // only compute the load factor if a transfer is possible
                for (int pos = 0; pos < proximity.size; pos++) {
                    GasContainerStat pb = possibleBlocks.get(pos);
                    var other = pb.building;
                    pb.loadFactor = (other.getMaximumAccepted(item) == 0) || (other.items == null) ? 0 : other.items.get(item) / (float) other.getMaximumAccepted(item);
                }
                possibleBlocks.sort(comparator);
                GasContainerStat dumpingFrom = null;
                GasContainerStat dumpingTo = null;
                // choose the building to accept the item
                for (int i = 0; i < possibleBlocks.size; i++) {
                    if (possibleBlocks.get(i).canLoad) {
                        dumpingTo = possibleBlocks.get(i);
                        break;
                    }
                }
                // choose the building to take the item from
                for (int i = possibleBlocks.size - 1; i >= 0; i--) {
                    if (possibleBlocks.get(i).canUnload) {
                        dumpingFrom = possibleBlocks.get(i);
                        break;
                    }
                }
                // increment the priority if not used
                for (int i = 0; i < possibleBlocks.size; i++) {
                    lastUsed[i] = (lastUsed[i] + 1) % 2147483647;
                }
                // trade the items
                // TODO  && dumpingTo != dumpingFrom ?
                if (dumpingFrom != null && dumpingTo != null && (dumpingFrom.loadFactor != dumpingTo.loadFactor || !dumpingFrom.canLoad)) {
                    dumpingTo.building.handleItem(this, item);
                    dumpingFrom.building.removeStack(item, 1);
                    lastUsed[dumpingFrom.index] = 0;
                    lastUsed[dumpingTo.index] = 0;
                    any = true;
                }
                if (sortItem == null)
                    rotations = item.id;
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

        @Override
        public void draw() {
            super.draw();
            Draw.color(sortItem == null ? Color.clear : sortItem.color);
            Draw.rect(centerRegion, x, y);
            Draw.color();
        }

        @Override
        public void buildConfiguration(Table table) {
            ItemSelection.buildTable(GasUnloader.this, table, content.items(), () -> sortItem, this::configure);
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
