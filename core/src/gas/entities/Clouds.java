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
        deposit(tile, source, gas, amount, 0);
    }

    public static void deposit(Tile tile, Gas gas, float amount) {
        deposit(tile, tile, gas, amount, 0);
    }

    public static Cloud get(Tile tile) {
        return map.get(tile.pos());
    }

    public static void deposit(Tile tile, Tile source, Gas gas, float amount, int generation) {
        if (tile != null) {
            Cloud c = map.get(tile.pos());
            if (c == null) {
                Cloud cloud = Cloud.create();
                cloud.tile(tile);
                cloud.gas(gas);
                cloud.amount(amount);
                cloud.generation(generation);
                cloud.set((tile.worldx() + source.worldx()) / 2.0F, (tile.worldy() + source.worldy()) / 2.0F);
                cloud.add();
                map.put(tile.pos(), cloud);
            } else if (c.gas() == gas) {
                c.accepting(Math.max(amount, c.accepting()));
                if (generation == 0 && c.lastRipple <= Time.time - 40.0F && c.amount() >= 35.0F) {
//                        Fx.ripple.at((tile.worldx() + source.worldx()) / 2.0F, (tile.worldy() + source.worldy()) / 2.0F, 1.0F, c.gas().color);
                    c.lastRipple = Time.time;
                }
            } else {
                c.amount(c.amount() + reactPuddle(c.gas(), gas, amount, c.tile(), (c.x() + source.worldx()) / 2.0F, (c.y() + source.worldy()) / 2.0F));
            }

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

    private static float reactPuddle(Gas dest, Gas gas, float amount, Tile tile, float x, float y) {
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

    private static boolean canStayOn(Gas gas, Gas other) {
        return false;
//        return gas == Liquids.oil && other == Liquids.water;
    }
}
