package gas.world.blocks.defense;

import gas.entities.comp.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import mindustry.world.blocks.defense.turrets.*;
import gas.world.blocks.payloads.*;
import mindustry.world.blocks.experimental.*;
import gas.world.meta.*;
import mindustry.annotations.Annotations.*;
import gas.world.blocks.units.*;
import gas.world.blocks.defense.*;
import arc.util.*;
import mindustry.world.blocks.legacy.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import arc.math.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.meta.*;
import arc.graphics.*;
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
import mindustry.world.blocks.payloads.*;
import mindustry.world.blocks.campaign.*;
import gas.gen.*;
import gas.world.*;
import gas.world.blocks.defense.turrets.*;
import gas.world.blocks.gas.*;
import arc.math.geom.*;
import gas.world.blocks.campaign.*;
import mindustry.world.modules.*;
import gas.ui.*;
import mindustry.world.blocks.environment.*;
import gas.world.consumers.*;
import arc.graphics.g2d.*;
import mindustry.world.blocks.*;
import gas.world.blocks.production.GasGenericCrafter.*;
import arc.*;
import mindustry.world.consumers.*;
import gas.world.modules.*;
import gas.world.blocks.*;
import gas.*;
import gas.io.*;
import mindustry.ui.*;
import mindustry.world.blocks.units.*;
import gas.content.*;
import gas.world.blocks.storage.*;
import mindustry.graphics.*;
import gas.world.blocks.production.*;
import mindustry.world.blocks.defense.OverdriveProjector.*;
import arc.util.io.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import gas.world.meta.values.*;
import mindustry.logic.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;
import static mindustry.Vars.*;

public class GasOverdriveProjector extends GasBlock {

    public final int timerUse = timers++;

    @Load("@-top")
    public TextureRegion topRegion;

    public float reload = 60f;

    public float range = 80f;

    public float speedBoost = 1.5f;

    public float speedBoostPhase = 0.75f;

    public float useTime = 400f;

    public float phaseRangeBoost = 20f;

    public boolean hasBoost = true;

    public Color baseColor = Color.valueOf("feb380");

    public Color phaseColor = Color.valueOf("ffd59e");

    public GasOverdriveProjector(String name) {
        super(name);
        solid = true;
        update = true;
        group = BlockGroup.projectors;
        hasPower = true;
        hasItems = true;
        canOverdrive = false;
        emitLight = true;
        lightRadius = 50f;
        envEnabled |= Env.space;
    }

    @Override
    public boolean outputsItems() {
        return false;
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, range, baseColor);
        indexer.eachBlock(player.team(), x * tilesize + offset, y * tilesize + offset, range, other -> other.block.canOverdrive, other -> Drawf.selected(other, Tmp.c1.set(baseColor).a(Mathf.absin(4f, 1f))));
    }

    @Override
    public void setStats() {
        stats.timePeriod = useTime;
        super.setStats();
        stats.add(Stat.speedIncrease, "+" + (int) (speedBoost * 100f - 100) + "%");
        stats.add(Stat.range, range / tilesize, StatUnit.blocks);
        stats.add(Stat.productionTime, useTime / 60f, StatUnit.seconds);
        if (hasBoost) {
            stats.add(Stat.boostEffect, (range + phaseRangeBoost) / tilesize, StatUnit.blocks);
            stats.add(Stat.boostEffect, "+" + (int) ((speedBoost + speedBoostPhase) * 100f - 100) + "%");
        }
    }

    @Override
    public void setBars() {
        super.setBars();
        bars.add("boost", (GasOverdriveBuild entity) -> new Bar(() -> Core.bundle.format("bar.boost", Math.max((int) (entity.realBoost() * 100 - 100), 0)), () -> Pal.accent, () -> entity.realBoost() / (hasBoost ? speedBoost + speedBoostPhase : speedBoost)));
    }

    public class GasOverdriveBuild extends GasBuilding implements Ranged {

        float heat;

        float charge = Mathf.random(reload);

        float phaseHeat;

        float smoothEfficiency;

        @Override
        public float range() {
            return range;
        }

        @Override
        public void drawLight() {
            Drawf.light(team, x, y, lightRadius * smoothEfficiency, baseColor, 0.7f * smoothEfficiency);
        }

        @Override
        public void updateTile() {
            smoothEfficiency = Mathf.lerpDelta(smoothEfficiency, efficiency(), 0.08f);
            heat = Mathf.lerpDelta(heat, consValid() ? 1f : 0f, 0.08f);
            charge += heat * Time.delta;
            if (hasBoost) {
                phaseHeat = Mathf.lerpDelta(phaseHeat, Mathf.num(cons.optionalValid()), 0.1f);
            }
            if (charge >= reload) {
                float realRange = range + phaseHeat * phaseRangeBoost;
                charge = 0f;
                indexer.eachBlock(this, realRange, other -> true, other -> other.applyBoost(realBoost(), reload + 1f));
            }
            if (timer(timerUse, useTime) && efficiency() > 0 && consValid()) {
                consume();
            }
        }

        public float realBoost() {
            return consValid() ? (speedBoost + phaseHeat * speedBoostPhase) * efficiency() : 0f;
        }

        @Override
        public void drawSelect() {
            float realRange = range + phaseHeat * phaseRangeBoost;
            indexer.eachBlock(this, realRange, other -> other.block.canOverdrive, other -> Drawf.selected(other, Tmp.c1.set(baseColor).a(Mathf.absin(4f, 1f))));
            Drawf.dashCircle(x, y, realRange, baseColor);
        }

        @Override
        public void draw() {
            super.draw();
            float f = 1f - (Time.time / 100f) % 1f;
            Draw.color(baseColor, phaseColor, phaseHeat);
            Draw.alpha(heat * Mathf.absin(Time.time, 10f, 1f) * 0.5f);
            Draw.rect(topRegion, x, y);
            Draw.alpha(1f);
            Lines.stroke((2f * f + 0.1f) * heat);
            float r = Math.max(0f, Mathf.clamp(2f - f * 2f) * size * tilesize / 2f - f - 0.2f), w = Mathf.clamp(0.5f - f) * size * tilesize;
            Lines.beginLine();
            for (int i = 0; i < 4; i++) {
                Lines.linePoint(x + Geometry.d4(i).x * r + Geometry.d4(i).y * w, y + Geometry.d4(i).y * r - Geometry.d4(i).x * w);
                if (f < 0.5f)
                    Lines.linePoint(x + Geometry.d4(i).x * r - Geometry.d4(i).y * w, y + Geometry.d4(i).y * r + Geometry.d4(i).x * w);
            }
            Lines.endLine(true);
            Draw.reset();
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(heat);
            write.f(phaseHeat);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            heat = read.f();
            phaseHeat = read.f();
        }
    }
}
