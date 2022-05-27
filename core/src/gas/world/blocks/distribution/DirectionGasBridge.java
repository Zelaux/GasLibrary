package gas.world.blocks.distribution;

import arc.graphics.g2d.*;
import arc.util.*;
import gas.type.*;
import gas.world.blocks.gas.*;
import mindustry.annotations.Annotations.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.meta.*;

public class DirectionGasBridge extends GasDirectionBridge{
    public final int timerFlow = timers++;

    public float speed = 5f;
    public float gasPadding = 1f;

    public @Load("@-bottom")
    TextureRegion bottomRegion;

    public DirectionGasBridge(String name){
        super(name);

        outputsGas = true;
        group = BlockGroup.liquids;
        canOverdrive = false;
        gasCapacity = 20f;
        hasGasses = true;
    }


    @Override
    public TextureRegion[] icons(){
        return new TextureRegion[]{bottomRegion, region, dirRegion};
    }

    public class GasDuctBridgeBuild extends GasDirectionBridgeBuild{

        @Override
        public void draw(){
            Draw.rect(bottomRegion, x, y);

            if(gasses.currentAmount() > 0.001f){
                GasGasBlock.drawTiledFrames(size, x, y, gasPadding, gasses.current(), gasses.currentAmount() / gasCapacity);
            }

            Draw.rect(block.region, x, y);

            Draw.rect(dirRegion, x, y, rotdeg());
            var link = findLink();
            if(link != null){
                Draw.z(Layer.power - 1);
                drawBridge(rotation, x, y, link.x, link.y, Tmp.c1.set(gasses.current().color).a(gasses.currentAmount() / gasCapacity * gasses.current().color.a));
            }
        }

        @Override
        public void updateTile(){
            var link = findLink();
            if(link != null){
                moveGas(link, gasses.current());
                link.occupied[rotation % 4] = this;
            }

            if(link == null){
                if(gasses.currentAmount() > 0.0001f && timer(timerFlow, 1)){
                    moveGasForward(false, gasses.current());
                }
            }

            for(int i = 0; i < 4; i++){
                if(occupied[i] == null || occupied[i].rotation != i || !occupied[i].isValid()){
                    occupied[i] = null;
                }
            }
        }

        @Override
        public boolean acceptGas(Building source, Gas gas){
            var link = findLink();
            //only accept if there's an output point, or it comes from a link
            if(link == null && !(source instanceof GasDirectionBridgeBuild b && b.findLink() == this)) return false;

            int rel = this.relativeToEdge(source.tile);

            return
            hasGasses && team == source.team &&
            (gasses.current() == gas || gasses.get(gasses.current()) < 0.2f) && rel != rotation &&
            (occupied[(rel + 2) % 4] == null || occupied[(rel + 2) % 4] == source);
        }
    }
}
