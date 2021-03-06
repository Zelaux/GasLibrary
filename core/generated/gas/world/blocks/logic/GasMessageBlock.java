package gas.world.blocks.logic;

import gas.entities.comp.*;
import arc.scene.ui.layout.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import gas.content.*;
import mindustry.world.blocks.defense.turrets.*;
import gas.world.blocks.payloads.*;
import gas.world.meta.*;
import mindustry.ui.*;
import gas.world.blocks.units.*;
import mindustry.world.blocks.heat.*;
import arc.util.*;
import mindustry.world.blocks.legacy.*;
import mindustry.gen.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.meta.*;
import gas.world.blocks.heat.*;
import gas.world.blocks.defense.*;
import arc.Input.*;
import mindustry.world.blocks.payloads.*;
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
import arc.util.pooling.*;
import gas.world.*;
import mindustry.world.consumers.*;
import gas.world.blocks.gas.*;
import arc.math.geom.*;
import gas.world.blocks.campaign.*;
import mindustry.world.modules.*;
import gas.ui.*;
import mindustry.world.blocks.environment.*;
import gas.world.consumers.*;
import arc.graphics.g2d.*;
import mindustry.world.blocks.*;
import gas.world.blocks.production.GasGenericCrafter.*;
import arc.*;
import mindustry.ui.dialogs.*;
import mindustry.world.blocks.logic.*;
import gas.world.modules.*;
import gas.world.blocks.*;
import arc.graphics.*;
import gas.*;
import gas.io.*;
import gas.world.draw.*;
import mindustry.world.blocks.defense.*;
import mindustry.world.blocks.logic.MessageBlock.*;
import gas.gen.*;
import gas.world.blocks.storage.*;
import mindustry.world.blocks.units.*;
import gas.world.blocks.production.*;
import arc.util.io.*;
import arc.scene.ui.*;
import gas.entities.bullets.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;
import static mindustry.Vars.*;

public class GasMessageBlock extends GasBlock {

    // don't change this too much unless you want to run into issues with packet sizes
    public int maxTextLength = 220;

    public int maxNewlines = 24;

    public GasMessageBlock(String name) {
        super(name);
        configurable = true;
        solid = true;
        destructible = true;
        group = BlockGroup.logic;
        drawDisabled = false;
        envEnabled = Env.any;
        config(String.class, (GasMessageBuild tile, String text) -> {
            if (text.length() > maxTextLength) {
                // no.
                return;
            }
            tile.message.ensureCapacity(text.length());
            tile.message.setLength(0);
            text = text.trim();
            int count = 0;
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '\n') {
                    if (count++ <= maxNewlines) {
                        tile.message.append('\n');
                    }
                } else {
                    tile.message.append(c);
                }
            }
        });
    }

    public class GasMessageBuild extends GasBuilding {

        public StringBuilder message = new StringBuilder();

        @Override
        public void drawSelect() {
            if (renderer.pixelator.enabled())
                return;
            Font font = Fonts.outline;
            GlyphLayout l = Pools.obtain(GlyphLayout.class, GlyphLayout::new);
            boolean ints = font.usesIntegerPositions();
            font.getData().setScale(1 / 4f / Scl.scl(1f));
            font.setUseIntegerPositions(false);
            CharSequence text = message == null || message.length() == 0 ? "[lightgray]" + Core.bundle.get("empty") : message;
            l.setText(font, text, Color.white, 90f, Align.left, true);
            float offset = 1f;
            Draw.color(0f, 0f, 0f, 0.2f);
            Fill.rect(x, y - tilesize / 2f - l.height / 2f - offset, l.width + offset * 2f, l.height + offset * 2f);
            Draw.color();
            font.setColor(Color.white);
            font.draw(text, x - l.width / 2f, y - tilesize / 2f - offset, 90f, Align.left, true);
            font.setUseIntegerPositions(ints);
            font.getData().setScale(1f);
            Pools.free(l);
        }

        @Override
        public void buildConfiguration(Table table) {
            table.button(Icon.pencil, Styles.cleari, () -> {
                if (mobile) {
                    Core.input.getTextInput(new TextInput() {

                        {
                            text = message.toString();
                            multiline = true;
                            maxLength = maxTextLength;
                            accepted = str -> {
                                if (!str.equals(text))
                                    configure(str);
                            };
                        }
                    });
                } else {
                    BaseDialog dialog = new BaseDialog("@editmessage");
                    dialog.setFillParent(false);
                    TextArea a = dialog.cont.add(new TextArea(message.toString().replace("\r", "\n"))).size(380f, 160f).get();
                    a.setFilter((textField, c) -> {
                        if (c == '\n') {
                            int count = 0;
                            for (int i = 0; i < textField.getText().length(); i++) {
                                if (textField.getText().charAt(i) == '\n') {
                                    count++;
                                }
                            }
                            return count < maxNewlines;
                        }
                        return true;
                    });
                    a.setMaxLength(maxTextLength);
                    dialog.cont.row();
                    dialog.cont.label(() -> a.getText().length() + " / " + maxTextLength).color(Color.lightGray);
                    dialog.buttons.button("@ok", () -> {
                        if (!a.getText().equals(message.toString()))
                            configure(a.getText());
                        dialog.hide();
                    }).size(130f, 60f);
                    dialog.update(() -> {
                        if (tile.build != this) {
                            dialog.hide();
                        }
                    });
                    dialog.closeOnBack();
                    dialog.show();
                }
                deselect();
            }).size(40f);
        }

        @Override
        public boolean onConfigureBuildTapped(Building other) {
            if (this == other) {
                deselect();
                return false;
            }
            return true;
        }

        @Override
        public void handleString(Object value) {
            message.setLength(0);
            message.append(value);
        }

        @Override
        public void updateTableAlign(Table table) {
            Vec2 pos = Core.input.mouseScreen(x, y + size * tilesize / 2f + 1);
            table.setPosition(pos.x, pos.y, Align.bottom);
        }

        @Override
        public String config() {
            return message.toString();
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.str(message.toString());
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            message = new StringBuilder(read.str());
        }
    }
}
