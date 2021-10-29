package gas.tools.gasBlockConverter;

import arc.util.*;

public class BlockConverterSettings{
    public static final String[] blackListClasses = {
    "CoreBlock",
    //distribution
    "Router",
    "MassDriver",
    //power
    "Battery",
    //units
    "CommandCenter",
    //payload
    "SingleBlockProducer",

    };
    public static final String[] blackListPackages = {
    "mindustry.world.blocks.legacy",
    "mindustry.world.blocks.payloads",
    "mindustry.world.blocks.distribution",
    "mindustry.world.blocks.experimental",
    };
    public static final String[] whiteList = {
    "ItemBridge",
    "PayloadBlock"
    };

    public static boolean inBlackList(ClassInfo classInfo){
        String packageName = classInfo.packageName();
        String className = classInfo.className();
        if(Structs.contains(blackListClasses, className)){
            return true;
        }
        if(Structs.contains(blackListPackages, packageName) && !Structs.contains(whiteList,className)){
            return true;
        }
        if(
        packageName.contains("world.blocks.environment") ||
        className.contains("Source") ||
        className.contains("Duct") ||
        className.contains("Conveyor") ||
        className.contains("Void")
        ){
            return true;
        }
        boolean result = !(packageName.contains("mindustry.world.blocks.") || packageName.contains("mindustry.world.draw"));
        return result;
    }
}
