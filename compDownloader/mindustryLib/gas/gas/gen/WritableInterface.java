package gas.gen;

import arc.util.io.Reads;
import arc.util.io.Writes;

public interface WritableInterface {
    void read(Reads reads);
    void write(Writes writes);
}
