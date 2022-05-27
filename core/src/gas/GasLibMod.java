package gas;

import arc.func.*;
import arc.struct.*;
import arc.util.*;
import arc.util.serialization.*;
import arc.util.serialization.Json.*;
import gas.annotations.GasAnnotations.*;
import gas.content.*;
import gas.gen.*;
import gas.type.*;
import gas.world.consumers.*;
import mindustry.*;
import mindustry.ctype.*;
import mindustry.mod.*;
import mma.*;
import mma.annotations.ModAnnotations.*;

import static gas.GasVars.loaded;

@MainClass()
@GasClassMapGenerator()
public class GasLibMod extends MMAMod{


    public GasLibMod(){
        super();
        GasClassMap.init();
        GasEntityMapping.init();
        GasVars.create();

        ContentParser contentParser = Reflect.get(Mods.class, Vars.mods, "parser");
        Json parser = Reflect.get(ContentParser.class, contentParser, "parser");
        Reflect.<ObjectMap<ContentType, ?>>get(ContentParser.class, contentParser, "parsers")
        .put(Gasses.gasType(), Reflect.invoke(ContentParser.class,contentParser,"parser",new Object[]{Gasses.gasType(),(Func<String,Content>)Gas::new},ContentType.class,Func.class));
        parser.setSerializer(GasStack.class, new Serializer<>(){
            @Override
            public void write(Json json, GasStack object, Class knownType){
                json.writeValue("gas", object.gas);
                json.writeValue("amount", object.amount);
            }

            @Override
            public GasStack read(Json json, JsonValue jsonData, Class type){
                try{
                    if(jsonData.isString() && jsonData.asString().contains("/")){
                        String[] split = jsonData.asString().split("/");
                        if(type == GasStack.class){
                            return json.fromJson(GasStack.class, "{gas: " + split[0] + ", amount: " + split[1] + "}");
                        }
                    }
                   /* System.out.println("jsonData: "+jsonData);
                    for(JsonValue c : jsonData){
                        System.out.println("c: "+c);
                    }*/
                    return new GasStack(json.fromJson(Gas.class, jsonData.get("gas").asString()), jsonData.get("amount").asFloat());
                }catch(Exception exception){
                    throw new SerializationException("Unable to convert value to required type: " + jsonData + " (" + type.getName() + ")", exception);
                }
            }
        });
        parser.setSerializer(ConsumeGas.class, new Serializer<>(){
            @Override
            public void write(Json json, ConsumeGas object, Class knownType){
                json.writeValue("gas", object.gas);
                json.writeValue("amount", object.amount);
            }

            @Override
            public ConsumeGas read(Json json, JsonValue jsonData, Class type){
                try{
                    if(jsonData.isString() && jsonData.asString().contains("/")){
                        String[] split = jsonData.asString().split("/");
                        if(type == ConsumeGas.class){
                            return json.fromJson(ConsumeGas.class, "{gas: " + split[0] + ", amount: " + split[1] + "}");
                        }

                    }
                    return new ConsumeGas(json.fromJson(Gas.class, jsonData.get("gas").toString()), json.fromJson(Float.class, jsonData.get("amount").toString()));
                }catch(Exception exception){
                    throw new SerializationException("Unable to convert value to required type: " + jsonData + " (" + type.getName() + ")", exception);
                }
            }
        });
    }

    public void init(){
        if(!loaded) return;
        super.init();
    }

    public void loadContent(){
        super.loadContent();
    }
}
