package gas;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import arc.struct.Seq;
import gas.gen.*;
import mindustry.Vars;
import mindustry.ctype.Content;
import mindustry.ctype.UnlockableContent;
import mma.MMAMod;
import mma.annotations.ModAnnotations;
import mma.core.ModContentLoader;

import static gas.GasVars.*;
import static mindustry.Vars.*;
@ModAnnotations.DependenciesAnnotation
public class GasLibMod extends MMAMod {


    public GasLibMod() {
        if (!GasDependencies.valid())return;
        GasEntityMapping.init();
        GasVars.modInfo = Vars.mods.getMod(getClass());
    }

    public static TextureRegion getIcon() {
        if (GasVars.modInfo == null || GasVars.modInfo.iconTexture == null) return Core.atlas.find("nomap");
        return new TextureRegion(GasVars.modInfo.iconTexture);
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
        Seq<Content> all = Seq.with(content.getContentMap()).<Content>flatten().select(c -> c.minfo.mod == GasVars.modInfo).as();
        for (Content c : all) {
            if (inPackage("gas", c)) {
                if (c instanceof UnlockableContent) checkTranslate((UnlockableContent) c);
            }
        }
    }

    public void loadContent() {
        GasVars.modInfo = Vars.mods.getMod(this.getClass());
        if (!GasDependencies.valid()) {
            if (GasVars.modInfo != null){
                GasVars.modInfo.missingDependencies.addAll(GasVars.modInfo.dependencies.select(mod->!mod.enabled()).map(l->l.name));
            }
            return;
        }
        if (GasVars.modInfo.dependencies.count(l->l.enabled()) != GasVars.modInfo.dependencies.size) {
            return;
        }

        new ModContentLoader((load) -> {
            load.load();
        });
        loaded = true;
    }
}
