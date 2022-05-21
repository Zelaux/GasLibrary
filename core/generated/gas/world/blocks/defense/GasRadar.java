package gas.world.blocks.defense;

import gas.entities.comp.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import gas.content.*;
import mindustry.world.blocks.defense.turrets.*;
import gas.world.blocks.payloads.*;
import gas.world.meta.*;
import mindustry.world.blocks.defense.Radar.*;
import gas.world.blocks.units.*;
import mindustry.world.blocks.heat.*;
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
import arc.graphics.*;
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
import mindustry.annotations.Annotations.*;
import gas.*;
import gas.io.*;
import gas.world.draw.*;
import gas.gen.*;
import gas.world.blocks.storage.*;
import mindustry.world.blocks.units.*;
import mindustry.graphics.*;
import gas.world.blocks.production.*;
import arc.struct.*;
import arc.util.io.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;
import static mindustry.Vars.*;

public class GasRadar extends GasBlock {

    public float discoveryTime = 60f * 10f;

    public float rotateSpeed = 2f;

    @Load("@-base")
    public TextureRegion baseRegion;

    @Load("@-glow")
    public TextureRegion glowRegion;

    public Color glowColor = Pal.turretHeat;

    public float glowScl = 5f, glowMag = 0.6f;

    public GasRadar(String name) {
        super(name);
        update = solid = true;
        flags = EnumSet.of(BlockFlag.hasFogRadius);
        outlineIcon = true;
        fogRadius = 10;
    }

    @Override
    public TextureRegion[] icons() {
        return new TextureRegion[] { baseRegion, region };
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, fogRadius * tilesize, Pal.accent);
    }

    public class GasRadarBuild extends GasBuilding {

        public float progress;

        public float lastRadius = 0f;

        public float smoothEfficiency = 1f;

        public float totalProgress;

        @Override
        public float fogRadius() {
            return fogRadius * progress * smoothEfficiency;
        }

        @Override
        public void updateTile() {
            smoothEfficiency = Mathf.lerpDelta(smoothEfficiency, efficiency, 0.05f);
            if (Math.abs(fogRadius() - lastRadius) >= 0.5f) {
                Vars.fogControl.forceUpdate(team, this);
                lastRadius = fogRadius();
            }
            progress += edelta() / discoveryTime;
            progress = Mathf.clamp(progress);
            totalProgress += efficiency * edelta();
        }

        @Override
        public void drawSelect() {
            Drawf.dashCircle(x, y, fogRadius() * tilesize, Pal.accent);
        }

        @Override
        public void draw() {
            Draw.rect(baseRegion, x, y);
            Draw.rect(region, x, y, rotateSpeed * totalProgress);
            Drawf.additive(glowRegion, glowColor, glowColor.a * (1f - glowMag + Mathf.absin(glowScl, glowMag)), x, y, rotateSpeed * totalProgress, Layer.blockAdditive);
        }

        @Override
        public float progress() {
            return progress;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(progress);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            progress = read.f();
        }
    }
}
