package gas.world.blocks.defense;

import mindustry.world.blocks.defense.Wall.*;
import gas.entities.comp.*;
import mindustry.entities.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import gas.content.*;
import mindustry.world.blocks.defense.turrets.*;
import gas.world.blocks.payloads.*;
import gas.world.meta.*;
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
import mindustry.world.blocks.payloads.*;
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
import arc.graphics.g2d.*;
import mindustry.world.blocks.*;
import gas.world.blocks.production.GasGenericCrafter.*;
import arc.*;
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
import arc.util.*;
import mindustry.world.blocks.defense.*;
import gas.world.blocks.production.*;
import gas.entities.bullets.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;
import static mindustry.Vars.*;

public class GasWall extends GasBlock {

    /**
     * Lighting chance. -1 to disable
     */
    public float lightningChance = -1f;

    public float lightningDamage = 20f;

    public int lightningLength = 17;

    public Color lightningColor = Pal.surge;

    public Sound lightningSound = Sounds.spark;

    /**
     * Bullet deflection chance. -1 to disable
     */
    public float chanceDeflect = -1f;

    public boolean flashHit;

    public Color flashColor = Color.white;

    public Sound deflectSound = Sounds.none;

    public GasWall(String name) {
        super(name);
        solid = true;
        destructible = true;
        group = BlockGroup.walls;
        buildCostMultiplier = 6f;
        canOverdrive = false;
        drawDisabled = false;
        crushDamageMultiplier = 5f;
        priority = TargetPriority.wall;
        // it's a wall of course it's supported everywhere
        envEnabled = Env.any;
    }

    @Override
    public void setStats() {
        super.setStats();
        if (chanceDeflect > 0f)
            stats.add(Stat.baseDeflectChance, chanceDeflect, StatUnit.none);
        if (lightningChance > 0f) {
            stats.add(Stat.lightningChance, lightningChance * 100f, StatUnit.percent);
            stats.add(Stat.lightningDamage, lightningDamage, StatUnit.none);
        }
    }

    @Override
    public TextureRegion[] icons() {
        return new TextureRegion[] { Core.atlas.find(Core.atlas.has(name) ? name : name + "1") };
    }

    public class GasWallBuild extends GasBuilding {

        public float hit;

        @Override
        public void draw() {
            super.draw();
            // draw flashing white overlay if enabled
            if (flashHit) {
                if (hit < 0.0001f)
                    return;
                Draw.color(flashColor);
                Draw.alpha(hit * 0.5f);
                Draw.blend(Blending.additive);
                Fill.rect(x, y, tilesize * size, tilesize * size);
                Draw.blend();
                Draw.reset();
                if (!state.isPaused()) {
                    hit = Mathf.clamp(hit - Time.delta / 10f);
                }
            }
        }

        @Override
        public boolean collision(Bullet bullet) {
            super.collision(bullet);
            hit = 1f;
            // create lightning if necessary
            if (lightningChance > 0f) {
                if (Mathf.chance(lightningChance)) {
                    Lightning.create(team, lightningColor, lightningDamage, x, y, bullet.rotation() + 180f, lightningLength);
                    lightningSound.at(tile, Mathf.random(0.9f, 1.1f));
                }
            }
            // deflect bullets if necessary
            if (chanceDeflect > 0f) {
                // slow bullets are not deflected
                if (bullet.vel.len() <= 0.1f || !bullet.type.reflectable)
                    return true;
                // bullet reflection chance depends on bullet damage
                if (!Mathf.chance(chanceDeflect / bullet.damage()))
                    return true;
                // make sound
                deflectSound.at(tile, Mathf.random(0.9f, 1.1f));
                // translate bullet back to where it was upon collision
                bullet.trns(-bullet.vel.x, -bullet.vel.y);
                float penX = Math.abs(x - bullet.x), penY = Math.abs(y - bullet.y);
                if (penX > penY) {
                    bullet.vel.x *= -1;
                } else {
                    bullet.vel.y *= -1;
                }
                bullet.owner = this;
                bullet.team = team;
                bullet.time += 1f;
                // disable bullet collision by returning false
                return false;
            }
            return true;
        }
    }
}
