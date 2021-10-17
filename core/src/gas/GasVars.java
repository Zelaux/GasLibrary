package gas;

import arc.struct.Seq;
import arc.util.Log;
import gas.content.GasBlocks;
import mindustry.ctype.ContentList;
import mma.ModVars;

public class GasVars extends ModVars {
    static Seq<Runnable> onLoad = new Seq<>();
static {
    new GasVars();
}
    public static void create() {

    }

    @Override
    protected void onLoad(Runnable runnable) {
        onLoad.add(runnable);
    }

    @Override
    protected void showException(Throwable ex) {
        Log.err(ex);
    }

    @Override
    public ContentList[] getContentList() {
        return new ContentList[]{
                new GasBlocks(),
        };

    }

    @Override
    public String getFullName(String name) {
        return name;
    }
}
