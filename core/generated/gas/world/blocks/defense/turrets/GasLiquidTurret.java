package gas.world.blocks.defense.turrets;

import mindustry.entities.bullet.*;
import mindustry.entities.*;
import gas.type.*;
import gas.world.blocks.campaign.*;
import gas.world.blocks.logic.*;
import mindustry.content.*;
import mindustry.type.*;
import mindustry.world.blocks.defense.turrets.LiquidTurret.*;
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
import mindustry.world.blocks.defense.turrets.*;
import gas.world.blocks.gas.*;
import gas.entities.comp.*;
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
import mindustry.world.blocks.defense.turrets.Turret.*;
import gas.content.*;
import gas.world.blocks.storage.*;
import mindustry.graphics.*;
import gas.world.blocks.production.*;
import arc.struct.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import gas.world.meta.values.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;
import static mindustry.Vars.*;

public class GasLiquidTurret extends GasTurret {

    public ObjectMap<Liquid, BulletType> ammoTypes = new ObjectMap<>();

    @Load("@-liquid")
    public TextureRegion liquidRegion;

    @Load("@-top")
    public TextureRegion topRegion;

    public boolean extinguish = true;

    public GasLiquidTurret(String name) {
        super(name);
        acceptCoolant = false;
        hasLiquids = true;
        loopSound = Sounds.spray;
        shootSound = Sounds.none;
        smokeEffect = Fx.none;
        shootEffect = Fx.none;
        outlinedIcon = 1;
    }

    /**
     * Initializes accepted ammo map. Format: [liquid1, bullet1, liquid2, bullet2...]
     */
    public void ammo(Object... objects) {
        ammoTypes = ObjectMap.of(objects);
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.ammo, StatValues.ammo(ammoTypes));
    }

    @Override
    public void init() {
        consumes.add(new ConsumeLiquidFilter(i -> ammoTypes.containsKey(i), 1f) {

            @Override
            public boolean valid(Building entity) {
                return entity.liquids.total() > 0.001f;
            }

            @Override
            public void update(Building entity) {
            }

            @Override
            public void display(Stats stats) {
            }
        });
        super.init();
    }

    @Override
    public TextureRegion[] icons() {
        if (topRegion.found())
            return new TextureRegion[] { baseRegion, region, topRegion };
        return super.icons();
    }

    public class GasLiquidTurretBuild extends GasTurretBuild {

        @Override
        public void draw() {
            super.draw();
            if (liquidRegion.found()) {
                Drawf.liquid(liquidRegion, x + tr2.x, y + tr2.y, liquids.total() / liquidCapacity, liquids.current().color, rotation - 90);
            }
            if (topRegion.found())
                Draw.rect(topRegion, x + tr2.x, y + tr2.y, rotation - 90);
        }

        @Override
        public boolean shouldActiveSound() {
            return wasShooting && enabled;
        }

        @Override
        public void updateTile() {
            if (unit != null) {
                unit.ammo(unit.type().ammoCapacity * liquids.currentAmount() / liquidCapacity);
            }
            super.updateTile();
        }

        @Override
        protected void findTarget() {
            if (extinguish && liquids.current().canExtinguish()) {
                Fire result = null;
                float mindst = 0f;
                int tr = (int) (range / tilesize);
                for (int x = -tr; x <= tr; x++) {
                    for (int y = -tr; y <= tr; y++) {
                        Tile other = world.tile(x + tile.x, y + tile.y);
                        var fire = Fires.get(x + tile.x, y + tile.y);
                        float dst = fire == null ? 0 : dst2(fire);
                        // do not extinguish fires on other team blocks
                        if (other != null && fire != null && Fires.has(other.x, other.y) && dst <= range * range && (result == null || dst < mindst) && (other.build == null || other.team() == team)) {
                            result = fire;
                            mindst = dst;
                        }
                    }
                }
                if (result != null) {
                    target = result;
                    // don't run standard targeting
                    return;
                }
            }
            super.findTarget();
        }

        @Override
        protected void effects() {
            BulletType type = peekAmmo();
            Effect fshootEffect = shootEffect == Fx.none ? type.shootEffect : shootEffect;
            Effect fsmokeEffect = smokeEffect == Fx.none ? type.smokeEffect : smokeEffect;
            fshootEffect.at(x + tr.x, y + tr.y, rotation, liquids.current().color);
            fsmokeEffect.at(x + tr.x, y + tr.y, rotation, liquids.current().color);
            shootSound.at(tile);
            if (shootShake > 0) {
                Effect.shake(shootShake, shootShake, tile.build);
            }
            recoil = recoilAmount;
        }

        @Override
        public BulletType useAmmo() {
            if (cheating())
                return ammoTypes.get(liquids.current());
            BulletType type = ammoTypes.get(liquids.current());
            liquids.remove(liquids.current(), 1f / type.ammoMultiplier);
            return type;
        }

        @Override
        public BulletType peekAmmo() {
            return ammoTypes.get(liquids.current());
        }

        @Override
        public boolean hasAmmo() {
            return ammoTypes.get(liquids.current()) != null && liquids.total() >= 1f / ammoTypes.get(liquids.current()).ammoMultiplier;
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            return false;
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid) {
            return ammoTypes.get(liquid) != null && (liquids.current() == liquid || (ammoTypes.containsKey(liquid) && (!ammoTypes.containsKey(liquids.current()) || liquids.get(liquids.current()) <= 1f / ammoTypes.get(liquids.current()).ammoMultiplier + 0.001f)));
        }
    }
}
