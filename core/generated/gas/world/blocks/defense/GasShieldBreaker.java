package gas.world.blocks.defense;

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
import gas.world.blocks.campaign.*;
import mindustry.world.modules.*;
import gas.ui.*;
import mindustry.world.blocks.environment.*;
import mindustry.*;
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
import gas.world.blocks.storage.*;
import mindustry.world.blocks.units.*;
import mindustry.world.blocks.defense.ShieldBreaker.*;
import gas.world.blocks.production.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;

public class GasShieldBreaker extends GasBlock {

    public Block[] toDestroy = {};

    public Effect effect = Fx.shockwave, breakEffect = Fx.reactorExplosion, selfKillEffect = Fx.massiveExplosion;

    public GasShieldBreaker(String name) {
        super(name);
        solid = update = true;
        rebuildable = false;
    }

    @Override
    public boolean canBreak(Tile tile) {
        return false;
    }

    public class GasShieldBreakerBuild extends GasBuilding {

        @Override
        public void updateTile() {
            if (Mathf.equal(efficiency, 1f)) {
                effect.at(this);
                for (var other : Vars.state.teams.active) {
                    if (team != other.team) {
                        for (var block : toDestroy) {
                            other.getBuildings(block).copy().each(b -> {
                                breakEffect.at(b);
                                b.kill();
                            });
                        }
                    }
                }
                selfKillEffect.at(this);
                kill();
            }
        }
    }
}
