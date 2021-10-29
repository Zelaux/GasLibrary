package gas.content;

import gas.world.blocks.sandbox.*;
import mindustry.ctype.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.meta.*;

import static mindustry.type.ItemStack.with;

public class GasBlocks implements ContentList{
    public static Block gasSource;
    public static Block gasVoid;

    public void load(){
        gasSource = new GasSource("gas-source"){{
//            localizedName = "Gas Source";
//            description = "Infinitely outputs gasses. Sandbox only.";
            requirements(Category.liquid, BuildVisibility.sandboxOnly, with());
            alwaysUnlocked = true;
        }};
        gasVoid = new GasVoid("gas-void"){{
//            localizedName = "Gas Void";
//            description = "Removes any liquids. Sandbox only.";
            requirements(Category.liquid, BuildVisibility.sandboxOnly, with());
            alwaysUnlocked = true;
        }};
    }
}
