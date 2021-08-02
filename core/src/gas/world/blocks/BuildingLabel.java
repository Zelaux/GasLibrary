package gas.world.blocks;

import arc.Core;
import arc.func.Func;
import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Font;
import arc.math.geom.Vec2;
import arc.scene.Action;
import arc.scene.Element;
import arc.scene.actions.Actions;
import arc.scene.event.Touchable;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.ui.Fonts;
import mindustry.ui.Styles;

import java.util.concurrent.atomic.AtomicBoolean;

public interface BuildingLabel extends BuildingTaskQueue {
    AtomicBoolean loadLabels=new AtomicBoolean(true);
    default void loadLabels(Runnable runnable){
        if (loadLabels.get()) {
            runnable.run();
            loadLabels.set(false);
        }

    }
    default void newLabel(Prov<Building> cons, Func<Building, String> name){
        newLabel(cons,name,(building)->false);
    }
    default void drawLabel(Building build,float textSize,Color color,String text){
        Font font = Fonts.outline;
        boolean ints = font.usesIntegerPositions();
        font.getData().setScale(textSize / Scl.scl(1.0f));
        font.setUseIntegerPositions(false);

        font.setColor(color);

        float z = Draw.z();
        Draw.z(Layer.overlayUI+1.f);
        font.draw(text, build.x - build.block.size * 4, build.y + 1);
        Draw.z(z);

        font.setUseIntegerPositions(ints);
        font.getData().setScale(1);
    }
    default void drawLabel(Building build,String text){
        drawLabel(build,0.23f,text);
    }
    default void drawLabel(Building build,Color color,String text){
        drawLabel(build,0.23f,color,text);
    }
    default void drawLabel(Building build,float textSize,String text){
        drawLabel(build,textSize,Color.white,text);
    }
    default void newLabel(Prov<Building> cons, Func<Building, String> name,Func<Building,Boolean> boolf){
        addTast(()->{
            Table table = (new Table(Styles.black3)).margin(4.0F);
            table.touchable = Touchable.disabled;
            Label label=new Label("");

            table.visibility=()->cons.get()!=null && boolf.get(cons.get());
            table.update(() -> {
                if (Vars.state.isMenu()) {
                    table.remove();
                }
//                label.setText(""+this.rotation);
                Building build=cons.get();
                if (build==null){
                    return;
                }
                label.setText(name.get(build));
                Vec2 v = Core.camera.project(build.tile().worldx(), build.tile().worldy());
                table.setPosition(v.x, v.y, 1);
            });
            Building me=(Building) this;
            table.actions(new Action() {
                @Override
                public boolean act(float v) {
                    return !me.isValid();
                }
            }, Actions.remove());
            table.add(label).style(Styles.outlineLabel);
            table.pack();
            table.act(0.0F);
            Core.scene.root.addChildAt(0, table);
            ((Element)table.getChildren().first()).act(0.0F);
        });
    }
}
