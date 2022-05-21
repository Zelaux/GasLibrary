package gas.world.blocks.units;

import gas.entities.comp.*;
import mindustry.entities.*;
import gas.type.*;
import gas.world.blocks.logic.*;
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
import mindustry.world.blocks.units.RepairTower.*;
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
import arc.struct.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import mindustry.logic.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;
import static mindustry.Vars.*;

public class GasRepairTower extends GasBlock {

    static final float refreshInterval = 6f;

    public float range = 80f;

    public Color circleColor = Pal.heal, glowColor = Pal.heal.cpy().a(0.5f);

    public float circleSpeed = 120f, circleStroke = 3f, squareRad = 3f, squareSpinScl = 0.8f, glowMag = 0.5f, glowScl = 8f;

    public float healAmount = 1f;

    @Load("@-glow")
    public TextureRegion glow;

    public GasRepairTower(String name) {
        super(name);
        update = true;
        solid = true;
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, range, Pal.placing);
    }

    public class GasRepairTowerBuild extends GasBuilding implements Ranged {

        public float refresh = Mathf.random(refreshInterval);

        public float warmup = 0f;

        public float totalProgress = 0f;

        public Seq<Unit> targets = new Seq<>();

        @Override
        public void updateTile() {
            if (potentialEfficiency > 0 && (refresh += Time.delta) >= refreshInterval) {
                targets.clear();
                refresh = 0f;
                Units.nearby(team, x, y, range, u -> {
                    if (u.damaged()) {
                        targets.add(u);
                    }
                });
            }
            boolean any = false;
            if (efficiency > 0) {
                for (var target : targets) {
                    if (target.damaged()) {
                        target.heal(healAmount * efficiency);
                        any = true;
                    }
                }
            }
            warmup = Mathf.lerpDelta(warmup, any ? efficiency : 0f, 0.08f);
            totalProgress += Time.delta / circleSpeed;
        }

        @Override
        public boolean shouldConsume() {
            return targets.size > 0;
        }

        @Override
        public void draw() {
            super.draw();
            if (warmup <= 0.001f)
                return;
            Draw.z(Layer.effect);
            float mod = totalProgress % 1f;
            Draw.color(circleColor);
            Lines.stroke(circleStroke * (1f - mod) * warmup);
            Lines.circle(x, y, range * mod);
            Draw.color(Pal.heal);
            Fill.square(x, y, squareRad * warmup, Time.time / squareSpinScl);
            Draw.reset();
            Drawf.additive(glow, glowColor, warmup * (1f - glowMag + Mathf.absin(Time.time, glowScl, glowMag)), x, y, 0f, Layer.blockAdditive);
        }

        @Override
        public float range() {
            return range;
        }

        @Override
        public float warmup() {
            return warmup;
        }
    }
}
