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
import gas.world.blocks.production.GasGenericCrafter.*;
import gas.world.blocks.campaign.*;
import mindustry.world.modules.*;
import gas.ui.*;
import mindustry.world.blocks.environment.*;
import gas.world.consumers.*;
import mindustry.world.blocks.payloads.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.defense.turrets.Turret.*;
import mindustry.world.blocks.logic.*;
import gas.world.modules.*;
import gas.world.blocks.*;
import gas.*;
import gas.io.*;
import gas.world.draw.*;
import gas.gen.*;
import mindustry.world.blocks.defense.turrets.BaseTurret.*;
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

public class GasBaseTurret extends GasBlock {

    public float range = 80f;

    public float placeOverlapMargin = 8 * 7f;

    public float rotateSpeed = 5;

    /**
     * Effect displayed when coolant is used.
     */
    public Effect coolEffect = Fx.fuelburn;

    /**
     * How much reload is lowered by for each unit of liquid of heat capacity.
     */
    public float coolantMultiplier = 5f;

    /**
     * If not null, this consumer will be used for coolant.
     */
    @Nullable
    public ConsumeLiquidBase coolant;

    public GasBaseTurret(String name) {
        super(name);
        update = true;
        solid = true;
        outlineIcon = true;
        attacks = true;
        priority = TargetPriority.turret;
        group = BlockGroup.turrets;
        flags = EnumSet.of(BlockFlag.turret);
    }

    @Override
    public void init() {
        if (coolant == null) {
            coolant = findConsumer(c -> c instanceof ConsumeCoolant);
        }
        // just makes things a little more convenient
        if (coolant != null) {
            // TODO coolant fix
            coolant.update = false;
            coolant.booster = true;
            coolant.optional = true;
        }
        placeOverlapRange = Math.max(placeOverlapRange, range + placeOverlapMargin);
        fogRadius = Math.max(Mathf.round(range / tilesize), fogRadius);
        super.init();
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, range, Pal.placing);
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.shootRange, range / tilesize, StatUnit.blocks);
    }

    public class GasBaseTurretBuild extends GasBuilding implements Ranged {

        public float rotation = 90;

        @Override
        public float range() {
            return range;
        }

        @Override
        public void drawSelect() {
            Drawf.dashCircle(x, y, range(), team.color);
        }
    }
}
