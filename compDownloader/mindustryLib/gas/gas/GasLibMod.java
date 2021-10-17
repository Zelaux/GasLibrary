package gas;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import arc.struct.Seq;
import gas.annotations.GasAnnotations;
import gas.core.GasContentLoader;
import gas.gen.*;
import mindustry.Vars;
import mindustry.ctype.Content;
import mindustry.ctype.UnlockableContent;
import mindustry.mod.Mod;

import static gas.GasVars.*;
import static mindustry.Vars.*;
@GasAnnotations.CashAnnotation2
public class GasLibMod extends Mod {


    public GasLibMod() {
        if (!GasDependencies.valid())return;
        GasEntityMapping.init();
        gasLibInfo = Vars.mods.getMod(getClass());
    }

    public static TextureRegion getIcon() {
        if (gasLibInfo == null || gasLibInfo.iconTexture == null) return Core.atlas.find("nomap");
        return new TextureRegion(gasLibInfo.iconTexture);
    }

    public static boolean inPackage(String packageName, Object obj) {
        if (packageName == null || obj == null) return false;
        String name;
        try {
            name = obj.getClass().getPackage().getName();
        } catch (Exception e) {
            return false;
        }
        return name.startsWith(packageName + ".");
    }

    public void init() {
        if (!GasDependencies.valid())return;
        if (!loaded) return;
        Seq<Content> all = Seq.with(content.getContentMap()).<Content>flatten().select(c -> c.minfo.mod == gasLibInfo).as();
        for (Content c : all) {
            if (inPackage("gas", c)) {
                if (c instanceof UnlockableContent) checkTranslate((UnlockableContent) c);
            }
        }
    }

    public void loadContent() {
        gasLibInfo = Vars.mods.getMod(this.getClass());
        if (!GasDependencies.valid()) {
            if (gasLibInfo != null){
                gasLibInfo.missingDependencies.addAll(gasLibInfo.dependencies.select(mod->!mod.enabled()).map(l->l.name));
            }
            return;
        }
        if (gasLibInfo.dependencies.count(l->l.enabled()) != gasLibInfo.dependencies.size) {
            return;
        }

        new GasContentLoader((load) -> {
            load.load();
        });
        loaded = true;
    }
}
