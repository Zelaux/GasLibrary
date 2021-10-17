package gas.entities.compByAnuke;

import mindustry.annotations.Annotations.*;
import mindustry.gen.*;


@gas.annotations.GasAnnotations.Component
abstract class RotComp implements Entityc {

    @gas.annotations.GasAnnotations.SyncField(false)
    @gas.annotations.GasAnnotations.SyncLocal
    float rotation;
}