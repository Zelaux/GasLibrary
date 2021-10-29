package gas.world.blocks.defense;

import gas.entities.comp.*;
import mindustry.entities.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.experimental.*;
import gas.world.meta.*;
import mindustry.annotations.Annotations.*;
import gas.world.blocks.units.*;
import gas.world.blocks.defense.*;
import arc.util.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import arc.math.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.meta.*;
import arc.graphics.*;
import gas.world.blocks.distribution.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import gas.world.blocks.payloads.*;
import mindustry.world.*;
import gas.world.blocks.production.*;
import mindustry.world.blocks.legacy.*;
import gas.world.blocks.liquid.*;
import mindustry.world.blocks.storage.*;
import gas.entities.*;
import mindustry.world.blocks.campaign.*;
import gas.world.blocks.defense.turrets.*;
import gas.gen.*;
import gas.world.*;
import mindustry.world.consumers.*;
import gas.world.blocks.gas.*;
import gas.world.blocks.campaign.*;
import mindustry.world.modules.*;
import gas.ui.*;
import mindustry.world.blocks.environment.*;
import gas.world.consumers.*;
import mindustry.world.blocks.payloads.*;
import gas.world.blocks.sandbox.*;
import mindustry.world.blocks.*;
import gas.world.blocks.production.GasGenericCrafter.*;
import arc.graphics.g2d.*;
import mindustry.world.blocks.logic.*;
import gas.world.modules.*;
import gas.world.blocks.*;
import mindustry.world.blocks.defense.ShockMine.*;
import gas.*;
import gas.io.*;
import gas.world.draw.*;
import mindustry.world.blocks.units.*;
import gas.content.*;
import gas.world.blocks.storage.*;
import gas.entities.bullets.*;
import mindustry.graphics.*;
import mindustry.world.blocks.defense.*;
import gas.world.meta.values.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;
import gas.world.blocks.power.*;

public class GasShockMine extends GasBlock {

    public final int timerDamage = timers++;

    public float cooldown = 80f;

    public float tileDamage = 5f;

    public float damage = 13;

    public int length = 10;

    public int tendrils = 6;

    public Color lightningColor = Pal.lancerLaser;

    public int shots = 6;

    public float inaccuracy = 0f;

    @Nullable
    public BulletType bullet;

    public float teamAlpha = 0.3f;

    @Load("@-team-top")
    public TextureRegion teamRegion;

    public GasShockMine(String name) {
        super(name);
        update = false;
        destructible = true;
        solid = false;
        targetable = false;
    }

    public class GasShockMineBuild extends GasBuilding {

        @Override
        public void drawTeam() {
            // no
        }

        @Override
        public void draw() {
            super.draw();
            Draw.color(team.color, teamAlpha);
            Draw.rect(teamRegion, x, y);
            Draw.color();
        }

        @Override
        public void drawCracks() {
            // no
        }

        @Override
        public void unitOn(Unit unit) {
            if (enabled && unit.team != team && timer(timerDamage, cooldown)) {
                triggered();
                damage(tileDamage);
            }
        }

        public void triggered() {
            for (int i = 0; i < tendrils; i++) {
                Lightning.create(team, lightningColor, damage, x, y, Mathf.random(360f), length);
            }
            if (bullet != null) {
                for (int i = 0; i < shots; i++) {
                    bullet.create(this, x, y, (360f / shots) * i + Mathf.random(inaccuracy));
                }
            }
        }
    }
}
