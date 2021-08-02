package gas.world.consumers;

import arc.func.Func;
import arc.scene.ui.layout.Table;
import arc.struct.Bits;
import mindustry.gen.Building;
import mindustry.type.Liquid;
import mindustry.type.LiquidStack;
//import mindustry.ui.Cicon;
import mindustry.ui.ReqImage;
import mindustry.world.consumers.Consume;
import mindustry.world.consumers.ConsumeType;
import mindustry.world.meta.Stats;

public class ConsumeLiquidDynamic extends Consume {
    public final Func<Building, LiquidStack[]> liquids;

    public <T extends Building> ConsumeLiquidDynamic(Func<Building,  LiquidStack[]> liquids) {
        this.liquids = liquids;
    }

    public void applyLiquidFilter(Bits filter) {
    }

    public ConsumeType type() {
        return ConsumeType.liquid;
    }

    public void build(Building tile, Table table) {
        LiquidStack[][] current = new LiquidStack[][]{this.liquids.get(tile)};
        table.table((cont) -> {
            table.update(() -> {
                if (current[0] != this.liquids.get(tile)) {
                    this.rebuild(tile, cont);
                    current[0] = this.liquids.get(tile);
                }

            });
            this.rebuild(tile, cont);
        });
    }
    private boolean hasLiquid(Building tile, Liquid liquid, float amount){
        return tile.liquids.get(liquid)>=amount;
    }
    private void rebuild(Building tile, Table table) {
        table.clear();
        LiquidStack[] var3 = this.liquids.get(tile);
        int var4 = var3.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            LiquidStack stack = var3[var5];
            table.add(new ReqImage(stack.liquid.uiIcon, () -> {
                return tile.items != null && hasLiquid(tile,stack.liquid, stack.amount);
            })).padRight(8.0F);
        }

    }

    public String getIcon() {
        return "icon-liquid-consume";
    }

    public void update(Building entity) {
    }
    public boolean hasLiquidStacks(Building tile,LiquidStack[] stacks) {

        for (LiquidStack stack : stacks) {
            if (!this.hasLiquid(tile, stack.liquid, stack.amount)) {
                return false;
            }
        }

        return true;
    }
    private boolean hasLiquid(Building tile,LiquidStack stack){
        return hasLiquid(tile,stack.liquid,stack.amount);
    }
    public void trigger(Building entity) {
        for (LiquidStack stack : this.liquids.get(entity)) {
            entity.liquids.remove(stack.liquid, stack.amount);
        }

    }

    public boolean valid(Building entity) {
        return entity.liquids != null && hasLiquidStacks(entity,this.liquids.get(entity));
    }

    public void display(Stats stats) {
    }

}
