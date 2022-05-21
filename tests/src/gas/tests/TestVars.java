package gas.tests;

import arc.struct.*;
import gas.tests.content.*;
import mindustry.*;
import mma.*;

public class TestVars extends ModVars{
    private static final Seq<Runnable> onLoad = new Seq<>();

    static{
        new TestVars();
    }

    @SuppressWarnings({"unused", "RedundantSuppression"})
    public static void create(){

    }

    public static void load(){
        onLoad.each(Runnable::run);
        onLoad.clear();
    }

    @Override
    protected void onLoad(Runnable runnable){
        onLoad.add(runnable);
    }

    @Override
    protected void showException(Throwable ex){
        Vars.ui.showException(ex);
    }

    @Override
    public void loadContent(){
        TestItems.load();
        TesGasses.load();
        TestBlocks.load();
    }
}
