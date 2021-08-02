package gas.world.blocks;

import arc.func.Cons;
import arc.func.Prov;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.ButtonGroup;
import arc.scene.ui.ImageButton;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.gen.Tex;
//import mindustry.ui.Cicon;
import mindustry.ui.Styles;

public class ModSelection {
    private static float scrollPos = 0.0F;

    public static <T> void buildTable(Table table, Seq<String> items, Prov<Integer> holder, Cons<Integer> consumer) {
        buildTable(table, items, holder, consumer, true);
    }

    public static <T> void buildTable(Table table, Seq<String> items, Prov<Integer> holder, Cons<Integer> consumer, boolean closeSelect) {
        ButtonGroup<ImageButton> group = new ButtonGroup<>();
        group.setMinCheckCount(0);
        Table cont = new Table();
        cont.defaults().size(40.0F);
        int i = 0;

        for (String item : items) {
            ImageButton button = cont.button(Tex.whiteui, Styles.clearToggleTransi, 24.0F, () -> {
                if (closeSelect) {
                    Vars.control.input.frag.config.hideConfig();
                }

            }).group(group).get();
            button.changed(() -> {
                consumer.get(button.isChecked() ? items.indexOf(item) : -1);
            });
            button.add(item);
            button.update(() -> {
                button.setChecked(holder.get() == items.indexOf(item));
            });
            if (i++ % 4 == 3) {
                cont.row();
            }

        }

        if (i % 4 != 0) {
            int remaining = 4 - i % 4;

            for (int j = 0; j < remaining; ++j) {
//                cont.image(Styles.black6);
            }
        }

        ScrollPane pane = new ScrollPane(cont, Styles.smallPane);
        pane.setScrollingDisabled(true, false);
        pane.setScrollYForce(scrollPos);
        pane.update(() -> {
            scrollPos = pane.getScrollY();
        });
        pane.setOverscroll(false, false);
        table.add(pane).maxHeight(Scl.scl(200.0F));
    }
}
