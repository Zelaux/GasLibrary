package gas.world;

import acontent.world.meta.*;
import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.math.*;
import gas.annotations.*;
import gas.annotations.GasAnnotations.*;
import gas.gen.*;
import gas.type.*;
import gas.world.consumers.*;
import gas.world.meta.*;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.ctype.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;

import static mindustry.Vars.tilesize;

@GasAnnotations.GasAddition
public class GasBlock extends Block{
    @Load("noon")
    public final GasConsumers consumes = new GasConsumers();
    /** If true, gasBuildings have a GasModule. */
    public boolean hasGasses = false;
    public float gasCapacity;
    public boolean outputsGas = false;
    public AStats aStats = new AStats();

    public GasBlock(String name){
        super(name);
        super.stats = aStats.copy(stats);
        this.gasCapacity = 10;
    }

    public void getDependencies(Cons<UnlockableContent> cons){
        for(ItemStack stack : this.requirements){
            cons.get(stack.item);
        }

        this.consumes.each((c) -> {
            if(!c.isOptional()){
                ConsumeItems i;
                if(c instanceof ConsumeItems && (i = (ConsumeItems)c) == c){
                    ItemStack[] var4 = i.items;
                    int var5 = var4.length;

                    for(int var6 = 0; var6 < var5; ++var6){
                        ItemStack stack = var4[var6];
                        cons.get(stack.item);
                    }
                }else{
                    ConsumeLiquid ix;
                    ConsumeGas ig;
                    if(c instanceof ConsumeLiquid && (ix = (ConsumeLiquid)c) == c){
                        cons.get(ix.liquid);
                    }else if(c instanceof ConsumeGas && (ig = (ConsumeGas)c) == c){
                        cons.get(ig.gas);
                    }
                }

            }
        });
    }

    @Override
    public void init(){
//        localizedName = Core.bundle.get(getContentType() + "." + this.name + ".name", localizedName);
//        description = Core.bundle.get(getContentType() + "." + this.name + ".description",description);
//        details = Core.bundle.get(getContentType() + "." + this.name + ".details",details);
        super.init();
        for(ConsumeType value : ConsumeType.values()){
            if(consumes.has(value)){
                super.consumes.add(consumes.get(value));
            }
        }
        if(health == -1){
            health = size * size * 40;
        }
        clipSize = Math.max(clipSize, size * tilesize);

        if(emitLight){
            clipSize = Math.max(clipSize, lightRadius * 2f);
        }
        if(group == BlockGroup.transportation || consumes.has(ConsumeType.item) || category == Category.distribution){
            acceptsItems = true;
        }

        offset = ((size + 1) % 2) * tilesize / 2f;

        buildCost = 0f;
        for(ItemStack stack : requirements){
            buildCost += stack.amount * stack.item.cost;
        }
        buildCost *= buildCostMultiplier;

        if(consumes.has(ConsumeType.power)) hasPower = true;
        if(consumes.has(ConsumeType.item)) hasItems = true;
        if(consumes.has(ConsumeType.liquid)) hasLiquids = true;
        if(consumes.hasGas()) hasGasses = true;

        setBars();

        stats.useCategories = true;

        consumes.init();
        super.consumes.init();

        if(!logicConfigurable){
            configurations.each((key, val) -> {
                if(UnlockableContent.class.isAssignableFrom(key)){
                    logicConfigurable = true;
                }
            });
        }

        if(!outputsPower && consumes.hasPower() && consumes.getPower().buffered){
            throw new IllegalArgumentException("Consumer using buffered power: " + name);
        }
    }

    public void setStats(){
//        super.setStats();
        aStats.add(Stat.size, "@x@", size, size);
        aStats.add(Stat.health, (float)health, StatUnit.none);
        if(canBeBuilt()){
            aStats.add(Stat.buildTime, buildCost / 60.0F, StatUnit.seconds);
            aStats.add(Stat.buildCost, StatValues.items(false, requirements));
        }

        if(instantTransfer){
            aStats.add(Stat.maxConsecutive, 2.0F, StatUnit.none);
        }

        consumes.display(aStats);
        if(hasLiquids){
            aStats.add(Stat.liquidCapacity, liquidCapacity, StatUnit.liquidUnits);
        }

        if(hasItems && itemCapacity > 0){
            aStats.add(Stat.itemCapacity, (float)itemCapacity, StatUnit.items);
        }
        if(hasGasses && gasCapacity > 0){
            aStats.add(GasStats.gasCapacity, gasCapacity, AStatUnit.get("gasUnits"));
        }

    }

    @Override
    public void load(){
        super.load();
        GasContentRegions.loadRegions(this);
    }

    @Override
    public void setBars(){
        bars.add("health", entity -> new Bar("stat.health", Pal.health, entity::healthf).blink(Color.white));

        if(hasLiquids){
            Func<Building, Liquid> current;
            if(consumes.has(ConsumeType.liquid) && consumes.get(ConsumeType.liquid) instanceof ConsumeLiquid){
                Liquid liquid = consumes.<ConsumeLiquid>get(ConsumeType.liquid).liquid;
                current = entity -> liquid;
            }else{
                current = entity -> entity.liquids == null ? Liquids.water : entity.liquids.current();
            }
            bars.add("liquid", entity -> new Bar(() -> entity.liquids.get(current.get(entity)) <= 0.001f ? Core.bundle.get("bar.liquid") : current.get(entity).localizedName,
            () -> current.get(entity).barColor(), () -> entity == null || entity.liquids == null ? 0f : entity.liquids.get(current.get(entity)) / liquidCapacity));
        }

        if(hasPower && consumes.hasPower()){
            ConsumePower cons = consumes.getPower();
            boolean buffered = cons.buffered;
            float capacity = cons.capacity;

            bars.add("power", entity -> new Bar(() -> buffered ? Core.bundle.format("bar.poweramount", Float.isNaN(entity.power.status * capacity) ? "<ERROR>" : UI.formatAmount((int)(entity.power.status * capacity))) :
            Core.bundle.get("bar.power"), () -> Pal.powerBar, () -> Mathf.zero(cons.requestedPower(entity)) && entity.power.graph.getPowerProduced() + entity.power.graph.getBatteryStored() > 0f ? 1f : entity.power.status));
        }

        if(hasItems && configurable){
            bars.add("items", entity -> new Bar(() -> Core.bundle.format("bar.items", entity.items.total()), () -> Pal.items, () -> (float)entity.items.total() / itemCapacity));
        }

        if(unitCapModifier != 0){
            stats.add(Stat.maxUnits, (unitCapModifier < 0 ? "-" : "+") + Math.abs(unitCapModifier));
        }
        if(hasGasses){
            Func<GasBuilding, Gas> current;
            if(consumes.has(ConsumeType.liquid) && consumes.get(ConsumeType.liquid) instanceof ConsumeGas){
                Gas gas = consumes.<ConsumeGas>getGas().gas;
                current = entity -> gas;
            }else{
                current = entity -> entity.gasses == null ? null : entity.gasses.current();
            }
            bars.<GasBuilding>add("liquid", entity -> new Bar(() -> entity.gasses.get(current.get(entity)) <= 0.001f ? Core.bundle.get("bar.liquid") : current.get(entity).localizedName,
            () -> current.get(entity).barColor(), () -> entity == null || entity.liquids == null ? 0f : entity.gasses.get(current.get(entity)) / liquidCapacity));

        }
    }

    public boolean positionsValid(int x, int y, int x1, int y1){
        return false;
    }
}
