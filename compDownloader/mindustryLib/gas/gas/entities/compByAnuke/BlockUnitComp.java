package gas.entities.compByAnuke;

import arc.graphics.g2d.*;
import mindustry.annotations.Annotations.*;
import mindustry.game.*;
import mindustry.gen.*;

import static mindustry.Vars.*;


@gas.annotations.GasAnnotations.Component
abstract class BlockUnitComp implements Unitc {

    @gas.annotations.GasAnnotations.Import
    Team team;

    @gas.annotations.GasAnnotations.ReadOnly
    transient Building tile;

    public void tile(Building tile) {
        this.tile = tile;
        // sets up block stats
        maxHealth(tile.block.health);
        health(tile.health());
        hitSize(tile.block.size * tilesize * 0.7f);
        set(tile);
    }

    @Override
    public void update() {
        if (tile != null) {
            team = tile.team;
        }
    }

    @gas.annotations.GasAnnotations.Replace
    @Override
    public TextureRegion icon() {
        return tile.block.fullIcon;
    }

    @Override
    public void killed() {
        tile.kill();
    }

    @gas.annotations.GasAnnotations.Replace
    public void damage(float v, boolean b) {
        tile.damage(v, b);
    }

    @gas.annotations.GasAnnotations.Replace
    public boolean dead() {
        return tile == null || tile.dead();
    }

    @gas.annotations.GasAnnotations.Replace
    public boolean isValid() {
        return tile != null && tile.isValid();
    }

    @gas.annotations.GasAnnotations.Replace
    public void team(Team team) {
        if (tile != null && this.team != team) {
            this.team = team;
            if (tile.team != team) {
                tile.team(team);
            }
        }
    }
}