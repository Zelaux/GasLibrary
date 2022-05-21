package gas.world.blocks.defense;

import gas.entities.comp.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import mindustry.content.*;
import gas.content.*;
import mindustry.world.blocks.defense.turrets.*;
import gas.world.blocks.payloads.*;
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
import mindustry.world.blocks.distribution.*;
import gas.world.blocks.power.*;
import mindustry.world.*;
import gas.world.blocks.sandbox.*;
import mindustry.world.blocks.storage.*;
import gas.world.blocks.liquid.*;
import gas.entities.*;
import mindustry.world.blocks.defense.MendProjector.*;
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
import arc.graphics.*;
import gas.*;
import gas.io.*;
import gas.world.draw.*;
import gas.gen.*;
import gas.world.blocks.storage.*;
import mindustry.world.blocks.units.*;
import mindustry.graphics.*;
import gas.world.blocks.production.*;
import arc.util.io.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import mindustry.logic.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;
import static mindustry.Vars.*;

public class GasMendProjector extends GasBlock {

    public final int timerUse = timers++;

    public Color baseColor = Color.valueOf("84f491");

    public Color phaseColor = baseColor;

    @Load("@-top")
    public TextureRegion topRegion;

    public float reload = 250f;

    public float range = 60f;

    public float healPercent = 12f;

    public float phaseBoost = 12f;

    public float phaseRangeBoost = 50f;

    public float useTime = 400f;

    public GasMendProjector(String name) {
        super(name);
        solid = true;
        update = true;
        group = BlockGroup.projectors;
        hasPower = true;
        hasItems = true;
        emitLight = true;
        lightRadius = 50f;
        suppressable = true;
        envEnabled |= Env.space;
    }

    @Override
    public boolean outputsItems() {
        return false;
    }

    @Override
    public void setStats() {
        stats.timePeriod = useTime;
        super.setStats();
        stats.add(Stat.repairTime, (int) (100f / healPercent * reload / 60f), StatUnit.seconds);
        stats.add(Stat.range, range / tilesize, StatUnit.blocks);
        stats.add(Stat.boostEffect, phaseRangeBoost / tilesize, StatUnit.blocks);
        stats.add(Stat.boostEffect, (phaseBoost + healPercent) / healPercent, StatUnit.timesSpeed);
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, range, baseColor);
        indexer.eachBlock(player.team(), x * tilesize + offset, y * tilesize + offset, range, other -> true, other -> Drawf.selected(other, Tmp.c1.set(baseColor).a(Mathf.absin(4f, 1f))));
    }

    public class GasMendBuild extends GasBuilding implements Ranged {

        public float heat, charge = Mathf.random(reload), phaseHeat, smoothEfficiency;

        @Override
        public float range() {
            return range;
        }

        @Override
        public void updateTile() {
            boolean canHeal = !checkSuppression();
            smoothEfficiency = Mathf.lerpDelta(smoothEfficiency, efficiency, 0.08f);
            heat = Mathf.lerpDelta(heat, efficiency > 0 && canHeal ? 1f : 0f, 0.08f);
            charge += heat * delta();
            phaseHeat = Mathf.lerpDelta(phaseHeat, optionalEfficiency, 0.1f);
            if (optionalEfficiency > 0 && timer(timerUse, useTime) && canHeal) {
                consume();
            }
            if (charge >= reload && canHeal) {
                float realRange = range + phaseHeat * phaseRangeBoost;
                charge = 0f;
                indexer.eachBlock(this, realRange, b -> b.damaged() && !b.isHealSuppressed(), other -> {
                    other.heal(other.maxHealth() * (healPercent + phaseHeat * phaseBoost) / 100f * efficiency);
                    other.recentlyHealed();
                    Fx.healBlockFull.at(other.x, other.y, other.block.size, baseColor, other.block);
                });
            }
        }

        @Override
        public double sense(LAccess sensor) {
            if (sensor == LAccess.progress)
                return Mathf.clamp(charge / reload);
            return super.sense(sensor);
        }

        @Override
        public void drawSelect() {
            float realRange = range + phaseHeat * phaseRangeBoost;
            indexer.eachBlock(this, realRange, other -> true, other -> Drawf.selected(other, Tmp.c1.set(baseColor).a(Mathf.absin(4f, 1f))));
            Drawf.dashCircle(x, y, realRange, baseColor);
        }

        @Override
        public void draw() {
            super.draw();
            float f = 1f - (Time.time / 100f) % 1f;
            Draw.color(baseColor, phaseColor, phaseHeat);
            Draw.alpha(heat * Mathf.absin(Time.time, 50f / Mathf.PI2, 1f) * 0.5f);
            Draw.rect(topRegion, x, y);
            Draw.alpha(1f);
            Lines.stroke((2f * f + 0.2f) * heat);
            Lines.square(x, y, Math.min(1f + (1f - f) * size * tilesize / 2f, size * tilesize / 2f));
            Draw.reset();
        }

        @Override
        public void drawLight() {
            Drawf.light(x, y, lightRadius * smoothEfficiency, baseColor, 0.7f * smoothEfficiency);
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
