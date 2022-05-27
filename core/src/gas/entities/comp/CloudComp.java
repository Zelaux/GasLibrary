package gas.entities.comp;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import gas.entities.*;
import gas.entities.bullets.*;
import gas.gen.*;
import gas.type.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.world.*;
import mindustry.world.blocks.defense.*;
import mma.annotations.*;

import static gas.entities.Clouds.maxGas;
import static mindustry.Vars.world;

@ModAnnotations.EntityDef(value = {Cloudc.class}, pooled = true)
@ModAnnotations.Component(base = true)
abstract class CloudComp implements Posc, Cloudc, Drawc{
    private static final Rect rect = new Rect(), rect2 = new Rect();
    private static int seeds;
    private static boolean hasWall;
    private static int blockCounter;
    @ModAnnotations.Import
    int id;
    @ModAnnotations.Import
    float x, y;

    transient float accepting, updateTime, lastRipple, lifeTime;
    float amount;
    int generation;
    Tile tile;
    Gas gasObject;


    public float getFlammability(){
        return this.gasObject.flammability * this.amount;
    }

    @Override
    @ModAnnotations.Replace
    public void update(){
        float addSpeed = accepting > 0 ? 3f : 0f;
        float prevAmount= amount;
        amount -= Time.delta * (1f - gasObject.viscosity) / (5f + addSpeed);
        amount += accepting;
        if (amount-prevAmount>0){
            lifeTime += Time.delta;
        }
        accepting = 0f;

        if(this.amount >= maxGas / 1.5f){
            float deposited = Math.min((amount - maxGas / 1.5f) / 4f, 0.3f) * Time.delta;
            int targets = 0;
            for(Point2 point : Geometry.d4){
                Tile other = world.tile(tile.x + point.x, tile.y + point.y);
                if(other != null && other.block() == Blocks.air){
                    targets++;
                    Clouds.deposit(other, tile, gasObject, deposited, false);
                }
            }
            amount -= deposited * targets;
        }

        amount = Mathf.clamp(amount, 0, maxGas);

        if(amount <= 0f){
            remove();
        }

        if(amount >= maxGas / 2f && updateTime <= 0f){
            Units.nearby(rect.setSize(Mathf.clamp(amount / (maxGas / 1.5f)) * 10f).setCenter(x, y), unit -> {
                if(unit.isGrounded() && !unit.hovering){
                    unit.hitbox(rect2);
                    if(rect.overlaps(rect2)){
                        unit.apply(gasObject.effect, 60 * 2);

                        if(unit.vel.len() > 0.1){
                            Fx.ripple.at(unit.x, unit.y, unit.type.rippleScale, gasObject.color);
                        }
                    }
                }
            });

            if(gasObject.temperature > 0.7f && (tile.build != null) && Mathf.chance(0.5)){
                Fires.create(tile);
            }

            updateTime = 40f;
        }

        Puddle puddle = Puddles.get(tile);
        if(puddle != null){
            if(puddle.liquid.temperature >= 0.7f && gasObject.temperature >= 0.7f){
                Fires.create(tile);
                if(Mathf.chance(0.006 * amount)){
                    Bullets.fireball.createNet(Team.derelict, x, y, Mathf.random(360f), -1f, 1f, 1f);
                }
            }
            if(puddle.liquid.temperature >= 0.7f && gasObject.explosiveness >= 0.9f){
                float flammability, explosiveness, radius;
                flammability = getFlammability() + puddle.getFlammability();
                explosiveness = gasObject.explosiveness + puddle.liquid.explosiveness;
                radius = (amount + puddle.amount) * 1.2f;
                Vars.world.tiles.eachTile((tile) -> {
                    if(tile == null || tile.build == null) return;
                    Building build = tile.build;
                    hasWall = false;
                    blockCounter = 0;
                    Vars.world.raycastEach(tileX(), tileY(), build.tileX(), build.tileY(), (x, y) -> {
                        Building b = Vars.world.build(x * 8, y * 8);
                        if(b == null) return true;
                        blockCounter++;
                        if(b.block instanceof Wall){
                            hasWall = true;
                            return false;
                        }

                        return true;
                    });
                    float distance = tile.build.dst(this);
                    float blockIndex = 1;
                    float distanceIndex = 1f - ((distance + 1f) / radius);
                    if(distance <= radius && !hasWall) tile.build.damage(distanceIndex * (40 / blockIndex));
                });
                Damage.dynamicExplosion(x, y, flammability, explosiveness, 0, Mathf.clamp((radius), 0, 30), Vars.state.rules.damageExplosions, true, Team.derelict);
                puddle.amount = Math.max(0, puddle.amount - explosiveness - flammability - radius / 8f);
                amount = Math.max(0, amount - explosiveness - flammability - radius / 8f);
            }
        }
        Bullet bullet1 = Groups.bullet.find(bullet -> {
            bullet.hitbox(Tmp.r1);
            rect.setSize(Mathf.clamp(amount / (maxGas / 1.5f)) * 10.0F).setCenter(x, y);
            return Tmp.r1.overlaps(rect);
        });
        if(bullet1 != null && bullet1.type instanceof GasBulletType && ((GasBulletType)bullet1.type).explodes(gasObject, amount)){
            float flammability = getFlammability();
            float explosiveness = gasObject.explosiveness;
            float radius = Mathf.clamp((amount * 1.2f), 0, 30);
            Damage.dynamicExplosion(x, y, flammability, explosiveness, 0, radius, Vars.state.rules.damageExplosions, true, bullet1.team);
            amount = Math.max(0, amount - explosiveness - flammability - radius / 8f);
        }
        updateTime -= Time.delta;
    }

