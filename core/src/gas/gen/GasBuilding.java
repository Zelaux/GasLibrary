package gas.gen;

import arc.math.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import arc.util.io.*;
import gas.entities.*;
import gas.type.*;
import gas.world.*;
import gas.world.modules.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.entities.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.modules.*;

import static mindustry.Vars.*;

public class GasBuilding extends Building{
    public GasModule gasses;
    public GasBlock block;
    public float smoothGas;

    protected GasBuilding(){
        block = (GasBlock)super.block;
    }

    public void handleGas(Building source, Gas gas, float amount){
        gasses.add(gas, amount);
    }

    public boolean acceptGas(Building source, Gas gas){
        if(source instanceof GasBuilding || source == null){
            return block.hasGasses && block.gasFilter[gas.id];
        }
        return false;
    }

    public double sense(Content content){
        if(content instanceof Item && items != null){
            return items.get((Item)content);
        }else if(content instanceof Gas && gasses != null){
            return gasses.get((Gas)content);
        }else{
            return content instanceof Liquid && liquids != null ? (double)liquids.get((Liquid)content) : 0.0D;
        }
    }

    @Override
    public void onDestroyed(){
        float explosiveness = block.baseExplosiveness;
        float flammability = 0f;
        float power = 0f;

        if(block.hasItems){
            for(Item item : content.items()){
                int amount = Math.min(items.get(item), explosionItemCap());
                explosiveness += item.explosiveness * amount;
                flammability += item.flammability * amount;
                power += item.charge * Mathf.pow(amount, 1.1f) * 150f;
            }
        }

        if(block.hasLiquids){
            flammability += liquids.sum((liquid, amount) -> liquid.flammability * amount / 2f);
            explosiveness += liquids.sum((liquid, amount) -> liquid.explosiveness * amount / 2f);
        }

        if(block.hasGasses){
            flammability += gasses.sum((gas, amountx) -> {
                return gas.flammability * amountx / 2.0F;
            });
            explosiveness += gasses.sum((gas, amountx) -> {
                return gas.explosiveness * amountx / 2.0F;
            });
        }
        if(block.consPower != null && block.consPower.buffered){
            power += this.power.status * block.consPower.capacity;
        }

        if(block.hasLiquids && state.rules.damageExplosions){

            liquids.each((liquid, amount) -> {
                float splash = Mathf.clamp(amount / 4f, 0f, 10f);

                for(int i = 0; i < Mathf.clamp(amount / 5, 0, 30); i++){
                    Time.run(i / 2f, () -> {
                        Tile other = world.tileWorld(x + Mathf.range(block.size * tilesize / 2), y + Mathf.range(block.size * tilesize / 2));
                        if(other != null){
                            Puddles.deposit(other, liquid, splash);
                        }
                    });
                }
            });
        }
        if(block.hasGasses && Vars.state.rules.damageExplosions){
            gasses.each((gas, amountx) -> {
                float splash = Mathf.clamp(amountx / 4.0F, 0.0F, 10.0F);
                for(int i = 0; (float)i < Mathf.clamp(amountx / 5.0F, 0.0F, 30.0F); ++i){
                    Time.run((float)i / 2.0F, () -> {
                        Tile other = Vars.world.tile(tileX() + Mathf.range(block.size / 2), tileY() + Mathf.range(block.size / 2));
                        if(other != null){
                            Clouds.deposit(other, gas, splash);
                        }

                    });
                }

            });
        }

        Damage.dynamicExplosion(x, y, flammability, explosiveness * 3.5f, power, tilesize * block.size / 2f, state.rules.damageExplosions, block.destroyEffect);

        if(!floor().solid && !floor().isLiquid){
            Effect.rubble(x, y, block.size);
        }
    }

    public GasModule gasses(){
        return gasses;
    }

    public void gasses(GasModule gasses){
        this.gasses = gasses;
    }

    public GasBuilding getGasDestination(Building from, Gas gas){
        return this;
    }

    public boolean canDumpGas(Building to, Gas gas){
        return true;
    }

    public void transferGas(Building n, float amount, Gas gas){
        if(!(n instanceof GasBuilding)) return;
        GasBuilding next = (GasBuilding)n;
        float flow = Math.min(next.block.gasCapacity - next.gasses.get(gas), amount);
        if(next.acceptGas(this, gas)){
            next.handleGas(this, gas, flow);
            gasses.remove(gas, flow);
        }
    }

    @Override
    public void displayBars(Table table){
        super.displayBars(table);
    }

    @Override
    public GasBlock block(){
        return block;
    }

    @Override
    public GasBuilding create(Block block, Team team){
        return create((GasBlock)block, team);
    }

    public GasBuilding create(GasBlock block, Team team){
        super.create(block, team);
        if(block.hasGasses){
            gasses = new GasModule();
        }
        this.block=block;
        return this;
    }

    public void dumpGas(Gas gas){
        dumpGas(gas, 2f);
    }

    public void dumpGas(Gas gas, float scaling){
        int dump = cdump;
        if(gasses.get(gas) > 0.0001f){
            if(!Vars.net.client() && Vars.state.isCampaign() && team == Vars.state.rules.defaultTeam){
                gas.unlock();
            }

            for(int i = 0; i < proximity.size; ++i){
                incrementDump(proximity.size);
                Building o = proximity.get((i + dump) % proximity.size);
                if(!(o instanceof GasBuilding)) continue;
                GasBuilding other = (GasBuilding)o;
                other = other.getGasDestination(this, gas);
                if(other != null && other.team == team && other.block.hasGasses && canDumpGas(other, gas) && other.gasses != null){
                    float ofract = other.gasses.get(gas) / other.block.gasCapacity;
                    float fract = gasses.get(gas) / block.gasCapacity;
                    if(ofract < fract){
                        transferGas(other, (fract - ofract) * block.gasCapacity / scaling, gas);
                    }
                }
            }

        }
    }

