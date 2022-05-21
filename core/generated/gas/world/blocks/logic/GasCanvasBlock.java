package gas.world.blocks.logic;

import gas.entities.comp.*;
import arc.scene.ui.layout.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import arc.input.*;
import gas.content.*;
import mindustry.world.blocks.defense.turrets.*;
import gas.world.blocks.payloads.*;
import gas.world.meta.*;
import mindustry.ui.*;
import gas.world.blocks.units.*;
import mindustry.world.blocks.heat.*;
import arc.scene.event.*;
import arc.util.*;
import mindustry.world.blocks.legacy.*;
import mindustry.gen.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import arc.math.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.meta.*;
import gas.world.blocks.heat.*;
import gas.world.blocks.defense.*;
import mindustry.world.blocks.logic.CanvasBlock.*;
import mindustry.world.blocks.distribution.*;
import gas.world.blocks.power.*;
import mindustry.world.*;
import gas.world.blocks.sandbox.*;
import mindustry.world.blocks.storage.*;
import gas.world.blocks.liquid.*;
import gas.entities.*;
import mindustry.world.blocks.campaign.*;
import gas.world.blocks.defense.turrets.*;
import gas.world.blocks.distribution.*;
import gas.world.*;
import mindustry.world.consumers.*;
import gas.world.blocks.gas.*;
import arc.math.geom.*;
import gas.world.blocks.campaign.*;
import mindustry.world.modules.*;
import gas.ui.*;
import mindustry.world.blocks.environment.*;
import gas.world.consumers.*;
import mindustry.world.blocks.payloads.*;
import mindustry.world.blocks.*;
import gas.world.blocks.production.GasGenericCrafter.*;
import arc.graphics.g2d.*;
import mindustry.world.blocks.logic.*;
import gas.world.modules.*;
import gas.world.blocks.*;
import arc.graphics.*;
import gas.*;
import gas.io.*;
import gas.world.draw.*;
import mindustry.world.blocks.defense.*;
import gas.gen.*;
import gas.world.blocks.storage.*;
import mindustry.world.blocks.units.*;
import gas.world.blocks.production.*;
import arc.scene.*;
import arc.struct.*;
import arc.util.io.*;
import arc.scene.ui.*;
import gas.entities.bullets.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;
import static mindustry.Vars.*;

public class GasCanvasBlock extends GasBlock {

    public float padding = 0f;

    public int canvasSize = 8;

    public int[] palette = { 0x634b7dff, 0xc45d9f_ff, 0xe39aac_ff, 0xf0dab1_ff, 0x6461c2_ff, 0x2ba9b4_ff, 0x93d4b5_ff, 0xf0f6e8_ff };

    public int bitsPerPixel;

    public IntIntMap colorToIndex = new IntIntMap();

    public GasCanvasBlock(String name) {
        super(name);
        configurable = true;
        destructible = true;
        canOverdrive = false;
        solid = true;
        config(byte[].class, (GasCanvasBuild build, byte[] bytes) -> {
            if (build.data.length == bytes.length) {
                build.data = bytes;
                build.updateTexture();
            }
        });
    }

    @Override
    public void init() {
        super.init();
        for (int i = 0; i < palette.length; i++) {
            colorToIndex.put(palette[i], i);
        }
        bitsPerPixel = Mathf.log2(Mathf.nextPowerOfTwo(palette.length));
    }

    public class GasCanvasBuild extends GasBuilding {

        @Nullable
        public Texture texture;

        public byte[] data = new byte[Mathf.ceil(canvasSize * canvasSize * bitsPerPixel / 8f)];

        public void updateTexture() {
            Pixmap pix = makePixmap();
            if (texture != null) {
                texture.draw(pix);
            } else {
                texture = new Texture(pix);
            }
            pix.dispose();
        }

        public Pixmap makePixmap() {
            Pixmap pix = new Pixmap(canvasSize, canvasSize);
            int bpp = bitsPerPixel;
            int pixels = canvasSize * canvasSize;
            for (int i = 0; i < pixels; i++) {
                int bitOffset = i * bpp;
                int pal = getByte(bitOffset);
                pix.set(i % canvasSize, i / canvasSize, palette[pal]);
            }
            return pix;
        }

        public byte[] packPixmap(Pixmap pixmap) {
            byte[] bytes = new byte[data.length];
            int pixels = canvasSize * canvasSize;
            for (int i = 0; i < pixels; i++) {
                int color = pixmap.get(i % canvasSize, i / canvasSize);
                int palIndex = colorToIndex.get(color);
                setByte(bytes, i * bitsPerPixel, palIndex);
            }
            return bytes;
        }