    @Override
    public void draw(){
        Draw.z(110.0F);
        gasObject.drawCloud(self());
       /* seeds = this.id();
        boolean onLiquid = this.tile.floor().isLiquid;
        float f = Mathf.clamp(this.amount / 46.666668F), a;
        float smag = onLiquid ? 0.8F : 0.0F;
        float sscl = 25.0F;
        Draw.blend(Blending.additive);
        Shader last = Draw.getShader();
//        last
//        Draw.shader(Shaders.PlanetShader);
        Color col = Tmp.c1.set(this.gasObject.color).shiftValue(-0.05F);
        a = (Mathf.clamp(this.amount / 250f) * gasObject.transparency) * 0.9f;
        a = Math.max(0.1f, a);
        Draw.color(col);
//        Draw.color(col,col.cpy().a(0),a);
        Draw.alpha(a);
        Fill.circle(this.x + Mathf.sin(Time.time + (float) (seeds * 532), sscl, smag), this.y + Mathf.sin(Time.time + (float) (seeds * 53), sscl, smag), f * 8.0F);
        Angles.randLenVectors((long) this.id(), 3, f * 6.0F, (ex, ey) -> {
            Fill.circle(this.x + ex + Mathf.sin(Time.time + (float) (seeds * 532), sscl, smag), this.y + ey + Mathf.sin(Time.time + (float) (seeds * 53), sscl, smag), f * 5.0F);
            ++seeds;
        });
        Draw.blend();
        Draw.color();
//        Draw.reset();
        if (this.gasObject.lightColor.a > 0.001F && f > 0.0F) {
            Color color = this.gasObject.lightColor;
            float opacity = color.a * f;
            Drawf.light(*//*Team.derelict,*//* this.tile.drawx(), this.tile.drawy(), 30.0F * f, color, opacity * 0.8F);
        }*/

    }

    @ModAnnotations.Replace
    public float clipSize(){
        return 20;
    }

    @Override
    public void remove(){
        Clouds.remove(tile);
    }

    @Override
    public void afterRead(){
        Clouds.register(self());
    }
}
