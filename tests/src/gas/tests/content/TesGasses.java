package gas.tests.content;

import gas.type.*;
import mindustry.content.*;

public class TesGasses{
    public static Gas testHydrogen;

    public static void load(){
        testHydrogen = new Gas("test-hydrogen", Liquids.hydrogen.color){
            {
                flammability = 1f;
                localizedName = "Test Hydrogen";
            }

            @Override
            public void load(){
                super.load();
                this.fullIcon = Liquids.hydrogen.fullIcon;
                uiIcon = Liquids.hydrogen.uiIcon;
            }
        };
    }
}
