package gas.world.blocks.defense.turrets;

import gas.entities.comp.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import mindustry.world.blocks.defense.turrets.PayloadAmmoTurret.*;
import gas.content.*;
import mindustry.world.blocks.defense.turrets.*;
import gas.world.blocks.payloads.*;
import gas.world.meta.*;
import mindustry.ui.*;
import gas.world.blocks.units.*;
import mindustry.world.blocks.heat.*;
import mindustry.world.blocks.legacy.*;
import mindustry.gen.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.meta.*;
import gas.world.blocks.heat.*;
import gas.world.blocks.defense.*;
import mindustry.entities.bullet.*;
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
import arc.scene.ui.layout.*;
import gas.world.blocks.campaign.*;
import mindustry.world.modules.*;
import gas.ui.*;
import mindustry.world.blocks.environment.*;
import gas.world.consumers.*;
import mindustry.world.blocks.payloads.*;
import mindustry.world.blocks.*;
import gas.world.blocks.production.GasGenericCrafter.*;
import arc.scene.ui.*;
import mindustry.world.blocks.logic.*;
import gas.world.modules.*;
import gas.world.blocks.*;
import gas.*;
import gas.io.*;
import gas.world.draw.*;
import gas.gen.*;
import mindustry.world.blocks.defense.turrets.Turret.*;
import gas.world.blocks.storage.*;
import mindustry.world.blocks.units.*;
import mindustry.graphics.*;
import gas.world.blocks.production.*;
import arc.struct.*;
import mindustry.ctype.*;
import arc.util.io.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import mindustry.world.blocks.power.*;
import mindustry.type.*;
import mindustry.world.blocks.sandbox.*;
import static mindustry.Vars.*;

// TODO visuals!
public class GasPayloadAmmoTurret extends GasTurret {

    public ObjectMap<UnlockableContent, BulletType> ammoTypes = new ObjectMap<>();

    protected UnlockableContent[] ammoKeys;

    public GasPayloadAmmoTurret(String name) {
        super(name);
        maxAmmo = 3;
    }

    /**
     * Initializes accepted ammo map. Format: [block1, bullet1, block2, bullet2...]
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
            entry.value.lifetime = (range + margin) / entry.value.speed;
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
        consume(new ConsumePayloadFilter(i -> ammoTypes.containsKey(i)) {

            @Override
            public void build(Building build, Table table) {
                MultiReqImage image = new MultiReqImage();
                content.blocks().each(i -> filter.get(i) && i.unlockedNow(), block -> image.add(new ReqImage(new Image(block.uiIcon), () -> build instanceof GasPayloadTurretBuild it && !it.payloads.isEmpty() && it.currentBlock() == block)));
                table.add(image).size(8 * 4);
            }

            @Override
            public float efficiency(Building build) {
                // valid when there's any ammo in the turret
                return build instanceof GasPayloadTurretBuild it && it.payloads.any() ? 1f : 0f;
            }

            @Override
            public void display(Stats stats) {
                // don't display
            }
        });
        ammoKeys = ammoTypes.keys().toSeq().toArray(Block.class);
        super.init();
    }

    public class GasPayloadTurretBuild extends GasTurretBuild {

        public PayloadSeq payloads = new PayloadSeq();

        public UnlockableContent currentBlock() {
            for (var block : ammoKeys) {
                if (payloads.contains(block)) {
                    return block;
                }
            }
            return null;
        }

        @Override
        public boolean acceptPayload(Building source, Payload payload) {
            return payload instanceof BuildPayload build && payloads.total() < maxAmmo && ammoTypes.containsKey(build.block());
        }

        @Override
        public void handlePayload(Building source, Payload payload) {
            payloads.add(((BuildPayload) payload).block());
        }

        @Override
        public boolean hasAmmo() {
            return payloads.total() > 0;
        }

        @Override
        public BulletType useAmmo() {
            for (var block : ammoKeys) {
                if (payloads.contains(block)) {
                    payloads.remove(block);
                    return ammoTypes.get(block);
                }
            }
            return null;
        }

        @Override
        public BulletType peekAmmo() {
            for (var block : ammoKeys) {
                if (payloads.contains(block)) {
                    return ammoTypes.get(block);
                }
            }
            return null;
        }

        @Override
        public PayloadSeq getPayloads() {
            return payloads;
        }

        @Override
        public void updateTile() {
            totalAmmo = payloads.total();
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
        public void write(Writes write) {
            super.write(write);
            payloads.write(write);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            payloads.read(read);
            // TODO remove invalid ammo
        }
    }
}
