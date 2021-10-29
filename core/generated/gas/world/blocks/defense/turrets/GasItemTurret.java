package gas.world.blocks.defense.turrets;

import gas.entities.comp.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import mindustry.content.*;
import gas.content.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.experimental.*;
import gas.world.meta.*;
import mindustry.game.EventType.*;
import gas.world.blocks.units.*;
import mindustry.world.blocks.defense.turrets.ItemTurret.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.meta.*;
import gas.world.blocks.distribution.*;
import mindustry.entities.bullet.*;
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
import mindustry.ui.*;
import gas.world.*;
import mindustry.world.consumers.*;
import gas.world.blocks.gas.*;
import arc.scene.ui.layout.*;
import gas.world.blocks.campaign.*;
import mindustry.world.modules.*;
import gas.ui.*;
import mindustry.world.blocks.environment.*;
import mindustry.*;
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
import mindustry.world.blocks.defense.turrets.Turret.*;
import gas.world.blocks.defense.*;
import gas.world.blocks.storage.*;
import mindustry.graphics.*;
import gas.gen.*;
import arc.struct.*;
import arc.util.io.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import gas.world.meta.values.*;
import mindustry.world.blocks.power.*;
import mindustry.type.*;
import mindustry.world.blocks.sandbox.*;
import gas.world.blocks.power.*;
import static mindustry.Vars.*;

public class GasItemTurret extends GasTurret {

    public ObjectMap<Item, BulletType> ammoTypes = new ObjectMap<>();

    public GasItemTurret(String name) {
        super(name);
        hasItems = true;
    }

    /**
     * Initializes accepted ammo map. Format: [item1, bullet1, item2, bullet2...]
     */
    public void ammo(Object... objects) {
        ammoTypes = ObjectMap.of(objects);
    }

    /**
     * Makes copies of all bullets and limits their range.
     */
    public void limitRange() {
        limitRange(1f);
    }

    /**
     * Makes copies of all bullets and limits their range.
     */
    public void limitRange(float margin) {
        for (var entry : ammoTypes.copy().entries()) {
            var copy = entry.value.copy();
            copy.lifetime = (range + margin) / copy.speed;
            ammoTypes.put(entry.key, copy);
        }
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.remove(Stat.itemCapacity);
        stats.add(Stat.ammo, StatValues.ammo(ammoTypes));
    }

    @Override
    public void init() {
        consumes.add(new ConsumeItemFilter(i -> ammoTypes.containsKey(i)) {

            @Override
            public void build(Building tile, Table table) {
                MultiReqImage image = new MultiReqImage();
                content.items().each(i -> filter.get(i) && i.unlockedNow(), item -> image.add(new ReqImage(new ItemImage(item.uiIcon), () -> tile instanceof GasItemTurretBuild it && !it.ammo.isEmpty() && ((GasItemEntry) it.ammo.peek()).item == item)));
                table.add(image).size(8 * 4);
            }

            @Override
            public boolean valid(Building entity) {
                // valid when there's any ammo in the turret
                return entity instanceof GasItemTurretBuild it && !it.ammo.isEmpty();
            }

            @Override
            public void display(Stats stats) {
                // don't display
            }
        });
        super.init();
    }

    public class GasItemTurretBuild extends GasTurretBuild {

        @Override
        public void onProximityAdded() {
            super.onProximityAdded();
            // add first ammo item to cheaty blocks so they can shoot properly
            if (cheating() && ammo.size > 0) {
                handleItem(this, ammoTypes.entries().next().key);
            }
        }

        @Override
        public void updateTile() {
            unit.ammo((float) unit.type().ammoCapacity * totalAmmo / maxAmmo);
            super.updateTile();
        }

        @Override
        public void displayBars(Table bars) {
            super.displayBars(bars);
            bars.add(new Bar("stat.ammo", Pal.ammo, () -> (float) totalAmmo / maxAmmo)).growX();
            bars.row();
        }

        @Override
        public int acceptStack(Item item, int amount, Teamc source) {
            BulletType type = ammoTypes.get(item);
            if (type == null)
                return 0;
            return Math.min((int) ((maxAmmo - totalAmmo) / ammoTypes.get(item).ammoMultiplier), amount);
        }

        @Override
        public void handleStack(Item item, int amount, Teamc source) {
            for (int i = 0; i < amount; i++) {
                handleItem(null, item);
            }
        }

        // currently can't remove items from turrets.
        @Override
        public int removeStack(Item item, int amount) {
            return 0;
        }

        @Override
        public void handleItem(Building source, Item item) {
            if (item == Items.pyratite) {
                Events.fire(Trigger.flameAmmo);
            }
            BulletType type = ammoTypes.get(item);
            if (type == null)
                return;
            totalAmmo += type.ammoMultiplier;
            // find ammo entry by type
            for (int i = 0; i < ammo.size; i++) {
                GasItemEntry entry = (GasItemEntry) ammo.get(i);
                // if found, put it to the right
                if (entry.item == item) {
                    entry.amount += type.ammoMultiplier;
                    ammo.swap(i, ammo.size - 1);
                    return;
                }
            }
            // must not be found
            ammo.add(new GasItemEntry(item, (int) type.ammoMultiplier));
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            return ammoTypes.get(item) != null && totalAmmo + ammoTypes.get(item).ammoMultiplier <= maxAmmo;
        }

        @Override
        public byte version() {
            return 2;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.b(ammo.size);
            for (var entry : ammo) {
                GasItemEntry i = (GasItemEntry) entry;
                write.s(i.item.id);
                write.s(i.amount);
            }
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            ammo.clear();
            totalAmmo = 0;
            int amount = read.ub();
            for (int i = 0; i < amount; i++) {
                Item item = Vars.content.item(revision < 2 ? read.ub() : read.s());
                short a = read.s();
                // only add ammo if this is a valid ammo type
                if (item != null && ammoTypes.containsKey(item)) {
                    totalAmmo += a;
                    ammo.add(new GasItemEntry(item, a));
                }
            }
        }
    }

    public class GasItemEntry extends AmmoEntry {

        protected Item item;

        GasItemEntry(Item item, int amount) {
            this.item = item;
            this.amount = amount;
        }

        @Override
        public BulletType type() {
            return ammoTypes.get(item);
        }
    }
}