    @Override
    public String toString(){
        return "GasBuilding#" + id;
    }

    public float moveGas(Building n, Gas gas){
        if(!(n instanceof GasBuilding)) return 0f;
        GasBuilding next = (GasBuilding)n;
        next = next.getGasDestination(this, gas);
        float gasAmount = gasses.get(gas);
        if(next.team != team || !next.block.hasGasses || !(gasAmount > 0.0F)) return 0;

        float ofract = next.gasses.get(gas) / next.block.gasCapacity;
        float fract = gasAmount / block.gasCapacity;
        float flow = Math.min(Mathf.clamp((fract - ofract) * 1.0F) * block.gasCapacity, gasAmount);
        flow = Math.min(flow, next.block.gasCapacity - next.gasses.get(gas));

        if(flow > 0.0F && ofract <= fract && next.acceptGas(this, gas)){
            next.handleGas(this, gas, flow);
            gasses.remove(gas, flow);
            return flow;
        }
        if(next.gasses.currentAmount() / next.block.gasCapacity > 0.1F && fract > 0.1F){
            float fx = (x + next.x) / 2.0F;
            float fy = (y + next.y) / 2.0F;
            Gas other = next.gasses.current();
            if(other.flammability > 0.3F && gas.temperature > 0.7F || gas.flammability > 0.3F && other.temperature > 0.7F){
                damage(1.0F * Time.delta);
                next.damage(1.0F * Time.delta);
                if(Mathf.chance(0.1D * (double)Time.delta)){
                    Fx.fire.at(fx, fy);
                }
            }else if(gas.temperature > 0.7F && other.temperature < 0.55F || other.temperature > 0.7F && gas.temperature < 0.55F){
                gasses.remove(gas, Math.min(gasAmount, 0.7F * Time.delta));
                if(Mathf.chance((double)(0.2F * Time.delta))){
                    Fx.steam.at(fx, fy);
                }
            }
        }
        return 0f;
    }

    public float moveGasForward(boolean leaks, Gas gas){
        Building next = front();
        Tile nextTile = tile.nearby(rotation);
        if((next instanceof GasBuilding)){
            return moveGas(next, gas);
        }
        if(nextTile != null && leaks && !nextTile.block().solid && !(nextTile.block() instanceof GasBlock gasBlock && gasBlock.hasGasses)){
            float leakAmount = gasses.get(gas) / 1.5F;

            Clouds.deposit(nextTile, tile, gas, leakAmount);
            gasses.remove(gas, leakAmount);
        }
        return 0.0F;
    }

    @Override
    public final void writeBase(Writes write){
        boolean writeVisibility = state.rules.fog && visibleFlags != 0;

        write.f(health);
        write.b(rotation | 0b10000000);
        write.b(team.id);
        write.b(writeVisibility ? 4 : 3); //version
        write.b(enabled ? 1 : 0);
        //write presence of items/power/liquids/cons, so removing/adding them does not corrupt future saves.
        write.b(moduleBitmask());
        if(items != null) items.write(write);
        if(power != null) power.write(write);
        if(liquids != null) liquids.write(write);
        if(gasses != null) gasses.write(write);

        //efficiency is written as two bytes to save space
        write.b((byte)(Mathf.clamp(efficiency) * 255f));
        write.b((byte)(Mathf.clamp(optionalEfficiency) * 255f));

        //only write visibility when necessary, saving 8 bytes - implies new version
        if(writeVisibility){
            write.l(visibleFlags);
        }
    }

    @Override
    public final void readBase(Reads read){
        //cap health by block health in case of nerfs
        health = Math.min(read.f(), block.health);
        byte rot = read.b();
        team = Team.get(read.b());

        rotation = rot & 0b01111111;

        int moduleBits = moduleBitmask();
        boolean legacy = true;
        byte version = 0;

        //new version
        if((rot & 0b10000000) != 0){
            version = read.b(); //version of entity save
            if(version >= 1){
                byte on = read.b();
                this.enabled = on == 1;
            }

            //get which modules should actually be read; this was added in version 2
            if(version >= 2){
                moduleBits = read.b();
            }
            legacy = false;
        }

        if((moduleBits & 0b0001) != 0) (items == null ? new ItemModule() : items).read(read, legacy);
        if((moduleBits & 0b0010) != 0) (power == null ? new PowerModule() : power).read(read, legacy);
        if((moduleBits & 0b0100) != 0) (liquids == null ? new LiquidModule() : liquids).read(read, legacy);
        if((moduleBits & 0b1000) != 0) (gasses == null ? new GasModule() : gasses).read(read, legacy);

        //unnecessary consume module read in version 2 and below
        if(version <= 2) read.bool();

        //version 3 has efficiency numbers instead of bools
        if(version >= 3){
            efficiency = potentialEfficiency = read.ub() / 255f;
            optionalEfficiency = read.ub() / 255f;
        }

        //version 4 (and only 4 at the moment) has visibility flags
        if(version == 4){
            visibleFlags = read.l();
        }
    }

    @Override
    public int moduleBitmask(){
        return (items != null ? 1 : 0) | (power != null ? 0b10 : 0) | (liquids != null ? 0b100 : 0) | (gasses != null ? 0b1000 : 0) | 0b1_0000;
    }
}
