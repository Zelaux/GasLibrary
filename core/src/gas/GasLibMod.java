package gas;

import ModVars.GasVars;
import arc.Core;
import arc.graphics.g2d.TextureRegion;
import arc.struct.Seq;
import arc.util.Log;
import gas.annotations.GasAnnotations;
import gas.core.GasContentLoader;
import gas.gen.*;
import mindustry.Vars;
import mindustry.ctype.Content;
import mindustry.ctype.UnlockableContent;
import mindustry.mod.Mod;
import mindustry.mod.Mods;

import static ModVars.modFunc.*;
import static ModVars.GasVars.*;
import static mindustry.Vars.*;
@GasAnnotations.CashAnnotation2
public class GasLibMod extends Mod {


    public GasLibMod() {
        if (!GasDependencies.valid())return;
        Log.info("Gaslib created");
        GasEntityMapping.init();
        modInfo = Vars.mods.getMod(getClass());
        GasVars.load();
    }

    public static TextureRegion getIcon() {
        if (modInfo == null || modInfo.iconTexture == null) return Core.atlas.find("nomap");
        return new TextureRegion(modInfo.iconTexture);
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
        Seq<Content> all = Seq.with(content.getContentMap()).<Content>flatten().select(c -> c.minfo.mod == modInfo).as();
        for (Content c : all) {
            if (inPackage("gas", c)) {
                if (c instanceof UnlockableContent) checkTranslate((UnlockableContent) c);
            }
        }
    }

    public void loadContent() {
        modInfo = Vars.mods.getMod(this.getClass());
        if (!GasDependencies.valid()) {
            if (modInfo!=null){
                modInfo.missingDependencies.addAll(modInfo.dependencies.select(mod->!mod.enabled()).map(l->l.name));
            }
            return;
        }
        if (modInfo.dependencies.count(l->l.enabled())!=modInfo.dependencies.size) {
            return;
        }

        new GasContentLoader((load) -> {
            try {
                load.load();
            } catch (NullPointerException e) {
                if (!headless) showException(e);
            }
        });
        loaded = true;
        Log.info("Gaslib loaded");
    }
}
