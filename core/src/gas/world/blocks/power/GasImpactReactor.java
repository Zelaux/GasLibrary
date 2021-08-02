package gas.world.blocks.power;

import gas.annotations.GasAnnotations;
import arc.Core;
import arc.Events;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.struct.EnumSet;
import arc.util.Strings;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.content.Fx;
import mindustry.entities.Damage;
import mindustry.entities.Effect;
import mindustry.game.EventType;
import mindustry.gen.Sounds;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.logic.LAccess;
import mindustry.ui.Bar;
import mindustry.world.meta.BlockFlag;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

import static mindustry.Vars.state;
import static mindustry.Vars.tilesize;

public class GasImpactReactor extends GasPowerGenerator{
    public final int timerUse = timers++;

    public float warmupSpeed = 0.001f;
    public float itemDuration = 60f;
    public int explosionRadius = 23;
    public int explosionDamage = 1900;

    public Color plasma1 = Color.valueOf("ffd06b"), plasma2 = Color.valueOf("ff361b");

    public @GasAnnotations.Load("@-bottom")
    TextureRegion bottomRegion;
    public @GasAnnotations.Load(value = "@-plasma-#", length = 4) TextureRegion[] plasmaRegions;

    public GasImpactReactor(String name){
        super(name);
        update = true;
        solid = true;
        hasPower = true;
        hasLiquids = true;
        liquidCapacity = 30f;
        hasItems = true;
        hasGas=true;
        gasCapacity = 30f;
        outputsPower = consumesPower = true;
        emitLight=true;
        flags = EnumSet.of(BlockFlag.reactor, BlockFlag.generator);
    }

    @Override
    public void setBars(){
        super.setBars();

        bars.add("poweroutput", (GasPowerGenerator.GasGeneratorBuild entity) -> new Bar(() ->
                Core.bundle.format("bar.poweroutput",
                        Strings.fixed(Math.max(entity.getPowerProduction() - consumes.getPower().usage, 0) * 60 * entity.timeScale, 1)),
                () -> Pal.powerBar,
                () -> entity.productionEfficiency));
    }

    @Override
    public void setStats(){
        super.setStats();

        if(hasItems){
            stats.add(Stat.productionTime, itemDuration / 60f, StatUnit.seconds);
        }
    }

    @Override
    public TextureRegion[] icons(){
        return new TextureRegion[]{bottomRegion, region};
    }

    public class GasImpactReactorBuild extends GasPowerGenerator.GasGeneratorBuild {
        public float warmup;

        @Override
        public void updateTile(){
            if(consValid() && power.status >= 0.99f){
                boolean prevOut = getPowerProduction() <= consumes.getPower().requestedPower(this);

                warmup = Mathf.lerpDelta(warmup, 1f, warmupSpeed * timeScale);
                if(Mathf.equal(warmup, 1f, 0.001f)){
                    warmup = 1f;
                }

                if(!prevOut && (getPowerProduction() > consumes.getPower().requestedPower(this))){
                    Events.fire(EventType.Trigger.impactPower);
                }

                if(timer(timerUse, itemDuration / timeScale)){
                    consume();
                }
            }else{
                warmup = Mathf.lerpDelta(warmup, 0f, 0.01f);
            }

            productionEfficiency = Mathf.pow(warmup, 5f);
        }

        @Override
        public float ambientVolume(){
            return warmup;
        }

        @Override
        public void draw(){
            Draw.rect(bottomRegion, x, y);

            for(int i = 0; i < plasmaRegions.length; i++){
                float r = size * tilesize - 3f + Mathf.absin(Time.time, 2f + i * 1f, 5f - i * 0.5f);

                Draw.color(plasma1, plasma2, (float)i / plasmaRegions.length);
                Draw.alpha((0.3f + Mathf.absin(Time.time, 2f + i * 2f, 0.3f + i * 0.05f)) * warmup);
                Draw.blend(Blending.additive);
                Draw.rect(plasmaRegions[i], x, y, r, r, Time.time * (12 + i * 6f) * warmup);
                Draw.blend();
            }

            Draw.color();

            Draw.rect(region, x, y);

            Draw.color();
        }

        @Override
        public void drawLight(){
            Drawf.light(team, x, y, (110f + Mathf.absin(5, 5f)) * warmup, Tmp.c1.set(plasma2).lerp(plasma1, Mathf.absin(7f, 0.2f)), 0.8f * warmup);
        }

        @Override
        public double sense(LAccess sensor){
            if(sensor == LAccess.heat) return warmup;
            return super.sense(sensor);
        }

        @Override
        public void onDestroyed(){
            super.onDestroyed();

            if(warmup < 0.4f || !state.rules.reactorExplosions) return;

            Sounds.explosionbig.at(tile);

            Effect.shake(6f, 16f, x, y);
            Fx.impactShockwave.at(x, y);
            for(int i = 0; i < 6; i++){
                Time.run(Mathf.random(80), () -> Fx.impactcloud.at(x, y));
            }

            Damage.damage(x, y, explosionRadius * tilesize, explosionDamage * 4);


            for(int i = 0; i < 20; i++){
                Time.run(Mathf.random(80), () -> {
                    Tmp.v1.rnd(Mathf.random(40f));
                    Fx.explosion.at(Tmp.v1.x + x, Tmp.v1.y + y);
                });
            }

            for(int i = 0; i < 70; i++){
                Time.run(Mathf.random(90), () -> {
                    Tmp.v1.rnd(Mathf.random(120f));
                    Fx.impactsmoke.at(Tmp.v1.x + x, Tmp.v1.y + y);
                });
            }
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(warmup);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            warmup = read.f();
        }
    }
}
