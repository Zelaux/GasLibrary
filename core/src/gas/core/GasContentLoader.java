package gas.core;

import gas.content.GasBlocks;
import gas.content.Gasses;
import arc.Events;
import arc.files.Fi;
import arc.func.Cons;
import arc.graphics.Pixmap;
import arc.struct.ObjectMap;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Nullable;
import mindustry.content.*;
import mindustry.core.ContentLoader;
import mindustry.ctype.Content;
import mindustry.ctype.ContentList;
import mindustry.ctype.ContentType;
import mindustry.ctype.MappableContent;
import mindustry.entities.bullet.BulletType;
import mindustry.game.EventType;
import mindustry.mod.Mods;
import mindustry.type.*;
import mindustry.world.Block;
import mindustry.world.ColorMapper;

import static arc.Core.files;
import static mindustry.Vars.constants;
import static mindustry.Vars.mods;

public class GasContentLoader extends ContentLoader {
    private ObjectMap<String, MappableContent>[] contentNameMap = new ObjectMap[ContentType.all.length];
    private Seq<Content>[] contentMap = new Seq[ContentType.all.length];
    private MappableContent[][] temporaryMapper;
    private @Nullable
    Mods.LoadedMod currentMod;
    private @Nullable Content lastAdded;
    private ObjectSet<Cons<Content>> initialization = new ObjectSet<>();
    protected boolean loadModContent=false;
    private final ContentList[] content = {
            new Items(),
            new StatusEffects(),
            new Liquids(),
            new Bullets(),
            new UnitTypes(),
            new Blocks(),
            new Loadouts(),
            new Weathers(),
            new Planets(),
            new SectorPresets(),
            new TechTree(),
    };
    private final ContentList[] modContent = {
//            new ModItems(),
//            new ModStatusEffects(),
//            new ModLiquids(),
//            new Gasses(),
//            new ModBullets(),
//            new ModUnitTypes(),
            new GasBlocks(),
//            new ModBlocks(),
//            new ModPlanets(),
//            new ModSectorPresets(),
//            new ModTechTree(),
    };

    public GasContentLoader(){
        clear();
    }
    public GasContentLoader(Cons<ContentList> cons){
        createModContent(cons);
    }

    /** Clears all initialized content.*/
    public void clear(){
        contentNameMap = new ObjectMap[ContentType.all.length];
        contentMap = new Seq[ContentType.all.length];
        initialization = new ObjectSet<>();

        for(ContentType type : ContentType.all){
            contentMap[type.ordinal()] = new Seq<>();
            contentNameMap[type.ordinal()] = new ObjectMap<>();
        }
    }


    /** Creates all base types. */
    public void createBaseContent(){
        for(ContentList list : content){
            list.load();
        }
    }

    /** Creates mod content, if applicable. */
    public void createModContent(){
        loadModContent=true;
        for (ContentList list : modContent) {
            list.load();
        }
        loadModContent=false;
    }
    public void createModContent(Cons<ContentList> cons){
        loadModContent=true;
        for (ContentList list : modContent) {
            cons.get(list);
//            list.load();
        }
        loadModContent=false;
    }

    /** Logs content statistics.*/
    public void logContent(){
        //check up ID mapping, make sure it's linear (debug only)
        for(Seq<Content> arr : contentMap){
            for(int i = 0; i < arr.size; i++){
                int id = arr.get(i).id;
                if(id != i){
                    throw new IllegalArgumentException("Out-of-order IDs for content '" + arr.get(i) + "' (expected " + i + " but got " + id + ")");
                }
            }
        }

        Log.debug("--- CONTENT INFO ---");
        for(int k = 0; k < contentMap.length; k++){
            Log.debug("[@]: loaded @", ContentType.all[k].name(), contentMap[k].size);
        }
        Log.debug("Total content loaded: @", Seq.with(ContentType.all).mapInt(c -> contentMap[c.ordinal()].size).sum());
        Log.debug("-------------------");
    }

    /** Calls Content#init() on everything. Use only after all modules have been created.*/
    public void init(){
        initialize(Content::init);
        if(constants != null) constants.init();
        Events.fire(new EventType.ContentInitEvent());
    }

    /** Calls Content#load() on everything. Use only after all modules have been created on the client.*/
    public void load(){
        initialize(Content::load);
    }

