package gas.world.blocks.defense.turrets;

import gas.entities.comp.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import mindustry.content.*;
import gas.content.*;
import mindustry.world.blocks.defense.turrets.*;
import gas.world.blocks.payloads.*;
import mindustry.world.blocks.legacy.*;
import mindustry.world.*;
import gas.world.blocks.units.*;
import mindustry.world.blocks.heat.*;
import gas.world.meta.*;
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
import gas.world.*;
import gas.world.blocks.sandbox.*;
import mindustry.world.blocks.storage.*;
import gas.world.blocks.liquid.*;
import gas.entities.*;
import mindustry.world.blocks.campaign.*;
import gas.world.blocks.defense.turrets.*;
import gas.world.blocks.distribution.*;
import mindustry.world.blocks.defense.turrets.ContinuousLiquidTurret.*;
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
import arc.struct.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import mindustry.world.blocks.power.*;
import mindustry.type.*;
import mindustry.world.blocks.sandbox.*;

public class GasContinuousLiquidTurret extends GasContinuousTurret {

    public ObjectMap<Liquid, BulletType> ammoTypes = new ObjectMap<>();

    public float liquidConsumed = 1f / 60f;

    public GasContinuousLiquidTurret(String name) {
        super(name);
        hasLiquids = true;
        // TODO
        loopSound = Sounds.minebeam;
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
        stats.remove(Stat.ammo);
        // TODO looks bad
        stats.add(Stat.ammo, StatValues.number(liquidConsumed * 60f, StatUnit.perSecond, true));
        stats.add(Stat.ammo, StatValues.ammo(ammoTypes));
    }

    @Override
    public void init() {
        // TODO display ammoMultiplier.
        consume(new ConsumeLiquidFilter(i -> ammoTypes.containsKey(i), liquidConsumed) {

            @Override
            public void display(Stats stats) {
            }
        });
        super.init();
    }

    public class GasLiquidTurretBuild extends GasContinuousTurretBuild {

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
        public BulletType useAmmo() {
            // does not consume ammo upon firing
            return peekAmmo();
        }

        @Override
        public BulletType peekAmmo() {
            return ammoTypes.get(liquids.current());
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
