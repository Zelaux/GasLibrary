package gas.tools.gasBlockConverter;

import arc.files.*;
import com.github.javaparser.ast.*;

public class ClassEntry{
    public final Fi fi;
    public final CompilationUnit compilationUnit;

    public ClassEntry(Fi fi, CompilationUnit compilationUnit){
        this.fi = fi;
        this.compilationUnit = compilationUnit;
    }
}
