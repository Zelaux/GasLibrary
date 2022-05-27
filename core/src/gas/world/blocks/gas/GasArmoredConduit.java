package gas.world.blocks.gas;

import gas.type.*;
import gas.world.*;
import gas.world.blocks.distribution.*;
import mindustry.gen.*;
import mindustry.world.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.liquid.*;

public class GasArmoredConduit extends GasConduit {

    public GasArmoredConduit(String name) {
        super(name);
        leaks = false;
    }

    @Override
    public boolean blends(Tile tile, int rotation, int otherx, int othery, int otherrot, Block otherblockSimple) {
        if (!(otherblockSimple instanceof GasBlock otherblock))return false;
        return (otherblock.outputsGas && blendsArmored(tile, rotation, otherx, othery, otherrot, otherblock)) ||
        (lookingAt(tile, rotation, otherx, othery, otherblock) && otherblock.hasGasses);
    }

    public class GasArmoredConduitBuild extends GasConduit.GasConduitBuild {

        @Override
        public boolean acceptGas(Building source, Gas gas) {
            return super.acceptGas(source, gas) && (tile == null || source.block instanceof Conduit || source.block instanceof DirectionGasBridge ||
            source.tile.absoluteRelativeTo(tile.x, tile.y) == rotation);
        }
    }
}
