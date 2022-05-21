package gas.world.blocks.sandbox;


import gas.annotations.GasAnnotations;
import gas.content.Gasses;
import gas.gen.GasBuilding;
import gas.type.Gas;
import gas.world.GasBlock;
import arc.graphics.g2d.Draw;
import arc.scene.ui.layout.Table;
import arc.util.Eachable;
import arc.util.Nullable;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.world.blocks.ItemSelection;
import mindustry.world.meta.BlockGroup;

public class GasSource extends GasBlock {
    public GasSource(String name) {
        super(name);
        hasGasses = true;
        outputsGas=true;
        update = true;
        solid = true;
        group = BlockGroup.transportation;
        configurable = true;
        saveConfig = true;
        noUpdateDisabled = true;
        displayFlow = false;
        config(Gas.class, (GasSourceBuild tile,Gas gas) -> {
            tile.source = gas;
        });
        configClear((GasSourceBuild tile) -> {
            tile.source = null;
        });
    }
    @Override
    public void drawPlanConfig(BuildPlan req, Eachable<BuildPlan> list) {
        this.drawPlanConfigCenter(req, req.config, "center");
    }
    public void setBars() {
        super.setBars();
        removeBar("gas");
    }
    public class GasSourceBuild extends GasBuilding {
        @Nullable
        Gas source =null;
        public void updateTile() {
            if (this.source == null) {
                this.gasses.clear();
            } else {
                this.gasses.add(this.source, GasSource.this.gasCapacity);
                this.dumpGas(this.source);
            }

        }

        public void draw() {
            super.draw();
            if (this.source == null) {
                Draw.rect("cross", this.x, this.y);
            } else {
                Draw.color(this.source.color);
                Draw.rect("center", this.x, this.y);
                Draw.color();
            }

        }

        public void buildConfiguration(Table table) {
            ItemSelection.buildTable(table, Vars.content.getBy(Gasses.gasType()), () -> {
                return this.source;
            }, this::configure);
        }

        public boolean onConfigureBuildTapped(Building other) {
            if (this == other) {
                this.deselect();
                this.configure((Object)null);
                return false;
            } else {
                return true;
            }
        }

        public Gas config() {
            return this.source;
        }

        public byte version() {
            return 1;
        }

        public void write(Writes write) {
            super.write(write);
            write.s(this.source == null ? -1 : this.source.id);
        }

        public void read(Reads read, byte revision) {
            super.read(read, revision);
            int id = revision == 1 ? read.s() : read.b();
            this.source = id == -1 ? null : Gasses.getByID(id);
        }

    }
}
