package gas.entities.compByAnuke;

import mindustry.annotations.Annotations.*;
import mindustry.entities.*;
import mindustry.entities.EntityCollisions.*;
import mindustry.gen.*;


@gas.annotations.GasAnnotations.Component
abstract class ElevationMoveComp implements Velc, Posc, Flyingc, Hitboxc {

    @gas.annotations.GasAnnotations.Import
    float x, y;

    @gas.annotations.GasAnnotations.Replace
    @Override
    public SolidPred solidity() {
        return isFlying() ? null : EntityCollisions::solid;
    }
}