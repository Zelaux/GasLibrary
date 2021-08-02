package gas.entities.compByAnuke;

import arc.util.*;
import mindustry.annotations.Annotations.*;
import mindustry.gen.*;


@gas.annotations.GasAnnotations.Component
abstract class ChildComp implements Posc {

    @gas.annotations.GasAnnotations.Import
    float x, y;

    @Nullable
    Posc parent;

    float offsetX, offsetY;

    @Override
    public void add() {
        if (parent != null) {
            offsetX = x - parent.getX();
            offsetY = y - parent.getY();
        }
    }

    @Override
    public void update() {
        if (parent != null) {
            x = parent.getX() + offsetX;
            y = parent.getY() + offsetY;
        }
    }
}