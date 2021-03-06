package gas.world.blocks.units;

import mindustry.entities.units.*;
import gas.entities.comp.*;
import arc.scene.ui.layout.*;
import mindustry.entities.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import arc.scene.style.*;
import gas.content.*;
import mindustry.world.blocks.defense.turrets.*;
import gas.world.blocks.payloads.*;
import gas.io.*;
import gas.world.meta.*;
import mindustry.game.EventType.*;
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
import mindustry.world.blocks.logic.*;
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
import mindustry.world.blocks.units.UnitFactory.*;
import gas.world.blocks.gas.*;
import mindustry.io.*;
import arc.math.geom.*;
import gas.world.blocks.campaign.*;
import mindustry.world.modules.*;
import gas.ui.*;
import mindustry.world.blocks.environment.*;
import mindustry.*;
import gas.world.consumers.*;
import mindustry.world.blocks.payloads.*;
import mindustry.world.blocks.*;
import gas.world.blocks.production.GasGenericCrafter.*;
import arc.*;
import mindustry.world.consumers.*;
import gas.world.modules.*;
import gas.world.blocks.*;
import arc.graphics.*;
import gas.*;
import arc.graphics.g2d.*;
import mindustry.ui.*;
import gas.gen.*;
import gas.world.blocks.storage.*;
import mindustry.world.blocks.units.*;
import mindustry.graphics.*;
import gas.world.blocks.production.*;
import arc.struct.*;
import arc.util.io.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import mindustry.logic.*;
import mindustry.world.blocks.power.*;
import mindustry.type.*;
import mindustry.world.blocks.sandbox.*;

public class GasUnitFactory extends GasUnitBlock {

    public int[] capacities = {};

    public Seq<UnitPlan> plans = new Seq<>(4);

    public GasUnitFactory(String name) {
        super(name);
        update = true;
        hasPower = true;
        hasItems = true;
        solid = true;
        configurable = true;
        clearOnDoubleTap = true;
        outputsPayload = true;
        rotate = true;
        regionRotated1 = 1;
        commandable = true;
        config(Integer.class, (GasUnitFactoryBuild tile, Integer i) -> {
            if (!configurable)
                return;
            if (tile.currentPlan == i)
                return;
            tile.currentPlan = i < 0 || i >= plans.size ? -1 : i;
            tile.progress = 0;
        });
        config(UnitType.class, (GasUnitFactoryBuild tile, UnitType val) -> {
            if (!configurable)
                return;
            int next = plans.indexOf(p -> p.unit == val);
            if (tile.currentPlan == next)
                return;
            tile.currentPlan = next;
            tile.progress = 0;
        });
        consume(new ConsumeItemDynamic((GasUnitFactoryBuild e) -> e.currentPlan != -1 ? plans.get(e.currentPlan).requirements : ItemStack.empty));
    }

    @Override
    public void init() {
        capacities = new int[Vars.content.items().size];
        for (var plan : plans) {
            for (var stack : plan.requirements) {
                capacities[stack.item.id] = Math.max(capacities[stack.item.id], stack.amount * 2);
                itemCapacity = Math.max(itemCapacity, stack.amount * 2);
            }
        }
        super.init();
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("progress", (GasUnitFactoryBuild e) -> new Bar("bar.progress", Pal.ammo, e::fraction));
        addBar("units", (GasUnitFactoryBuild e) -> new Bar(() -> e.unit() == null ? "[lightgray]" + Iconc.cancel : Core.bundle.format("bar.unitcap", Fonts.getUnicodeStr(e.unit().name), e.team.data().countType(e.unit()), Units.getStringCap(e.team)), () -> Pal.power, () -> e.unit() == null ? 0f : (float) e.team.data().countType(e.unit()) / Units.getCap(e.team)));
    }

    @Override
    public boolean outputsItems() {
        return false;
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.remove(Stat.itemCapacity);
        stats.add(Stat.output, table -> {
            Seq<UnitPlan> p = plans.select(u -> u.unit.unlockedNow());
            table.row();
            for (var plan : p) {
                if (plan.unit.unlockedNow()) {
                    table.image(plan.unit.uiIcon).size(8 * 3).padRight(2).right();
                    table.add(plan.unit.localizedName).left();
                    table.add(Strings.autoFixed(plan.time / 60f, 1) + " " + Core.bundle.get("unit.seconds")).color(Color.lightGray).padLeft(12).left();
                    table.row();
                }
            }
        });
    }

    @Override
    public TextureRegion[] icons() {
        return new TextureRegion[] { region, outRegion, topRegion };
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
        Draw.rect(region, plan.drawx(), plan.drawy());
        Draw.rect(outRegion, plan.drawx(), plan.drawy(), plan.rotation * 90);
        Draw.rect(topRegion, plan.drawx(), plan.drawy());
    }

    public class GasUnitFactoryBuild extends GasUnitBuild {

        @Nullable
        public Vec2 commandPos;

