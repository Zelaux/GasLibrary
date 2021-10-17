package gas.entities.compByAnuke;

import mindustry.annotations.Annotations.*;
import mindustry.gen.*;


@gas.annotations.GasAnnotations.Component
abstract class DrawComp implements Posc {

    float clipSize() {
        return Float.MAX_VALUE;
    }

    void draw() {
    }
}