package gas.tools;

import arc.util.serialization.Json;
import arc.util.serialization.Jval;
import gas.GasVars;
import arc.Core;
import arc.files.Fi;
import arc.graphics.Pixmap;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureAtlas;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Vec2;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.*;
import gas.gen.GasContentRegions;
import gas.gen.GasEntityMapping;
import mindustry.Vars;
import mindustry.ctype.Content;
import mindustry.ctype.MappableContent;
import mindustry.ctype.UnlockableContent;
import mindustry.mod.Mods;
import mindustry.tools.ImagePacker;
import mma.gen.ModContentRegions;

public class GasImagePacker extends mma.tools.ModImagePacker {

    public GasImagePacker() {
        super();
    }

    public static void main(String[] args) {
        new GasImagePacker();
    }

    @Override
    protected void preCreatingContent() {
        super.preCreatingContent();
        GasEntityMapping.init();
    }

    @Override
    protected void checkContent(Content content) {
        super.checkContent(content);
        if (content instanceof MappableContent) {
            GasContentRegions.loadRegions((MappableContent) content);
        }
    }

    @Override
    protected void runGenerators() {
        new GasGenerators();
    }
}
