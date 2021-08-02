package gas.content;

import gas.type.Gas;
import arc.graphics.Color;
import mindustry.ctype.ContentList;

public class Gasses implements ContentList {
    public static Gas methane;

    public void load() {
        methane = new Gas("methane") {{
            this.localizedName = "Methane";
            this.color = Color.valueOf("bcf9ff");
            this.flammability = 0.7f;
            this.explosiveness = 0.9f;
        }};
    }
}
