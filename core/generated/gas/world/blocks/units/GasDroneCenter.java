package gas.world.blocks.units;

import mindustry.entities.units.*;
import gas.entities.comp.*;
import mindustry.entities.*;
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
import arc.graphics.g2d.*;
import mindustry.world.blocks.logic.*;
import gas.world.modules.*;
import gas.world.blocks.*;
import gas.*;
import gas.io.*;
import gas.world.draw.*;
import gas.gen.*;
import gas.world.blocks.storage.*;
import mindustry.world.blocks.units.DroneCenter.*;
import mindustry.world.blocks.units.*;
import mindustry.graphics.*;
import gas.world.blocks.production.*;
import arc.struct.*;
import arc.util.io.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import mindustry.world.blocks.power.*;
import mindustry.type.*;
import mindustry.world.blocks.sandbox.*;
import static mindustry.Vars.*;

// TODO remove
public class GasDroneCenter extends GasBlock {

    public int unitsSpawned = 4;

    public UnitType droneType;

    public StatusEffect status = StatusEffects.overdrive;

    public float droneConstructTime = 60f * 3f;

    public float statusDuration = 60f * 2f;

    public float droneRange = 50f;

    public GasDroneCenter(String name) {
        super(name);
        update = solid = true;
        configurable = true;
    }

    @Override
    public void init() {
        super.init();
        droneType.aiController = GasEffectDroneAI::new;
    }

    public class GasDroneCenterBuild extends GasBuilding {

        protected IntSeq readUnits = new IntSeq();

        protected int readTarget = -1;

        public Seq<Unit> units = new Seq<>();

        @Nullable
        public Unit target;

        public float droneProgress, droneWarmup, totalDroneProgress;

        @Override
        public void updateTile() {
            if (!readUnits.isEmpty()) {
                units.clear();
                readUnits.each(i -> {
                    var unit = Groups.unit.getByID(i);
                    if (unit != null) {
                        units.add(unit);
                    }
                });
                readUnits.clear();
            }
            units.removeAll(u -> !u.isAdded() || u.dead);
            droneWarmup = Mathf.lerpDelta(droneWarmup, units.size < unitsSpawned ? efficiency : 0f, 0.1f);
            totalDroneProgress += droneWarmup * Time.delta;
            if (readTarget != 0) {
                target = Groups.unit.getByID(readTarget);
                readTarget = 0;
            }
            // TODO better effects?
            if (units.size < unitsSpawned && (droneProgress += edelta() / droneConstructTime) >= 1f) {
                var unit = droneType.create(team);
                if (unit instanceof BuildingTetherc bt) {
                    bt.building(this);
                }
                unit.set(x, y);
                unit.rotation = 90f;
                unit.add();
                Fx.spawn.at(unit);
                units.add(unit);
                droneProgress = 0f;
            }
            if (target != null && !target.isValid()) {
                target = null;
            }
            // TODO no autotarget, bad
            if (target == null) {
                target = Units.closest(team, x, y, u -> !u.spawnedByCore && u.type != droneType);
            }
        }

        @Override
        public void drawConfigure() {
            Drawf.square(x, y, tile.block().size * tilesize / 2f + 1f + Mathf.absin(Time.time, 4f, 1f));
            if (target != null) {
                Drawf.square(target.x, target.y, target.hitSize * 0.8f);
            }
        }

        @Override
        public void draw() {
            super.draw();
            // TODO draw more stuff
            if (droneWarmup > 0) {
                Draw.draw(Layer.blockOver + 0.2f, () -> {
                    Drawf.construct(this, droneType.fullIcon, Pal.accent, 0f, droneProgress, droneWarmup, totalDroneProgress, 14f);
                });
            }
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.i(target == null ? -1 : target.id);
            write.s(units.size);
            for (var unit : units) {
                write.i(unit.id);
            }
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            readTarget = read.i();
            int count = read.s();
            readUnits.clear();
            for (int i = 0; i < count; i++) {
                readUnits.add(read.i());
            }
        }
    }

    public class GasEffectDroneAI extends AIController {

        @Override
        public void updateUnit() {
            if (!(unit instanceof BuildingTetherc tether))
                return;
            if (!(tether.building() instanceof GasDroneCenterBuild build))
                return;
            if (build.target == null)
                return;
            target = build.target;
            // TODO what angle?
            moveTo(target, build.target.hitSize / 1.8f + droneRange - 10f);
            unit.lookAt(target);
            // TODO low power? status effects may not be the best way to do this...
            if (unit.within(target, droneRange + build.target.hitSize)) {
                build.target.apply(status, statusDuration);
            }
        }
    }
}
