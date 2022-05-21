package gas.world.blocks.logic;

import gas.entities.comp.*;
import arc.graphics.gl.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import gas.content.*;
import mindustry.world.blocks.defense.turrets.*;
import gas.world.blocks.payloads.*;
import gas.world.meta.*;
import mindustry.annotations.Annotations.*;
import gas.world.blocks.units.*;
import mindustry.world.blocks.heat.*;
import mindustry.world.blocks.logic.LogicDisplay.*;
import arc.util.*;
import mindustry.world.blocks.legacy.*;
import mindustry.gen.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.meta.*;
import gas.world.blocks.heat.*;
import gas.world.blocks.defense.*;
import gas.world.draw.*;
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
import gas.world.*;
import mindustry.world.consumers.*;
import gas.world.blocks.gas.*;
import gas.world.blocks.campaign.*;
import mindustry.world.modules.*;
import gas.ui.*;
import mindustry.world.blocks.environment.*;
import mindustry.*;
import gas.world.consumers.*;
import arc.graphics.g2d.*;
import mindustry.world.blocks.*;
import gas.world.blocks.production.GasGenericCrafter.*;
import arc.*;
import mindustry.world.blocks.logic.*;
import gas.world.modules.*;
import gas.world.blocks.*;
import arc.graphics.*;
import gas.*;
import gas.io.*;
import mindustry.ui.*;
import gas.gen.*;
import gas.world.blocks.storage.*;
import mindustry.world.blocks.units.*;
import mindustry.graphics.*;
import gas.world.blocks.production.*;
import arc.struct.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;

public class GasLogicDisplay extends GasBlock {

    public static final byte commandClear = 0, commandColor = 1, // virtual command, unpacked in instruction
    commandColorPack = 2, commandStroke = 3, commandLine = 4, commandRect = 5, commandLineRect = 6, commandPoly = 7, commandLinePoly = 8, commandTriangle = 9, commandImage = 10;

    public int maxSides = 25;

    public int displaySize = 64;

    public float scaleFactor = 1f;

    public GasLogicDisplay(String name) {
        super(name);
        update = true;
        solid = true;
        canOverdrive = false;
        group = BlockGroup.logic;
        drawDisabled = false;
        envEnabled = Env.any;
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.displaySize, "@x@", displaySize, displaySize);
    }

    public class GasLogicDisplayBuild extends GasBuilding {

        public FrameBuffer buffer;

        public float color = Color.whiteFloatBits;

        public float stroke = 1f;

        public LongQueue commands = new LongQueue(256);

        @Override
        public void draw() {
            super.draw();
            // don't even bother processing anything when displays are off.
            if (!Vars.renderer.drawDisplays)
                return;
            Draw.draw(Draw.z(), () -> {
                if (buffer == null) {
                    buffer = new FrameBuffer(displaySize, displaySize);
                    // clear the buffer - some OSs leave garbage in it
                    buffer.begin(Pal.darkerMetal);
                    buffer.end();
                }
            });
            // don't bother processing commands if displays are off
            if (!commands.isEmpty()) {
                Draw.draw(Draw.z(), () -> {
                    Tmp.m1.set(Draw.proj());
                    Draw.proj(0, 0, displaySize, displaySize);
                    buffer.begin();
                    Draw.color(color);
                    Lines.stroke(stroke);
                    while (!commands.isEmpty()) {
                        long c = commands.removeFirst();
                        byte type = DisplayCmd.type(c);
                        int x = unpackSign(DisplayCmd.x(c)), y = unpackSign(DisplayCmd.y(c)), p1 = unpackSign(DisplayCmd.p1(c)), p2 = unpackSign(DisplayCmd.p2(c)), p3 = unpackSign(DisplayCmd.p3(c)), p4 = unpackSign(DisplayCmd.p4(c));
                        switch(type) {
                            case commandClear:
                                Core.graphics.clear(x / 255f, y / 255f, p1 / 255f, 1f);
                            case commandLine:
                                Lines.line(x, y, p1, p2);
                            case commandRect:
                                Fill.crect(x, y, p1, p2);
                            case commandLineRect:
                                Lines.rect(x, y, p1, p2);
                            case commandPoly:
                                Fill.poly(x, y, Math.min(p1, maxSides), p2, p3);
                            case commandLinePoly:
                                Lines.poly(x, y, Math.min(p1, maxSides), p2, p3);
                            case commandTriangle:
                                Fill.tri(x, y, p1, p2, p3, p4);
                            case commandColor:
                                Draw.color(this.color = Color.toFloatBits(x, y, p1, p2));
                            case commandStroke:
                                Lines.stroke(this.stroke = x);
                            case commandImage:
                                Draw.rect(Fonts.logicIcon(p1), x, y, p2, p2, p3);
                        }
                    }
                    buffer.end();
                    Draw.proj(Tmp.m1);
                    Draw.reset();
                });
            }
            Draw.blend(Blending.disabled);
            Draw.draw(Draw.z(), () -> {
                if (buffer != null) {
                    Draw.rect(Draw.wrap(buffer.getTexture()), x, y, buffer.getWidth() * scaleFactor * Draw.scl, -buffer.getHeight() * scaleFactor * Draw.scl);
                }
            });
            Draw.blend();
        }

        @Override
        public void remove() {
            super.remove();
            if (buffer != null) {
                buffer.dispose();
                buffer = null;
            }
        }
    }

    static int unpackSign(int value) {
        return (value & 0b0111111111) * ((value & (0b1000000000)) != 0 ? -1 : 1);
    }
}
