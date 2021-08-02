package gas.tools;

import ModVars.GasVars;
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
import gas.core.ModContentLoader;
import gas.gen.GasContentRegions;
import gas.gen.GasEntityMapping;
import mindustry.Vars;
import mindustry.ctype.MappableContent;
import mindustry.ctype.UnlockableContent;
import mindustry.tools.ImagePacker;

public class ModImagePacker extends ImagePacker {
    static ObjectMap<String, PackIndex> cache = new ObjectMap<>();

    public ModImagePacker() {
    }

    public static void main(String[] args) throws Exception {
        Vars.headless = true;
        GasVars.packSprites = true;
        ArcNativesLoader.load();
        Log.logger = new Log.NoopLogHandler();
        Vars.content = new ModContentLoader();
        GasEntityMapping.init();
        Vars.content.createBaseContent();
        Vars.content.createModContent();
        Log.logger = new Log.DefaultLogHandler();
        Fi.get("../../../assets-raw/sprites_out").walk((path) -> {
            if (path.extEquals("png")) {
                cache.put(path.nameWithoutExtension(), new PackIndex(path));
            }
        });
        Core.atlas = new TextureAtlas() {
            @Override
            public AtlasRegion find(String name) {
                if (!cache.containsKey(name)) {
                    GenRegion region = new GenRegion(name, null);
                    region.invalid = true;
                    return region;
                }

                PackIndex index = cache.get(name);
                if (index.pixmap == null) {
                    index.pixmap = new Pixmap(index.file);
                    index.region = new GenRegion(name, index.file) {{
                        width = index.pixmap.width;
                        height = index.pixmap.height;
                        u2 = v2 = 1f;
                        u = v = 0f;
                    }};
                }
                return index.region;
            }

            @Override
            public AtlasRegion find(String name, TextureRegion def) {
                if (!cache.containsKey(name)) {
                    return (AtlasRegion) def;
                }
                return find(name);
            }

            @Override
            public AtlasRegion find(String name, String def) {
                if (!cache.containsKey(name)) {
                    return find(def);
                }
                return find(name);
            }

            @Override
            public boolean has(String s) {
                return cache.containsKey(s);
            }
        };
        Core.atlas.setErrorRegion("error");

        Draw.scl = 1f / Core.atlas.find("scale_marker").width;

        Vars.content.each(c -> {
            if (c instanceof MappableContent) GasContentRegions.loadRegions((MappableContent) c);
        });
        Time.mark();
        Generators.run();
        Log.info("&ly[Generator]&lc Total time to generate: &lg@&lcms", Time.elapsed());
        Log.info("&ly[Disposing]&lc Start");
        Time.mark();
        Log.info("&ly[Disposing]&lc Total time: @", Time.elapsed());
        GasVars.packSprites = false;
    }


    static String texname(UnlockableContent c) {
        return c.getContentType() + "-" + c.name + "-ui";
    }

    static void generate(String name, Runnable run) {
        Time.mark();
        Log.info("&ly[Generator]&lc Start &lm@&lc", name);
        run.run();
        Log.info("&ly[Generator]&lc Time to generate &lm@&lc: &lg@&lcms", name, Time.elapsed());
    }

    static Pixmap get(String name) {
        return get(Core.atlas.find(name));
    }

    static boolean has(String name) {
        return Core.atlas.has(name);
    }

    static Pixmap get(TextureRegion region) {
        validate(region);

        return cache.get(((TextureAtlas.AtlasRegion) region).name).pixmap.copy();
    }

    static void save(Pixmap pix, String path) {
        Fi.get(path + ".png").writePng(pix);
    }

    static void drawCenter(Pixmap pix, Pixmap other) {
        pix.draw(other, pix.width / 2 - other.width / 2, pix.height / 2 - other.height / 2, true);
    }

    static void saveScaled(Pixmap pix, String name, int size) {
        Pixmap scaled = new Pixmap(size, size);
        //TODO bad linear scaling
        scaled.draw(pix, 0, 0, pix.width, pix.height, 0, 0, size, size, true, true);
        save(scaled, name);
    }

    static void drawScaledFit(Pixmap base, Pixmap image) {
        Vec2 size = Scaling.fit.apply(image.width, image.height, base.width, base.height);
        int wx = (int) size.x, wy = (int) size.y;
        //TODO bad linear scaling
        base.draw(image, 0, 0, image.width, image.height, base.width / 2 - wx / 2, base.height / 2 - wy / 2, wx, wy, true, true);
    }

    static void replace(String name, Pixmap image) {
        Fi.get(name + ".png").writePng(image);
        ((GenRegion) Core.atlas.find(name)).path.delete();
    }

    static void replace(TextureRegion region, Pixmap image) {
        replace(((GenRegion) region).name, image);
    }

    static void validate(TextureRegion region) {
        if (((GenRegion) region).invalid) {
            err("Region does not exist: @", ((GenRegion) region).name);
        }
    }

    static void err(String message, Object... args) {
        Log.err(message, args);
//        throw new IllegalArgumentException(Strings.format(message, args));
    }

    static class GenRegion extends TextureAtlas.AtlasRegion {
        boolean invalid;
        String regionName = "unknown";
        Fi path;
        Seq<String> notExistNames = new Seq<>();

        GenRegion(String name, Fi path) {
            if (name == null) {
                throw new IllegalArgumentException("name is null");
            } else {
                this.name = name;
                this.path = path;
            }
        }

        static void validate(TextureRegion region) {
            GenRegion genRegion = (GenRegion) region;
            if (genRegion.invalid) {
                Seq<String> names = genRegion.notExistNames;
                if (names.size == 1) {
                    err("Region does not exist: @", names.first());
                } else if (names.size == 0) {
//                    err("Region does not exist0: @", genRegion.name);
                } else {
                    err("Regions does not exist: @", names.toString(", "));
                }
                names.clear();
            }

        }

        public boolean found() {
            return !this.invalid;
        }

        public void addName(String name) {
            if (!notExistNames.contains(name)) notExistNames.add(name);
        }
    }

    static class PackIndex {
        @Nullable
        TextureAtlas.AtlasRegion region;
        @Nullable
        Pixmap pixmap;
        Fi file;

        public PackIndex(Fi file) {
            this.file = file;
        }
    }
}
