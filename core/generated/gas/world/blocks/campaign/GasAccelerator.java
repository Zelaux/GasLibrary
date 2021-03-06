package gas.world.blocks.campaign;

import gas.entities.comp.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import mindustry.content.*;
import gas.content.*;
import mindustry.world.blocks.defense.turrets.*;
import gas.world.meta.*;
import mindustry.annotations.Annotations.*;
import gas.world.blocks.units.*;
import arc.Graphics.*;
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
import gas.world.draw.*;
import mindustry.world.blocks.logic.*;
import mindustry.world.blocks.distribution.*;
import gas.world.blocks.power.*;
import mindustry.world.*;
import gas.world.blocks.sandbox.*;
import mindustry.game.EventType.*;
import gas.world.blocks.liquid.*;
import mindustry.world.blocks.storage.*;
import gas.entities.*;
import mindustry.world.blocks.payloads.*;
import mindustry.world.blocks.campaign.*;
import gas.world.blocks.defense.turrets.*;
import gas.world.blocks.distribution.*;
import gas.world.*;
import mindustry.type.*;
import mindustry.world.blocks.campaign.Accelerator.*;
import gas.world.blocks.gas.*;
import arc.scene.ui.layout.*;
import gas.world.blocks.campaign.*;
import mindustry.world.modules.*;
import gas.ui.*;
import mindustry.world.blocks.environment.*;
import gas.world.consumers.*;
import arc.graphics.g2d.*;
import mindustry.world.blocks.*;
import gas.world.blocks.production.GasGenericCrafter.*;
import arc.*;
import mindustry.world.consumers.*;
import gas.world.modules.*;
import gas.world.blocks.*;
import gas.*;
import gas.io.*;
import gas.world.blocks.payloads.*;
import gas.gen.*;
import gas.world.blocks.storage.*;
import mindustry.world.blocks.units.*;
import mindustry.world.blocks.heat.*;
import mindustry.graphics.*;
import gas.world.blocks.production.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import mindustry.world.blocks.power.*;
import arc.Graphics.Cursor.*;
import mindustry.world.blocks.sandbox.*;
import static mindustry.Vars.*;

public class GasAccelerator extends GasBlock {

    @Load("launch-arrow")
    public TextureRegion arrowRegion;

    // TODO dynamic
    public Block launching = Blocks.coreNucleus;

    public int[] capacities = {};

    public GasAccelerator(String name) {
        super(name);
        update = true;
        solid = true;
        hasItems = true;
        itemCapacity = 8000;
        configurable = true;
    }

    @Override
    public void init() {
        itemCapacity = 0;
        capacities = new int[content.items().size];
        for (var stack : launching.requirements) {
            capacities[stack.item.id] = stack.amount;
            itemCapacity += stack.amount;
        }
        consumeItems(launching.requirements);
        super.init();
    }

    @Override
    public boolean outputsItems() {
        return false;
    }

    public class GasAcceleratorBuild extends GasBuilding {

        public float heat, statusLerp;

        @Override
        public void updateTile() {
            super.updateTile();
            heat = Mathf.lerpDelta(heat, efficiency, 0.05f);
            statusLerp = Mathf.lerpDelta(statusLerp, power.status, 0.05f);
        }

        @Override
        public void draw() {
            super.draw();
            for (int l = 0; l < 4; l++) {
                float length = 7f + l * 5f;
                Draw.color(Tmp.c1.set(Pal.darkMetal).lerp(team.color, statusLerp), Pal.darkMetal, Mathf.absin(Time.time + l * 50f, 10f, 1f));
                for (int i = 0; i < 4; i++) {
                    float rot = i * 90f + 45f;
                    Draw.rect(arrowRegion, x + Angles.trnsx(rot, length), y + Angles.trnsy(rot, length), rot + 180f);
                }
            }
            if (heat < 0.0001f)
                return;
            float rad = size * tilesize / 2f * 0.74f;
            float scl = 2f;
            Draw.z(Layer.bullet - 0.0001f);
            Lines.stroke(1.75f * heat, Pal.accent);
            Lines.square(x, y, rad * 1.22f, 45f);
            Lines.stroke(3f * heat, Pal.accent);
            Lines.square(x, y, rad, Time.time / scl);
            Lines.square(x, y, rad, -Time.time / scl);
            Draw.color(team.color);
            Draw.alpha(Mathf.clamp(heat * 3f));
            for (int i = 0; i < 4; i++) {
                float rot = i * 90f + 45f + (-Time.time / 3f) % 360f;
                float length = 26f * heat;
                Draw.rect(arrowRegion, x + Angles.trnsx(rot, length), y + Angles.trnsy(rot, length), rot + 180f);
            }
            Draw.reset();
        }

        @Override
        public Cursor getCursor() {
            return !state.isCampaign() || efficiency <= 0f ? SystemCursor.arrow : super.getCursor();
        }

        @Override
        public void buildConfiguration(Table table) {
            deselect();
            if (!state.isCampaign() || efficiency <= 0f)
                return;
            ui.planet.showPlanetLaunch(state.rules.sector, sector -> {
                // TODO cutscene, etc...
                // TODO should consume resources based on destination schem
                consume();
                universe.clearLoadoutInfo();
                universe.updateLoadout(sector.planet.generator.getDefaultLoadout().findCore(), sector.planet.generator.getDefaultLoadout());
            });
            Events.fire(Trigger.acceleratorUse);
        }

        @Override
        public int getMaximumAccepted(Item item) {
            return capacities[item.id];
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            return items.get(item) < getMaximumAccepted(item);
        }
    }
}