        public int currentPlan = -1;

        public float fraction() {
            return currentPlan == -1 ? 0 : progress / plans.get(currentPlan).time;
        }

        @Override
        public Vec2 getCommandPosition() {
            return commandPos;
        }

        @Override
        public void onCommand(Vec2 target) {
            commandPos = target;
        }

        @Override
        public Object senseObject(LAccess sensor) {
            if (sensor == LAccess.config)
                return currentPlan == -1 ? null : plans.get(currentPlan).unit;
            return super.senseObject(sensor);
        }

        @Override
        public double sense(LAccess sensor) {
            if (sensor == LAccess.progress)
                return Mathf.clamp(fraction());
            return super.sense(sensor);
        }

        @Override
        public void buildConfiguration(Table table) {
            Seq<UnitType> units = Seq.with(plans).map(u -> u.unit).filter(u -> u.unlockedNow() && !u.isBanned());
            if (units.any()) {
                ItemSelection.buildTable(GasUnitFactory.this, table, units, () -> currentPlan == -1 ? null : plans.get(currentPlan).unit, unit -> configure(plans.indexOf(u -> u.unit == unit)));
            } else {
                table.table(Styles.black3, t -> t.add("@none").color(Color.lightGray));
            }
        }

        @Override
        public boolean acceptPayload(Building source, Payload payload) {
            return false;
        }

        @Override
        public void display(Table table) {
            super.display(table);
            TextureRegionDrawable reg = new TextureRegionDrawable();
            table.row();
            table.table(t -> {
                t.left();
                t.image().update(i -> {
                    i.setDrawable(currentPlan == -1 ? Icon.cancel : reg.set(plans.get(currentPlan).unit.uiIcon));
                    i.setScaling(Scaling.fit);
                    i.setColor(currentPlan == -1 ? Color.lightGray : Color.white);
                }).size(32).padBottom(-4).padRight(2);
                t.label(() -> currentPlan == -1 ? "@none" : plans.get(currentPlan).unit.localizedName).wrap().width(230f).color(Color.lightGray);
            }).left();
        }

        @Override
        public Object config() {
            return currentPlan;
        }

        @Override
        public void draw() {
            Draw.rect(region, x, y);
            Draw.rect(outRegion, x, y, rotdeg());
            if (currentPlan != -1) {
                UnitPlan plan = plans.get(currentPlan);
                Draw.draw(Layer.blockOver, () -> Drawf.construct(this, plan.unit, rotdeg() - 90f, progress / plan.time, speedScl, time));
            }
            Draw.z(Layer.blockOver);
            payRotation = rotdeg();
            drawPayload();
            Draw.z(Layer.blockOver + 0.1f);
            Draw.rect(topRegion, x, y);
        }

        @Override
        public void updateTile() {
            if (!configurable) {
                currentPlan = 0;
            }
            if (currentPlan < 0 || currentPlan >= plans.size) {
                currentPlan = -1;
            }
            if (efficiency > 0 && currentPlan != -1) {
                time += edelta() * speedScl * Vars.state.rules.unitBuildSpeed(team);
                progress += edelta() * Vars.state.rules.unitBuildSpeed(team);
                speedScl = Mathf.lerpDelta(speedScl, 1f, 0.05f);
            } else {
                speedScl = Mathf.lerpDelta(speedScl, 0f, 0.05f);
            }
            moveOutPayload();
            if (currentPlan != -1 && payload == null) {
                UnitPlan plan = plans.get(currentPlan);
                // make sure to reset plan when the unit got banned after placement
                if (plan.unit.isBanned()) {
                    currentPlan = -1;
                    return;
                }
                if (progress >= plan.time) {
                    progress %= 1f;
                    Unit unit = plan.unit.create(team);
                    if (commandPos != null && unit.isCommandable()) {
                        unit.command().commandPosition(commandPos);
                    }
                    payload = new UnitPayload(unit);
                    payVector.setZero();
                    consume();
                    Events.fire(new UnitCreateEvent(payload.unit, this));
                }
                progress = Mathf.clamp(progress, 0, plan.time);
            } else {
                progress = 0f;
            }
        }

        @Override
        public boolean shouldConsume() {
            if (currentPlan == -1)
                return false;
            return enabled && payload == null;
        }

        @Override
        public int getMaximumAccepted(Item item) {
            return capacities[item.id];
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            return currentPlan != -1 && items.get(item) < getMaximumAccepted(item) && Structs.contains(plans.get(currentPlan).requirements, stack -> stack.item == item);
        }

        @Nullable
        public UnitType unit() {
            return currentPlan == -1 ? null : plans.get(currentPlan).unit;
        }

        @Override
        public byte version() {
            return 2;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(progress);
            write.s(currentPlan);
            TypeIO.writeVecNullable(write, commandPos);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            progress = read.f();
            currentPlan = read.s();
            if (revision >= 2) {
                commandPos = TypeIO.readVecNullable(read);
            }
        }
    }
}
