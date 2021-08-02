package gas.world.consumers;

import arc.func.Func;
import arc.scene.ui.layout.Table;
import arc.struct.Bits;
import mindustry.gen.Building;
import mindustry.type.ItemStack;
//import mindustry.ui.Cicon;
import mindustry.ui.ItemImage;
import mindustry.ui.ReqImage;
import mindustry.world.consumers.Consume;
import mindustry.world.consumers.ConsumeType;
import mindustry.world.meta.Stats;

public class ModConsumeItemDynamic extends Consume {
    public final Func<Building, ItemStack[]> items;

    public  ModConsumeItemDynamic(Func<Building, ItemStack[]> items) {
        this.items = items;
    }

    public void applyItemFilter(Bits filter) {
    }

    public ConsumeType type() {
        return ConsumeType.item;
    }

    public void build(Building tile, Table table) {
        ItemStack[][] current = new ItemStack[][]{(ItemStack[])this.items.get(tile)};
        table.table((cont) -> {
            table.update(() -> {
                if (current[0] != this.items.get(tile)) {
                    this.rebuild(tile, cont);
                    current[0] = (ItemStack[])this.items.get(tile);
                }

            });
            this.rebuild(tile, cont);
        });
    }

    private void rebuild(Building tile, Table table) {
        table.clear();
        int i = 0;
        ItemStack[] var4 = (ItemStack[])this.items.get(tile);
        int var5 = var4.length;

        for(int var6 = 0; var6 < var5; ++var6) {
            ItemStack stack = var4[var6];
            table.add(new ReqImage(new ItemImage(stack.item.uiIcon, stack.amount), () -> {
                return tile.items != null && tile.items.has(stack.item, stack.amount);
            })).padRight(8.0F).left();
            ++i;
            if (i % 4 == 0) {
                table.row();
            }
        }

    }

    public String getIcon() {
        return "icon-item";
    }

    public void update(Building entity) {
    }

    public void trigger(Building entity) {
        ItemStack[] var2 = (ItemStack[])this.items.get(entity);
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            ItemStack stack = var2[var4];
            entity.items.remove(stack);
        }

    }

    public boolean valid(Building entity) {
        return entity.items != null && entity.items.has((ItemStack[])this.items.get(entity));
    }

    public void display(Stats stats) {
    }
}

