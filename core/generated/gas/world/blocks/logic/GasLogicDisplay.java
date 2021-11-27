package gas.world.blocks.logic;

import gas.entities.comp.*;
import arc.graphics.gl.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import mindustry.world.blocks.defense.turrets.*;
import gas.world.blocks.payloads.*;
import mindustry.world.blocks.experimental.*;
import gas.io.*;
import mindustry.world.blocks.logic.LogicDisplay.*;
import gas.world.meta.*;
import mindustry.annotations.Annotations.*;
import gas.world.blocks.units.*;
import gas.world.blocks.defense.*;
import arc.util.*;
import mindustry.world.blocks.legacy.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.meta.*;
import arc.graphics.*;
import gas.world.blocks.distribution.*;
import gas.world.draw.*;
import mindustry.world.blocks.logic.*;
import mindustry.gen.*;
import gas.world.blocks.power.*;
import mindustry.world.*;
import gas.world.blocks.sandbox.*;
import mindustry.world.blocks.storage.*;
import gas.world.blocks.liquid.*;
import gas.entities.*;
import mindustry.world.blocks.campaign.*;
import gas.gen.*;
import gas.world.*;
import gas.world.blocks.defense.turrets.*;
import gas.world.blocks.gas.*;
import gas.world.blocks.campaign.*;
import mindustry.world.modules.*;
import gas.ui.*;
import mindustry.world.blocks.environment.*;
import gas.world.consumers.*;
import mindustry.world.blocks.payloads.*;
import mindustry.world.blocks.*;
import gas.world.blocks.production.GasGenericCrafter.*;
import arc.*;
import mindustry.world.consumers.*;
import gas.world.modules.*;
import gas.world.blocks.*;
import gas.*;
import arc.graphics.g2d.*;
import mindustry.ui.*;
import mindustry.world.blocks.units.*;
import gas.content.*;
import gas.world.blocks.storage.*;
import mindustry.graphics.*;
import gas.world.blocks.production.*;
import arc.struct.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import gas.world.meta.values.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;

public class GasLogicDisplay extends GasBlock {

    public static final byte commandClear = 0, commandColor = 1, commandStroke = 2, commandLine = 3, commandRect = 4, commandLineRect = 5, commandPoly = 6, commandLinePoly = 7, commandTriangle = 8, commandImage = 9;

    public int maxSides = 25;

    public int displaySize = 64;

    public GasLogicDisplay(String name) {
        super(name);
        update = true;
        solid = true;
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
            Draw.draw(Draw.z(), () -> {
                if (buffer == null) {
                    buffer = new FrameBuffer(displaySize, displaySize);
                    // clear the buffer - some OSs leave garbage in it
                    buffer.begin(Pal.darkerMetal);
                    buffer.end();
                }
            });
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
                    Draw.rect(Draw.wrap(buffer.getTexture()), x, y, buffer.getWidth() * Draw.scl, -buffer.getHeight() * Draw.scl);
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
