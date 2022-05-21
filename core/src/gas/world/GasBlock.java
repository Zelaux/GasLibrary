package gas.world;

import acontent.world.meta.*;
import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import gas.annotations.*;
import gas.content.*;
import gas.gen.*;
import gas.type.*;
import gas.world.consumers.*;
import gas.world.meta.*;
import mindustry.ctype.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;
import mma.annotations.*;

import static mindustry.Vars.*;

@GasAnnotations.GasAddition
public class GasBlock extends Block{
    @ModAnnotations.Load("noon")
    public TextureRegion noon;
//    public final GasConsumers consumes = new GasConsumers();
    /** If true, gasBuildings have a GasModule. */
    public boolean hasGasses = false;
    public float gasCapacity;
    public boolean outputsGas = false;
    public AStats aStats = new AStats();
    public boolean[] gasFilter = {};

    public GasBlock(String name){
        super(name);
        super.stats = aStats.copy(stats);
        this.gasCapacity = 10;
    }

    public void getDependencies(Cons<UnlockableContent> cons){
        for(ItemStack stack : this.requirements){
            cons.get(stack.item);
        }
        //also requires inputs
        for(var c : consumeBuilder){
            if(c.optional) continue;

            if(c instanceof ConsumeItems i){
                for(ItemStack stack : i.items){
                    cons.get(stack.item);
                }
            }else if(c instanceof ConsumeLiquid i){
                cons.get(i.liquid);
            }else if(c instanceof ConsumeLiquids i){
                for(var stack : i.liquids){
                    cons.get(stack.liquid);
                }
            }else if(c instanceof ConsumeGas i){
                cons.get(i.gas);
            }else if(c instanceof ConsumeGasses i){
                for(var stack : i.gasStacks){
                    cons.get(stack.gas);
                }
            }
        }
    }

    @Override
    public void init(){
//        localizedName = Core.bundle.get(getContentType() + "." + this.name + ".name", localizedName);
//        description = Core.bundle.get(getContentType() + "." + this.name + ".description",description);
//        details = Core.bundle.get(getContentType() + "." + this.name + ".details",details);
//        super.init();
        //disable standard shadow
        if(customShadow){
            hasShadow = false;
        }

        if(fogRadius > 0){
            flags = flags.with(BlockFlag.hasFogRadius);
        }

        //initialize default health based on size
        if(health == -1){
            boolean round = false;
            if(scaledHealth < 0){
                scaledHealth = 40;

                float scaling = 1f;
                for(var stack : requirements){
                    scaling += stack.item.healthScaling;
                }

                scaledHealth *= scaling;
                round = true;
            }

            health = round ?
            Mathf.round(size * size * scaledHealth, 5) :
            (int)(size * size * scaledHealth);
        }

        clipSize = Math.max(clipSize, size * tilesize);

        if(emitLight){
            clipSize = Math.max(clipSize, lightRadius * 2f);
        }

        if(group == BlockGroup.transportation || category == Category.distribution){
            acceptsItems = true;
        }

        offset = ((size + 1) % 2) * tilesize / 2f;
        sizeOffset = -((size - 1) / 2);

        if(requirements.length > 0){
            buildCost = 0f;
            for(ItemStack stack : requirements){
                buildCost += stack.amount * stack.item.cost;
            }
        }

        buildCost *= buildCostMultiplier;

        consumers = consumeBuilder.toArray(Consume.class);
        optionalConsumers = consumeBuilder.select(consume -> consume.optional && !consume.ignore()).toArray(Consume.class);
        nonOptionalConsumers = consumeBuilder.select(consume -> !consume.optional && !consume.ignore()).toArray(Consume.class);
        updateConsumers = consumeBuilder.select(consume -> consume.update && !consume.ignore()).toArray(Consume.class);
        hasConsumers = consumers.length > 0;
        itemFilter = new boolean[content.items().size];
        liquidFilter = new boolean[content.liquids().size];
        gasFilter = new boolean[Gasses.all().size];

        for(Consume cons : consumers){
            cons.apply(this);
        }

        setBars();

        stats.useCategories = true;

        //TODO check for double power consumption

        if(!logicConfigurable){
            configurations.each((key, val) -> {
                if(UnlockableContent.class.isAssignableFrom(key)){
                    logicConfigurable = true;
                }
            });
        }

        if(!outputsPower && consPower != null && consPower.buffered){
            Log.warn("Consumer using buffered power: @. Disabling buffered power.", name);
            consPower.buffered = false;
        }

        if(buildVisibility == BuildVisibility.sandboxOnly){
            hideDetails = false;
        }
    }

    public void setStats(){
//        super.setStats();
        super.setStats();
        if(hasGasses && gasCapacity > 0){
            aStats.add(GasStats.gasCapacity, gasCapacity, GasStatUnit.gasUnits);
        }

    }

    @Override
    public void load(){
        super.load();
        GasContentRegions.loadRegions(this);
    }

    @Override
    public void setBars(){
        super.setBars();

        //gasses added last
        if(hasGasses){
            //TODO gasses need to be handled VERY carefully. there are several potential possibilities:
            //1. no consumption or output (conduit/tank)
            // - display current(), 1 bar
            //2. static set of inputs and outputs
            // - create bars for each input/output, straightforward
            //3. TODO dynamic input/output combo???
            // - confusion

            boolean added = false;

            //TODO handle in consumer
            //add bars for *specific* consumed gasses
            for(var consl : consumers){
                if(consl instanceof ConsumeGas gas){
                    added = true;
                    addGasBar(gas.gas);
                }else if(consl instanceof ConsumeGasses multi){
                    added = true;
                    for(var stack : multi.gasStacks){
                        addGasBar(stack.gas);
                    }
                }
            }

            //nothing was added, so it's safe to add a dynamic liquid bar (probably?)
            if(!added){
                addGasBar(build -> build.gasses.current());
            }
        }
    }
    public void addGasBar(Gas gas){
        addBar("gas-" + gas.name, (GasBuilding entity) -> !gas.unlocked() ? null : new Bar(
        () -> gas.localizedName,
        gas::barColor,
        () -> entity.gasses.get(gas) / gasCapacity
        ));
    }

    /** Adds a liquid bar that dynamically displays a liquid type. */
    public <T extends GasBuilding> void addGasBar(Func<T, Gas> current){
        //noinspection unchecked
        addBar("gas", (GasBuilding entity) -> new Bar(
        () -> current.get((T)entity) == null || entity.gasses.get(current.get((T)entity)) <= 0.001f ? Core.bundle.get("bar.gas") : current.get((T)entity).localizedName,
        () -> current.get((T)entity) == null ? Color.clear : current.get((T)entity).barColor(),
        () -> current.get((T)entity) == null ? 0f : entity.gasses.get(current.get((T)entity)) / gasCapacity)
        );
    }

    public boolean positionsValid(int x, int y, int x1, int y1){
        return false;
    }
}
