package gas.world.blocks;

import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.gen.Building;
import mindustry.gen.Buildingc;

public interface BuildingTaskQueue extends Buildingc {
    ObjectMap<Buildingc,Seq<Runnable>> func=new ObjectMap<>();
    default void addTast(Buildingc building,Runnable runnable){
        func.get(building,Seq::new).add(runnable);
    }
    default void runUpdateTaskQueue(){
        runUpdateTaskQueue(this);
    }
    default void addTast(Runnable runnable){
        addTast(this,runnable);
    }
    public default void runUpdateTaskQueue(Buildingc building){
if (!func.containsKey(building))return;
        func.get(building,Seq::new).each(Runnable::run);
        func.remove(building);
//        updateTaskQueue.clear();
    }
}
