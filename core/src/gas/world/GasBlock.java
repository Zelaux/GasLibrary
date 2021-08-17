package gas.world;

import acontent.world.meta.AStatUnit;
import acontent.world.meta.AStatValues;
import acontent.world.meta.AStats;
import arc.util.Log;
import gas.annotations.GasAnnotations;
import gas.content.*;
import gas.gen.GasBuilding;
import gas.gen.GasContentRegions;
import gas.type.Gas;
import gas.world.blocks.gas.GasGasBlock.*;
import gas.world.consumers.ConsumeGas;
import gas.world.consumers.GasConsumers;
import arc.Core;
import arc.func.Cons;
import arc.func.Func;
import arc.graphics.Color;
import arc.math.Mathf;
import gas.world.meta.GasStats;
import mindustry.content.Liquids;
import mindustry.core.*;
import mindustry.ctype.UnlockableContent;
import mindustry.gen.Building;
import mindustry.graphics.Pal;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.type.Liquid;
import mindustry.ui.Bar;
import mindustry.world.Block;
import mindustry.world.consumers.ConsumeItems;
import mindustry.world.consumers.ConsumeLiquid;
import mindustry.world.consumers.ConsumePower;
import mindustry.world.consumers.ConsumeType;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.StatValues;

import static mindustry.Vars.tilesize;
@GasAnnotations.GasAddition
public class GasBlock extends Block {
    /** If true, gasBuildings have a GasModule. */
    public boolean hasGas = false;

    public final GasConsumers consumes = new GasConsumers();
    public float gasCapacity;
    public boolean outputsGas = false;
    public AStats aStats = new AStats();

    public GasBlock(String name) {
        super(name);
        super.stats=aStats.copy(stats);
        this.gasCapacity = 10;
    }

    public void getDependencies(Cons<UnlockableContent> cons) {
        for (ItemStack stack : this.requirements) {
            cons.get(stack.item);
        }

        this.consumes.each((c) -> {
            if (!c.isOptional()) {
                ConsumeItems i;
                if (c instanceof ConsumeItems && (i = (ConsumeItems) c) == c) {
                    ItemStack[] var4 = i.items;
                    int var5 = var4.length;

                    for (int var6 = 0; var6 < var5; ++var6) {
                        ItemStack stack = var4[var6];
                        cons.get(stack.item);
                    }
                } else {
                    ConsumeLiquid ix;
                    ConsumeGas ig;
                    if (c instanceof ConsumeLiquid && (ix = (ConsumeLiquid) c) == c) {
                        cons.get(ix.liquid);
                    } else if (c instanceof ConsumeGas && (ig = (ConsumeGas) c) == c) {
                        cons.get(ig.gas);
                    }
                }

            }
        });
    }

    @Override
    public void init() {
//        localizedName = Core.bundle.get(getContentType() + "." + this.name + ".name", localizedName);
//        description = Core.bundle.get(getContentType() + "." + this.name + ".description",description);
//        details = Core.bundle.get(getContentType() + "." + this.name + ".details",details);
        super.init();
        for (ConsumeType value : ConsumeType.values()) {
            if (consumes.has(value)) {
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
        if(consumes.hasGas()) hasGas = true;

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

    public void setStats() {
//        super.setStats();
        aStats.add(Stat.size, "@x@", size, size);
        aStats.add(Stat.health, (float) health, StatUnit.none);
        if (canBeBuilt()) {
            aStats.add(Stat.buildTime, buildCost / 60.0F, StatUnit.seconds);
            aStats.add(Stat.buildCost, StatValues.items(false, requirements));
        }

        if (instantTransfer) {
            aStats.add(Stat.maxConsecutive, 2.0F, StatUnit.none);
        }

        consumes.display(aStats);
        if (hasLiquids) {
            aStats.add(Stat.liquidCapacity, liquidCapacity, StatUnit.liquidUnits);
        }

        if (hasItems && itemCapacity > 0) {
            aStats.add(Stat.itemCapacity, (float) itemCapacity, StatUnit.items);
        }
        if (hasGas && gasCapacity > 0) {
            aStats.add(GasStats.gasCapacity, gasCapacity, AStatUnit.get("gasUnits"));
        }

    }

    @Override
    public void load() {
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
        if(hasGas){
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
}
