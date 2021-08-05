package gas.entities;

import gas.type.Gas;
import arc.math.Mathf;
import arc.struct.IntMap;
import arc.util.Time;
import gas.gen.Cloud;
import mindustry.content.Bullets;
import mindustry.content.Fx;
import mindustry.entities.Fires;
import mindustry.game.Team;
import mindustry.world.Tile;

public class Clouds {
    private static final IntMap<Cloud> map = new IntMap();
    public static final float maxGas = 70.0F;

    public Clouds() {
    }

    public static void deposit(Tile tile, Tile source, Gas gas, float amount) {
        deposit(tile, source, gas, amount,true);
    }

    public static void deposit(Tile tile, Gas gas, float amount) {
        deposit(tile, tile, gas, amount, true);
    }

    public static Cloud get(Tile tile) {
        return map.get(tile.pos());
    }

    public static void deposit(Tile tile, Tile source, Gas gas, float amount,boolean initial) {
        if(tile == null) return;

        if(tile.floor().solid){
            return;
        }

        Cloud p = map.get(tile.pos());
        if(p == null){
            Cloud cloud = Cloud.create();
            cloud.tile = tile;
            cloud.gas = gas;
            cloud.amount = amount;
            cloud.set((tile.worldx() + source.worldx()) / 2f, (tile.worldy() + source.worldy()) / 2f);
            cloud.add();
            map.put(tile.pos(), cloud);
        }else if(p.gas == gas){
            p.accepting = Math.max(amount, p.accepting);

            if(initial && p.lastRipple <= Time.time - 40f && p.amount >= maxGas / 2f){
                Fx.ripple.at((tile.worldx() + source.worldx()) / 2f, (tile.worldy() + source.worldy()) / 2f, 1f, p.gas.color);
                p.lastRipple = Time.time;
            }
        }else{
            p.amount += reactCloud(p.gas, gas, amount, p.tile, (p.x + source.worldx()) / 2f, (p.y + source.worldy()) / 2f);
        }
    }

    public static void remove(Tile tile) {
        if (tile != null) {
            map.remove(tile.pos());
        }
    }

    public static void register(Cloud cloud) {
        map.put(cloud.tile().pos(), cloud);
    }

    private static float reactCloud(Gas dest, Gas gas, float amount, Tile tile, float x, float y) {
        if (dest==null || gas==null)return 0.0F;
        if (dest.flammability > 0.3F && gas.temperature > 0.7F || gas.flammability > 0.3F && dest.temperature > 0.7F) {
            Fires.create(tile);
            if (Mathf.chance(0.006D * (double)amount)) {
                Bullets.fireball.createNet(Team.derelict, x, y, Mathf.random(360.0F), -1.0F, 1.0F, 1.0F);
            }
        } else {
            if (dest.temperature > 0.7F && gas.temperature < 0.55F) {
                if (Mathf.chance((0.5F * amount))) {
                    Fx.steam.at(x, y);
                }

                return -0.1F * amount;
            }

            if (gas.temperature > 0.7F && dest.temperature < 0.55F) {
                if (Mathf.chance((0.8F * amount))) {
                    Fx.steam.at(x, y);
                }

                return -0.4F * amount;
            }
        }

        return 0.0F;
    }
}
