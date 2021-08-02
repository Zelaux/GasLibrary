package gas.io;

import arc.util.io.Reads;
import arc.util.io.Writes;
import gas.annotations.GasAnnotations;
import gas.type.Gas;
import mindustry.Vars;
import mindustry.ctype.ContentType;
import mindustry.io.TypeIO;

@GasAnnotations.TypeIOHandler
public class ModTypeIO extends TypeIO {
    public static void writeGas(Writes writes, Gas gas) {
        writes.i(gas.id);
    }

    public static Gas readGas(Reads reads) {
        return Vars.content.getByID(ContentType.typeid_UNUSED, reads.s());
    }
}

