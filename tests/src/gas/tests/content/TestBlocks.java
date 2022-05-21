package gas.tests.content;

import arc.util.*;
import gas.world.blocks.gas.*;
import mindustry.content.*;
import mindustry.type.*;
import mindustry.world.*;
import mma.type.*;

import static mindustry.type.ItemStack.with;

public class TestBlocks{
    public static Block testConduit;

    public static void load(){
        testConduit=new GasConduit("test-conduit"){{

            size = 1;
            requirements(Category.crafting, with(Items.copper, 3));
        }};
    }
}
