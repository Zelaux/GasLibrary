package gas.io;

import arc.util.io.Reads;
import arc.util.io.Writes;
import gas.annotations.GasAnnotations;
import gas.content.Gasses;
import gas.type.Gas;
import mindustry.io.TypeIO;

@GasAnnotations.TypeIOHandler
public class GasTypeIO extends TypeIO {
    public static void writeGas(Writes writes, Gas gas) {
        writes.i(gas.id);
    }

    public static Gas readGas(Reads reads) {
        return Gasses.getByID(reads.s());
    }
}

