package gas.world.blocks.logic;

import mindustry.world.blocks.logic.SwitchBlock.*;
import gas.entities.comp.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.experimental.*;
import gas.world.meta.*;
import mindustry.annotations.Annotations.*;
import gas.world.blocks.units.*;
import gas.world.blocks.defense.*;
import mindustry.world.blocks.legacy.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.meta.*;
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
import gas.world.*;
import gas.world.blocks.defense.turrets.*;
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
import mindustry.world.consumers.*;
import gas.world.modules.*;
import gas.world.blocks.*;
import gas.*;
import gas.io.*;
import gas.world.blocks.payloads.*;
import mindustry.world.blocks.units.*;
import gas.content.*;
import gas.world.blocks.storage.*;
import gas.world.blocks.production.*;
import arc.util.io.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import gas.world.meta.values.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;

public class GasSwitchBlock extends GasBlock {

    @Load("@-on")
    public TextureRegion onRegion;

    public GasSwitchBlock(String name) {
        super(name);
        configurable = true;
        update = true;
        drawDisabled = false;
        autoResetEnabled = false;
        group = BlockGroup.logic;
        envEnabled = Env.any;
        config(Boolean.class, (GasSwitchBuild entity, Boolean b) -> entity.enabled = b);
    }

    public class GasSwitchBuild extends GasBuilding {

        @Override
        public boolean configTapped() {
            configure(!enabled);
            Sounds.click.at(this);
            return false;
        }

        @Override
        public void draw() {
            super.draw();
            if (enabled) {
                Draw.rect(onRegion, x, y);
            }
        }

        @Override
        public Boolean config() {
            return enabled;
        }

        @Override
        public byte version() {
            return 1;
        }

        @Override
        public void readAll(Reads read, byte revision) {
            super.readAll(read, revision);
            if (revision == 1) {
                enabled = read.bool();
            }
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.bool(enabled);
        }
    }
}