        protected int getByte(int bitOffset) {
            int result = 0, bpp = bitsPerPixel;
            for (int i = 0; i < bpp; i++) {
                int word = i + bitOffset >>> 3;
                result |= (((data[word] & (1 << (i + bitOffset & 7))) == 0 ? 0 : 1) << i);
            }
            return result;
        }

        protected void setByte(byte[] bytes, int bitOffset, int value) {
            int bpp = bitsPerPixel;
            for (int i = 0; i < bpp; i++) {
                int word = i + bitOffset >>> 3;
                if (((value >>> i) & 1) == 0) {
                    bytes[word] &= ~(1 << (i + bitOffset & 7));
                } else {
                    bytes[word] |= (1 << (i + bitOffset & 7));
                }
            }
        }

        @Override
        public void draw() {
            super.draw();
            if (texture == null) {
                updateTexture();
            }
            Tmp.tr1.set(texture);
            Draw.rect(Tmp.tr1, x, y, size * tilesize - padding, size * tilesize - padding);
        }

        @Override
        public void remove() {
            super.remove();
            if (texture != null) {
                texture.dispose();
                texture = null;
            }
        }

        @Override
        public void buildConfiguration(Table table) {
            table.button(Icon.pencil, Styles.cleari, () -> {
                Dialog dialog = new Dialog();
                Pixmap pix = makePixmap();
                Texture texture = new Texture(pix);
                int[] curColor = { palette[0] };
                boolean[] modified = { false };
                dialog.cont.table(Tex.pane, body -> {
                    body.stack(new Element() {

                        int lastX, lastY;

                        {
                            addListener(new InputListener() {

                                int convertX(float ex) {
                                    return (int) ((ex - x) / width * canvasSize);
                                }

                                int convertY(float ey) {
                                    return pix.height - 1 - (int) ((ey - y) / height * canvasSize);
                                }

                                @Override
                                public boolean touchDown(InputEvent event, float ex, float ey, int pointer, KeyCode button) {
                                    int cx = convertX(ex), cy = convertY(ey);
                                    draw(cx, cy);
                                    lastX = cx;
                                    lastY = cy;
                                    return true;
                                }

                                @Override
                                public void touchDragged(InputEvent event, float ex, float ey, int pointer) {
                                    int cx = convertX(ex), cy = convertY(ey);
                                    Bresenham2.line(lastX, lastY, cx, cy, (x, y) -> draw(x, y));
                                    lastX = cx;
                                    lastY = cy;
                                }
                            });
                        }

                        void draw(int x, int y) {
                            if (pix.get(x, y) != curColor[0]) {
                                pix.set(x, y, curColor[0]);
                                Pixmaps.drawPixel(texture, x, y, curColor[0]);
                                modified[0] = true;
                            }
                        }

                        @Override
                        public void draw() {
                            Tmp.tr1.set(texture);
                            Draw.alpha(parentAlpha);
                            Draw.rect(Tmp.tr1, x + width / 2f, y + height / 2f, width, height);
                        }
                    }, new GridImage(canvasSize, canvasSize) {

                        {
                            touchable = Touchable.disabled;
                        }
                    }).size(500f);
                });
                dialog.cont.row();
                dialog.cont.table(Tex.button, p -> {
                    for (int i = 0; i < palette.length; i++) {
                        int fi = i;
                        var button = p.button(Tex.whiteui, Styles.squareTogglei, 30, () -> {
                            curColor[0] = palette[fi];
                        }).size(44).checked(b -> curColor[0] == palette[fi]).get();
                        button.getStyle().imageUpColor = new Color(palette[i]);
                    }
                });
                dialog.closeOnBack();
                dialog.buttons.defaults().size(150f, 64f);
                dialog.buttons.button("@cancel", Icon.cancel, dialog::hide);
                dialog.buttons.button("@ok", Icon.ok, () -> {
                    if (modified[0]) {
                        configure(packPixmap(pix));
                        pix.dispose();
                        texture.dispose();
                    }
                    dialog.hide();
                });
                dialog.show();
            }).size(40f);
        }

        @Override
        public byte[] config() {
            return data;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            // for future canvas resizing events
            write.i(data.length);
            write.b(data);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            int len = read.i();
            if (data.length == len) {
                read.b(data);
            } else {
                read.skip(len);
            }
        }
    }
}
