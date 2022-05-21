package gas.world.blocks.defense.turrets;

import gas.entities.comp.*;
import mindustry.entities.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import mindustry.content.*;
import gas.content.*;
import mindustry.world.blocks.defense.turrets.*;
import gas.world.blocks.payloads.*;
import gas.world.meta.*;
import gas.world.blocks.units.*;
import mindustry.world.blocks.heat.*;
import mindustry.core.*;
import mindustry.world.blocks.legacy.*;
import mindustry.gen.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.meta.*;
import gas.world.blocks.heat.*;
import gas.world.blocks.defense.*;
import mindustry.entities.bullet.*;
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
import mindustry.world.blocks.logic.*;
import gas.world.modules.*;
import gas.world.blocks.*;
import gas.*;
import gas.io.*;
import gas.world.draw.*;
import gas.gen.*;
import mindustry.world.blocks.defense.turrets.Turret.*;
import gas.world.blocks.storage.*;
import mindustry.world.blocks.units.*;
import gas.world.blocks.production.*;
import mindustry.world.blocks.defense.turrets.LiquidTurret.*;
import arc.struct.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import mindustry.world.blocks.power.*;
import mindustry.type.*;
import mindustry.world.blocks.sandbox.*;
import static mindustry.Vars.*;

public class GasLiquidTurret extends GasTurret {

    public ObjectMap<Liquid, BulletType> ammoTypes = new ObjectMap<>();

    public boolean extinguish = true;

    public GasLiquidTurret(String name) {
        super(name);
        hasLiquids = true;
        loopSound = Sounds.spray;
        shootSound = Sounds.none;
        smokeEffect = Fx.none;
        shootEffect = Fx.none;
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
        consume(new ConsumeLiquidFilter(i -> ammoTypes.containsKey(i), 1f) {

            @Override
            public void update(Building build) {
            }

            @Override
            public void display(Stats stats) {
            }
        });
        super.init();
    }

    public class GasLiquidTurretBuild extends GasTurretBuild {

        @Override
        public boolean shouldActiveSound() {
            return wasShooting && enabled;
        }

        @Override
        public void updateTile() {
            unit.ammo(unit.type().ammoCapacity * liquids.currentAmount() / liquidCapacity);
            super.updateTile();
        }

        @Override
        protected void findTarget() {
            if (extinguish && liquids.current().canExtinguish()) {
                int tx = World.toTile(x), ty = World.toTile(y);
                Fire result = null;
                float mindst = 0f;
                int tr = (int) (range / tilesize);
                for (int x = -tr; x <= tr; x++) {
                    for (int y = -tr; y <= tr; y++) {
                        Tile other = world.tile(x + tx, y + ty);
                        var fire = Fires.get(x + tx, y + ty);
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
            return ammoTypes.get(liquids.current()) != null && liquids.currentAmount() >= 1f / ammoTypes.get(liquids.current()).ammoMultiplier;
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            return false;
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid) {
            return ammoTypes.get(liquid) != null && (liquids.current() == liquid || ((!ammoTypes.containsKey(liquids.current()) || liquids.get(liquids.current()) <= 1f / ammoTypes.get(liquids.current()).ammoMultiplier + 0.001f)));
        }
    }
}
