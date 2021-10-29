package gas.world.blocks.defense.turrets;

import gas.entities.comp.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.experimental.*;
import gas.world.meta.*;
import gas.world.blocks.units.*;
import gas.world.blocks.defense.*;
import arc.util.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import arc.math.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.meta.*;
import gas.world.blocks.distribution.*;
import mindustry.world.blocks.defense.turrets.ReloadTurret.*;
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
import mindustry.world.blocks.logic.*;
import gas.world.modules.*;
import gas.world.blocks.*;
import gas.*;
import gas.io.*;
import gas.world.draw.*;
import mindustry.world.blocks.units.*;
import mindustry.world.blocks.defense.turrets.Turret.*;
import gas.content.*;
import gas.world.blocks.storage.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import gas.world.meta.values.*;
import mindustry.world.blocks.power.*;
import mindustry.type.*;
import mindustry.world.blocks.sandbox.*;
import gas.world.blocks.power.*;
import static mindustry.Vars.*;

public class GasReloadTurret extends GasBaseTurret {

    public float reloadTime = 10f;

    public GasReloadTurret(String name) {
        super(name);
    }

    @Override
    public void setStats() {
        super.setStats();
        if (acceptCoolant) {
            stats.add(Stat.booster, StatValues.boosters(reloadTime, consumes.<ConsumeLiquidBase>get(ConsumeType.liquid).amount, coolantMultiplier, true, l -> consumes.liquidfilters.get(l.id)));
        }
    }

    public class GasReloadTurretBuild extends GasBaseTurretBuild {

        public float reload;

        protected void updateCooling() {
            if (reload < reloadTime) {
                float maxUsed = consumes.<ConsumeLiquidBase>get(ConsumeType.liquid).amount;
                Liquid liquid = liquids.current();
                float used = Math.min(liquids.get(liquid), maxUsed * Time.delta) * baseReloadSpeed();
                reload += used * liquid.heatCapacity * coolantMultiplier;
                liquids.remove(liquid, used);
                if (Mathf.chance(0.06 * used)) {
                    coolEffect.at(x + Mathf.range(size * tilesize / 2f), y + Mathf.range(size * tilesize / 2f));
                }
            }
        }

        protected float baseReloadSpeed() {
            return efficiency();
        }
    }
}
