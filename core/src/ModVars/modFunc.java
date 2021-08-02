package ModVars;


import arc.Core;
import arc.Events;
import arc.func.Cons;
import arc.graphics.Color;
import arc.scene.ui.Dialog;
import arc.scene.ui.Label;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Collapser;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Strings;
import arc.util.Time;
import mindustry.Vars;
import mindustry.content.TechTree;
import mindustry.ctype.UnlockableContent;
import mindustry.game.EventType;
import mindustry.gen.Building;
import mindustry.ui.Styles;

import java.util.Objects;

import static ModVars.GasVars.modInfo;
import static ModVars.GasVars.packSprites;

public class modFunc {
    public static void inTry(ThrowableRunnable runnable) {
        try {
            runnable.run();
        } catch (Exception ex) {
            showException(ex);
        }
    }

    public static void checkTranslate(UnlockableContent content) {
        content.localizedName = Core.bundle.get(content.getContentType() + "." + content.name + ".name", content.localizedName);
        content.description = Core.bundle.get(content.getContentType() + "." + content.name + ".description", content.description);
        content.details = Core.bundle.get(content.getContentType() + "." + content.name + ".details", content.details);
    }

    public static String fullName(String name) {
        if (packSprites) return name;
        return Strings.format("@-@", modInfo == null ? "gas" : modInfo.name, name);
    }
    private static void showExceptionDialog(final String text, final Throwable exc) {
        (new Dialog("") {
            {
                String message = Strings.getFinalMessage(exc);
                this.setFillParent(true);
                this.cont.margin(15.0F);
                this.cont.add("@error.title").colspan(2);
                this.cont.row();
                this.cont.image().width(300.0F).pad(2.0F).colspan(2).height(4.0F).color(Color.scarlet);
                this.cont.row();
                ((Label) this.cont.add((text.startsWith("@") ? Core.bundle.get(text.substring(1)) : text) + (message == null ? "" : "\n[lightgray](" + message + ")")).colspan(2).wrap().growX().center().get()).setAlignment(1);
                this.cont.row();
                Collapser col = new Collapser((base) -> {
                    base.pane((t) -> {
                        t.margin(14.0F).add(Strings.neatError(exc)).color(Color.lightGray).left();
                    });
                }, true);
                Table var10000 = this.cont;
                TextButton.TextButtonStyle var10002 = Styles.togglet;
                Objects.requireNonNull(col);
                var10000.button("@details", var10002, col::toggle).size(180.0F, 50.0F).checked((b) -> {
                    return !col.isCollapsed();
                }).fillX().right();
                this.cont.button("@ok", this::hide).size(110.0F, 50.0F).fillX().left();
                this.cont.row();
                this.cont.add(col).colspan(2).pad(2.0F);
                this.closeOnBack();
            }
        }).show();
    }

    public static void showException(Exception exception) {
        Log.err(exception);
        try {
            Vars.ui.showException(Strings.format("@: error", modInfo.meta.displayName), exception);
        } catch (NullPointerException n) {
            Events.on(EventType.ClientLoadEvent.class, event -> {
                showExceptionDialog(Strings.format("@: error", modInfo == null ? null : modInfo.meta.displayName), exception);
            });
        }
    }

    public interface ThrowableRunnable {
        void run() throws Exception;
    }
}