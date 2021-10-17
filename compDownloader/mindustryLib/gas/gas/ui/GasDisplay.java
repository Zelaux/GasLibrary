package gas.ui;

import gas.type.Gas;
import arc.graphics.Color;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import arc.util.Strings;
//import mindustry.ui.Cicon;
import mindustry.ui.Styles;
import mindustry.world.meta.StatUnit;

public class GasDisplay extends Table {
    public final Gas gas;
    public final float amount;
    public final boolean perSecond;

    public GasDisplay(final Gas gas, final float amount, boolean perSecond) {
        this.gas = gas;
        this.amount = amount;
        this.perSecond = perSecond;
        this.add(new Stack() {
            {
                this.add(new Image(gas.uiIcon));
                if (amount != 0.0F) {
                    Table t = (new Table()).left().bottom();
                    t.add(Strings.autoFixed(amount, 1)).style(Styles.outlineLabel);
                    this.add(t);
                }

            }
        }).size(32.0F).padRight((float)(3 + (amount != 0.0F && Strings.autoFixed(amount, 1).length() > 2 ? 8 : 0)));
        if (perSecond) {
            this.add(StatUnit.perSecond.localized()).padLeft(2.0F).padRight(5.0F).color(Color.lightGray).style(Styles.outlineLabel);
        }
        this.add(gas.localizedName);
    }
}
