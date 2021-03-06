package gas.world.blocks.campaign;

import mindustry.annotations.Annotations.*;
import mindustry.logic.*;
import gas.entities.comp.*;
import arc.scene.ui.layout.*;
import mindustry.entities.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import mindustry.content.*;
import gas.content.*;
import mindustry.type.*;
import gas.io.*;
import mindustry.world.blocks.defense.turrets.*;
import gas.world.meta.*;
import mindustry.ui.*;
import gas.world.blocks.units.*;
import mindustry.world.blocks.heat.*;
import arc.audio.*;
import mindustry.world.blocks.legacy.*;
import mindustry.gen.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import arc.math.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.meta.*;
import gas.world.blocks.heat.*;
import gas.world.blocks.defense.*;
import arc.*;
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
import mindustry.world.modules.*;
import gas.world.blocks.gas.*;
import arc.math.geom.*;
import gas.world.blocks.campaign.*;
import arc.Graphics.*;
import gas.ui.*;
import mindustry.world.blocks.environment.*;
import mindustry.world.blocks.campaign.LaunchPad.*;
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
import mindustry.game.EventType.*;
import gas.world.blocks.payloads.*;
import gas.gen.*;
import gas.world.blocks.storage.*;
import mindustry.world.blocks.units.*;
import mindustry.graphics.*;
import arc.util.*;
import arc.struct.*;
import arc.util.io.*;
import mindustry.world.blocks.defense.*;
import gas.world.blocks.production.*;
import gas.entities.bullets.*;
import mindustry.world.blocks.power.*;
import arc.Graphics.Cursor.*;
import mindustry.world.blocks.sandbox.*;
import static mindustry.Vars.*;

public class GasLaunchPad extends GasBlock {

    /**
     * Time inbetween launches.
     */
    public float launchTime = 1f;

    public Sound launchSound = Sounds.none;

    @Load("@-light")
    public TextureRegion lightRegion;

    @Load(value = "@-pod", fallback = "launchpod")
    public TextureRegion podRegion;

    public Color lightColor = Color.valueOf("eab678");

    public GasLaunchPad(String name) {
        super(name);
        hasItems = true;
        solid = true;
        update = true;
        configurable = true;
        flags = EnumSet.of(BlockFlag.launchPad);
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.launchTime, launchTime / 60f, StatUnit.seconds);
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("items", entity -> new Bar(() -> Core.bundle.format("bar.items", entity.items.total()), () -> Pal.items, () -> (float) entity.items.total() / itemCapacity));
        // TODO is "bar.launchcooldown" the right terminology?
        addBar("progress", (GasLaunchPadBuild build) -> new Bar(() -> Core.bundle.get("bar.launchcooldown"), () -> Pal.ammo, () -> Mathf.clamp(build.launchCounter / launchTime)));
    }

    @Override
    public boolean outputsItems() {
        return false;
    }

    public class GasLaunchPadBuild extends GasBuilding {

        public float launchCounter;

        @Override
        public Cursor getCursor() {
            return !state.isCampaign() || net.client() ? SystemCursor.arrow : super.getCursor();
        }

        @Override
        public boolean shouldConsume() {
            // TODO add launch costs, maybe legacy version
            return launchCounter < launchTime;
        }

        @Override
        public double sense(LAccess sensor) {
            if (sensor == LAccess.progress)
                return Mathf.clamp(launchCounter / launchTime);
            return super.sense(sensor);
        }

        @Override
        public void draw() {
            super.draw();
            if (!state.isCampaign())
                return;
            if (lightRegion.found()) {
                Draw.color(lightColor);
                float progress = Math.min((float) items.total() / itemCapacity, launchCounter / launchTime);
                int steps = 3;
                float step = 1f;
                for (int i = 0; i < 4; i++) {
                    for (int j = 0; j < steps; j++) {
                        float alpha = Mathf.curve(progress, (float) j / steps, (j + 1f) / steps);
                        float offset = -(j - 1f) * step;
                        Draw.color(Pal.metalGrayDark, lightColor, alpha);
                        Draw.rect(lightRegion, x + Geometry.d8edge(i).x * offset, y + Geometry.d8edge(i).y * offset, i * 90);
                    }
                }
                Draw.reset();
            }
            Draw.rect(podRegion, x, y);
            Draw.reset();
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            return items.total() < itemCapacity;
        }

        @Override
        public void updateTile() {
            if (!state.isCampaign())
                return;
            // increment launchCounter then launch when full and base conditions are met
            if ((launchCounter += edelta()) >= launchTime && items.total() >= itemCapacity) {
                // if there are item requirements, use those.
                consume();
                launchSound.at(x, y);
                LaunchPayload entity = LaunchPayload.create();
                items.each((item, amount) -> entity.stacks.add(new ItemStack(item, amount)));
                entity.set(this);
                entity.lifetime(120f);
                entity.team(team);
                entity.add();
                Fx.launchPod.at(this);
                items.clear();
                Effect.shake(3f, 3f, this);
                launchCounter = 0f;
            }
        }

        @Override
        public void display(Table table) {
            super.display(table);
            if (!state.isCampaign() || net.client() || team != player.team())
                return;
            table.row();
            table.label(() -> {
                Sector dest = state.rules.sector == null ? null : state.rules.sector.info.getRealDestination();
                return Core.bundle.format("launch.destination", dest == null || !dest.hasBase() ? Core.bundle.get("sectors.nonelaunch") : "[accent]" + dest.name());
            }).pad(4).wrap().width(200f).left();
        }

        @Override
        public void buildConfiguration(Table table) {
            if (!state.isCampaign() || net.client()) {
                deselect();
                return;
            }
            table.button(Icon.upOpen, Styles.cleari, () -> {
                ui.planet.showSelect(state.rules.sector, other -> {
                    if (state.isCampaign() && other.planet == state.rules.sector.planet) {
                        state.rules.sector.info.destination = other;
                    }
                });
                deselect();
            }).size(40f);
        }

        @Override
        public byte version() {
            return 1;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(launchCounter);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            if (revision >= 1) {
                launchCounter = read.f();
            }
        }
    }
}