    /** Initializes all content with the specified function. */
    private void initialize(Cons<Content> callable){
        if(initialization.contains(callable)) return;

        for(ContentType type : ContentType.all){
            for(Content content : contentMap[type.ordinal()]){
                try{
                    callable.get(content);
                }catch(Throwable e){
                    if(content.minfo.mod != null){
                        Log.err(e);
                        mods.handleContentError(content, e);
                    }else{
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        initialization.add(callable);
    }

    /** Loads block colors. */
    public void loadColors(){
        Pixmap pixmap = new Pixmap(files.internal("sprites/block_colors.png"));
        for(int i = 0; i < pixmap.getWidth(); i++){
            if(blocks().size > i){
                int color = pixmap.get(i, 0);

                if(color == 0 || color == 255) continue;

                Block block = block(i);
                block.mapColor.rgba8888(color);
                //partial alpha colors indicate a square sprite
                block.squareSprite = block.mapColor.a > 0.5f;
                block.mapColor.a = 1f;
                block.hasColor = true;
            }
        }
        pixmap.dispose();
        ColorMapper.load();
    }

    public void dispose(){
        initialize(Content::dispose);
        clear();
    }

    /** Get last piece of content created for error-handling purposes. */
    public @Nullable Content getLastAdded(){
        return lastAdded;
    }

    /** Remove last content added in case of an exception. */
    public void removeLast(){
        if(lastAdded != null && contentMap[lastAdded.getContentType().ordinal()].peek() == lastAdded){
            contentMap[lastAdded.getContentType().ordinal()].pop();
            if(lastAdded instanceof MappableContent){
                contentNameMap[lastAdded.getContentType().ordinal()].remove(((MappableContent) lastAdded).name);
            }
        }
    }
    public static class ModedModContentInfo extends Content.ModContentInfo{

    }
    public void handleContent(Content content){
        this.lastAdded = content;
        if (loadModContent){
            content.minfo=new ModedModContentInfo();
        }
        contentMap[content.getContentType().ordinal()].add(content);
    }

    public void setCurrentMod(@Nullable Mods.LoadedMod mod){
        this.currentMod = mod;
    }

    public String transformName(String name){
        return currentMod == null ? name : currentMod.name + "-" + name;
    }

    public void handleMappableContent(MappableContent content){
        if(contentNameMap[content.getContentType().ordinal()].containsKey(content.name)){
            throw new IllegalArgumentException("Two content objects cannot have the same name! (issue: '" + content.name + "')");
        }
        if(currentMod != null){
            content.minfo.mod = currentMod;
            if(content.minfo.sourceFile == null){
                content.minfo.sourceFile = new Fi(content.name);
            }
        }
        contentNameMap[content.getContentType().ordinal()].put(content.name, content);
    }

    public void setTemporaryMapper(MappableContent[][] temporaryMapper){
        this.temporaryMapper = temporaryMapper;
    }

    public Seq<Content>[] getContentMap(){
        return contentMap;
    }

    public void each(Cons<Content> cons){
        for(Seq<Content> seq : contentMap){
            seq.each(cons);
        }
    }

    public <T extends MappableContent> T getByName(ContentType type, String name){
        if(contentNameMap[type.ordinal()] == null){
            return null;
        }
        return (T)contentNameMap[type.ordinal()].get(name);
    }

    public <T extends Content> T getByID(ContentType type, int id){

        if(temporaryMapper != null && temporaryMapper[type.ordinal()] != null && temporaryMapper[type.ordinal()].length != 0){
            //-1 = invalid content
            if(id < 0){
                return null;
            }
            if(temporaryMapper[type.ordinal()].length <= id || temporaryMapper[type.ordinal()][id] == null){
                return (T)contentMap[type.ordinal()].get(0); //default value is always ID 0
            }
            return (T)temporaryMapper[type.ordinal()][id];
        }

        if(id >= contentMap[type.ordinal()].size || id < 0){
            return null;
        }
        return (T)contentMap[type.ordinal()].get(id);
    }

    public <T extends Content> Seq<T> getBy(ContentType type){
        return (Seq<T>)contentMap[type.ordinal()].select(c->c.minfo instanceof ModedModContentInfo);
    }

    //utility methods, just makes things a bit shorter

    public Seq<Block> blocks(){
        return getBy(ContentType.block);
    }

    public Block block(int id){
        return getByID(ContentType.block, id);
    }

    public Block block(String name){
        return getByName(ContentType.block, name);
    }

    public Seq<Item> items(){
        return getBy(ContentType.item);
    }

    public Item item(int id){
        return getByID(ContentType.item, id);
    }

    public Seq<Liquid> liquids(){
        return getBy(ContentType.liquid);
    }

    public Liquid liquid(int id){
        return getByID(ContentType.liquid, id);
    }

    public Seq<BulletType> bullets(){
        return getBy(ContentType.bullet);
    }

    public BulletType bullet(int id){
        return getByID(ContentType.bullet, id);
    }

    public Seq<SectorPreset> sectors(){
        return getBy(ContentType.sector);
    }

    public Seq<UnitType> units(){
        return getBy(ContentType.unit);
    }

    public Seq<Planet> planets(){
        return getBy(ContentType.planet);
    }

}
