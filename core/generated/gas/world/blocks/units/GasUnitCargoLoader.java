package gas.world.blocks.units;

import gas.world.blocks.distribution.*;
import mindustry.entities.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import mindustry.content.*;
import gas.content.*;
import mindustry.type.*;
import gas.world.blocks.payloads.*;
import mindustry.world.blocks.units.UnitCargoLoader.*;
import arc.*;
import gas.world.meta.*;
import mindustry.annotations.Annotations.*;
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
import gas.world.draw.*;
import mindustry.world.blocks.distribution.*;
import gas.world.blocks.power.*;
import mindustry.world.*;
import gas.world.blocks.sandbox.*;
import mindustry.world.blocks.storage.*;
import gas.world.blocks.liquid.*;
import mindustry.game.*;
import gas.entities.*;
import mindustry.world.blocks.campaign.*;
import gas.world.blocks.defense.turrets.*;
import mindustry.ui.*;
import gas.world.*;
import mindustry.world.consumers.*;
import mindustry.world.blocks.defense.turrets.*;
import gas.world.blocks.gas.*;
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
import arc.graphics.*;
import gas.*;
import gas.io.*;
import gas.entities.comp.*;
import gas.gen.*;
import gas.world.blocks.storage.*;
import mindustry.world.blocks.units.*;
import mindustry.graphics.*;
import gas.world.blocks.production.*;
import arc.util.io.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;
import static mindustry.Vars.*;

public class GasUnitCargoLoader extends GasBlock {

    public UnitType unitType = UnitTypes.manifold;

    public float buildTime = 60f * 8f;

    public float polyStroke = 1.8f, polyRadius = 8f;

    public int polySides = 6;

    public float polyRotateSpeed = 1f;

    public Color polyColor = Pal.accent;

    public GasUnitCargoLoader(String name) {
        super(name);
        solid = true;
        update = true;
        hasItems = true;
        itemCapacity = 200;
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("units", (GasUnitTransportSourceBuild e) -> new Bar(() -> Core.bundle.format("bar.unitcap", Fonts.getUnicodeStr(unitType.name), e.team.data().countType(unitType), Units.getStringCap(e.team)), () -> Pal.power, () -> (float) e.team.data().countType(unitType) / Units.getCap(e.team)));
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation) {
        return super.canPlaceOn(tile, team, rotation) && Units.canCreate(team, unitType);
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        if (!Units.canCreate(Vars.player.team(), unitType)) {
            drawPlaceText(Core.bundle.get("bar.cargounitcap"), x, y, valid);
        }
    }

    @Remote(called = Loc.server)
    public static void cargoLoaderDroneSpawned(Tile tile, int id) {
        if (tile == null || !(tile.build instanceof GasUnitTransportSourceBuild build))
            return;
        build.spawned(id);
    }

    public class GasUnitTransportSourceBuild extends GasBuilding {

        // needs to be "unboxed" after reading, since units are read after buildings.
        public int readUnitId = -1;

        public float buildProgress, totalProgress;

        public float warmup, readyness;

        @Nullable
        public Unit unit;

        @Override
        public void updateTile() {
            // unit was lost/destroyed
            if (unit != null && (unit.dead || !unit.isAdded())) {
                unit = null;
            }
            if (readUnitId != -1) {
                unit = Groups.unit.getByID(readUnitId);
                readUnitId = -1;
            }
            warmup = Mathf.approachDelta(warmup, efficiency, 1f / 60f);
            readyness = Mathf.approachDelta(readyness, unit != null ? 1f : 0f, 1f / 60f);
            if (unit == null && Units.canCreate(team, unitType)) {
                buildProgress += edelta() / buildTime;
                totalProgress += edelta();
                if (buildProgress >= 1f) {
                    if (!net.client()) {
                        unit = unitType.create(team);
                        if (unit instanceof BuildingTetherc bt) {
                            bt.building(this);
                        }
                        unit.set(x, y);
                        unit.rotation = 90f;
                        unit.add();
                        Call.cargoLoaderDroneSpawned(tile, unit.id);
                    }
                }
            }
        }

        public void spawned(int id) {
            Fx.spawn.at(x, y);
            buildProgress = 0f;
            if (net.client()) {
                readUnitId = id;
            }
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            return items.total() < itemCapacity;
        }

        @Override
        public boolean shouldConsume() {
            return unit == null;
        }

        @Override
        public void draw() {
            Draw.rect(block.region, x, y);
            if (unit == null) {
                Draw.draw(Layer.blockOver, () -> {
                    // TODO make sure it looks proper
                    Drawf.construct(this, unitType.fullIcon, 0f, buildProgress, warmup, totalProgress);
                });
            } else {
                Draw.z(Layer.bullet - 0.01f);
                Draw.color(polyColor);
                Lines.stroke(polyStroke * readyness);
                Lines.poly(x, y, polySides, polyRadius, Time.time * polyRotateSpeed);
                Draw.reset();
                Draw.z(Layer.block);
            }
        }

        @Override
        public float totalProgress() {
            return totalProgress;
        }

        @Override
        public float progress() {
            return buildProgress;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.i(unit == null ? -1 : unit.id);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            readUnitId = read.i();
        }
    }
}
