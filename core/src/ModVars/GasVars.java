package ModVars;

import gas.GasLibMod;
import gas.ModListener;
import mindustry.mod.Mods;

import static mindustry.Vars.*;

public class GasVars {
    public static Mods.LoadedMod modInfo;
    public static ModListener listener;
    public static GasLibMod mod;
    public static boolean renderUpdate;
    public static boolean loaded = false;
    public static boolean packSprites;


    public static void init() {
    }

    public static void load() {
        ModListener.load();
    }
}
