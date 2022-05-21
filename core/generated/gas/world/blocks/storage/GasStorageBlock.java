package gas.world.blocks.storage;

import gas.entities.comp.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import mindustry.content.*;
import gas.content.*;
import mindustry.world.blocks.defense.turrets.*;
import gas.world.blocks.payloads.*;
import gas.world.meta.*;
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
import mindustry.world.blocks.storage.CoreBlock.*;
import mindustry.world.blocks.logic.*;
import gas.world.modules.*;
import gas.world.blocks.*;
import gas.*;
import gas.io.*;
import gas.world.draw.*;
import gas.gen.*;
import gas.world.blocks.storage.*;
import mindustry.world.blocks.units.*;
import mindustry.world.blocks.storage.StorageBlock.*;
import gas.world.blocks.production.*;
import arc.struct.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import mindustry.world.blocks.power.*;
import mindustry.type.*;
import mindustry.world.blocks.sandbox.*;
import static mindustry.Vars.*;

public class GasStorageBlock extends GasBlock {

    public boolean coreMerge = true;

    public GasStorageBlock(String name) {
        super(name);
        hasItems = true;
        solid = true;
        update = false;
        destructible = true;
        separateItemCapacity = true;
        group = BlockGroup.transportation;
        flags = EnumSet.of(BlockFlag.storage);
        allowResupply = true;
        envEnabled = Env.any;
        highUnloadPriority = true;
    }

    @Override
    public boolean outputsItems() {
        return false;
    }

    public static void incinerateEffect(Building self, Building source) {
        if (Mathf.chance(0.3)) {
            Tile edge = Edges.getFacingEdge(source, self);
            Tile edge2 = Edges.getFacingEdge(self, source);
            if (edge != null && edge2 != null && self.wasVisible) {
                Fx.coreBurn.at((edge.worldx() + edge2.worldx()) / 2f, (edge.worldy() + edge2.worldy()) / 2f);
            }
        }
    }

    public class GasStorageBuild extends GasBuilding {

        @Nullable
        public Building linkedCore;

        @Override
        public boolean acceptItem(Building source, Item item) {
            return linkedCore != null ? linkedCore.acceptItem(source, item) : items.get(item) < getMaximumAccepted(item);
        }

        @Override
        public void handleItem(Building source, Item item) {
            if (linkedCore != null) {
                if (linkedCore.items.get(item) >= ((CoreBuild) linkedCore).storageCapacity) {
                    incinerateEffect(this, source);
                }
                ((CoreBuild) linkedCore).noEffect = true;
                linkedCore.handleItem(source, item);
            } else {
                super.handleItem(source, item);
            }
        }

        @Override
        public void itemTaken(Item item) {
            if (linkedCore != null) {
                linkedCore.itemTaken(item);
            }
        }

        @Override
        public int removeStack(Item item, int amount) {
            int result = super.removeStack(item, amount);
            if (linkedCore != null && team == state.rules.defaultTeam && state.isCampaign()) {
                state.rules.sector.info.handleCoreItem(item, -result);
            }
            return result;
        }

        @Override
        public int getMaximumAccepted(Item item) {
            return linkedCore != null ? linkedCore.getMaximumAccepted(item) : itemCapacity;
        }

        @Override
        public int explosionItemCap() {
            // when linked to a core, containers/vaults are made significantly less explosive.
            return linkedCore != null ? Math.min(itemCapacity / 60, 6) : itemCapacity;
        }

        @Override
        public void drawSelect() {
            if (linkedCore != null) {
                linkedCore.drawSelect();
            }
        }

        @Override
        public void overwrote(Seq<Building> previous) {
            // only add prev items when core is not linked
            if (linkedCore == null) {
                for (var other : previous) {
                    if (other.items != null && other.items != items) {
                        items.add(other.items);
                    }
                }
                items.each((i, a) -> items.set(i, Math.min(a, itemCapacity)));
            }
        }

        @Override
        public boolean canPickup() {
            return linkedCore == null;
        }
    }
}
