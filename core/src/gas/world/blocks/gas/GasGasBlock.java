package gas.world.blocks.gas;

import arc.graphics.g2d.*;
import gas.gen.*;
import gas.world.*;
import mindustry.annotations.Annotations.*;
import mindustry.graphics.*;
import mindustry.world.meta.*;

public class GasGasBlock extends GasBlock {

    public @Load("@-gas") TextureRegion gasRegion;
    @Load("@-top")
    public TextureRegion topRegion;

    @Load("@-bottom")
    public TextureRegion bottomRegion;

    public GasGasBlock(String name) {
        super(name);
        update = true;
        solid = true;
        hasGasses = true;
        group = BlockGroup.liquids;
        outputsGas = true;
        envEnabled |= Env.space | Env.underwater;
    }

    @Override
    public TextureRegion[] icons() {
        return new TextureRegion[] { bottomRegion, topRegion };
    }

    public class GasBuild extends GasBuilding {

        @Override
        public void draw() {
            float rotation = rotate ? rotdeg() : 0;
            Draw.rect(bottomRegion, x, y, rotation);
//            liquids.currentAmount()
            if (gasses.currentAmount() > 0.001f) {
                Drawf.liquid(gasRegion, x, y, gasses.currentAmount() / gasCapacity, gasses.current().color);
            }
            Draw.rect(topRegion, x, y, rotation);
        }
    }
}
