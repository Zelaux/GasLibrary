package gas.world.blocks.gas;

import arc.graphics.g2d.*;
import gas.type.*;
import mindustry.gen.*;

public class GasRouter extends GasGasBlock {
    public float gasPadding = 0f;

    public GasRouter(String name) {
        super(name);
        underBullets = true;
        solid = false;
        noUpdateDisabled = true;
        canOverdrive = false;
    }
    @Override
    public TextureRegion[] icons(){
        return new TextureRegion[]{bottomRegion, region};
    }


    public class GasRouterBuild extends GasGasBuild{

        @Override
        public void updateTile() {
            if (gasses.currentAmount() > 0.01f) {
                dumpGas(gasses.current());
            }
        }

        @Override
        public void draw(){
            Draw.rect(bottomRegion, x, y);

            if(gasses.currentAmount() > 0.001f){
                drawTiledFrames(size, x, y, gasPadding, gasses.current(), gasses.currentAmount() / gasCapacity);
            }

            Draw.rect(region, x, y);
        }


        @Override
        public boolean acceptGas(Building source, Gas gas) {
            return (gasses.current() == gas || gasses.currentAmount() < 0.2f);
        }
    }
}
