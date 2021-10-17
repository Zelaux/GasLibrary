package gas.entities.compByAnuke;

import arc.util.*;
import mindustry.annotations.Annotations.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.world.blocks.storage.CoreBlock.*;

import static mindustry.Vars.*;


@gas.annotations.GasAnnotations.Component
abstract class TeamComp implements Posc {

    @gas.annotations.GasAnnotations.Import
    float x, y;

    Team team = Team.derelict;

    public boolean cheating() {
        return team.rules().cheat;
    }

    @Nullable
    public CoreBuild core() {
        return team.core();
    }

    @Nullable
    public CoreBuild closestCore() {
        return state.teams.closestCore(x, y, team);
    }

    @Nullable
    public CoreBuild closestEnemyCore() {
        return state.teams.closestEnemyCore(x, y, team);
    }
}