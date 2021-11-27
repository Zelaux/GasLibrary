package gas.world.blocks.defense.turrets;

import mindustry.entities.bullet.*;
import gas.type.*;
import gas.world.blocks.campaign.*;
import gas.world.blocks.logic.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.experimental.*;
import gas.world.meta.*;
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
import gas.world.blocks.gas.*;
import gas.entities.comp.*;
import mindustry.world.modules.*;
import gas.ui.*;
import mindustry.world.blocks.environment.*;
import gas.world.consumers.*;
import mindustry.world.blocks.payloads.*;
import mindustry.world.blocks.*;
import gas.world.blocks.production.GasGenericCrafter.*;
import mindustry.world.blocks.defense.turrets.PowerTurret.*;
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
import gas.world.blocks.production.*;
import arc.struct.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import gas.world.meta.values.*;
import mindustry.logic.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;

public class GasPowerTurret extends GasTurret {

    public BulletType shootType;

    public float powerUse = 1f;

    public GasPowerTurret(String name) {
        super(name);
        hasPower = true;
        envEnabled |= Env.space;
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.ammo, StatValues.ammo(ObjectMap.of(this, shootType)));
    }

    @Override
    public void init() {
        consumes.powerCond(powerUse, TurretBuild::isActive);
        super.init();
    }

    public class GasPowerTurretBuild extends GasTurretBuild {

        @Override
        public void updateTile() {
            if (unit != null) {
                unit.ammo(power.status * unit.type().ammoCapacity);
            }
            super.updateTile();
        }

        @Override
        public double sense(LAccess sensor) {
            switch(sensor) {
                case ammo:
                    return power.status;
                case ammoCapacity:
                    return 1;
                default:
                    return super.sense(sensor);
            }
        }

        @Override
        public BulletType useAmmo() {
            // nothing used directly
            return shootType;
        }

        @Override
        public boolean hasAmmo() {
            // you can always rotate, but never shoot if there's no power
            return true;
        }

        @Override
        public BulletType peekAmmo() {
            return shootType;
        }
    }
}
