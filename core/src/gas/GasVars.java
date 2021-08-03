package gas;

import arc.Core;
import arc.util.Strings;
import gas.GasLibMod;
import mindustry.ctype.UnlockableContent;
import mindustry.mod.Mods;

public class GasVars {
    public static Mods.LoadedMod gasLibInfo;
    public static boolean loaded = false;
    public static boolean packSprites;
    public static void checkTranslate(UnlockableContent content) {
        content.localizedName = Core.bundle.get(content.getContentType() + "." + content.name + ".name", content.localizedName);
        content.description = Core.bundle.get(content.getContentType() + "." + content.name + ".description", content.description);
        content.details = Core.bundle.get(content.getContentType() + "." + content.name + ".details", content.details);
    }

    public static String fullName(String name) {
        if (packSprites) return name;
        return Strings.format("@-@", gasLibInfo == null ? "gas" : gasLibInfo.name, name);
    }
}
