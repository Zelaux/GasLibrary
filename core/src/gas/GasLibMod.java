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
public class GasLibMod extends MMAMod {


    public GasLibMod() {
        super();
        GasEntityMapping.init();
        GasVars.create();
    }

    public void init() {
        if (!loaded) return;
        super.init();
    }

    public void loadContent() {
        super.loadContent();
    }
}
