package gas.world.consumers;

import arc.func.Cons;
import arc.struct.Bits;
import arc.util.Nullable;
import arc.util.Structs;
import gas.content.Gasses;
import mindustry.world.consumers.Consume;
import mindustry.world.consumers.ConsumeType;
import mindustry.world.consumers.Consumers;
import mindustry.world.meta.Stats;

import java.util.Objects;

public class GasConsumers extends Consumers {
    public final Bits gasFilter;
    protected Consume[] map = new Consume[ConsumeType.values().length + 1];
    protected Consume[] results;
    protected Consume[] optionalResults;

    public GasConsumers() {
        super();
        this.gasFilter = new Bits(Gasses.all().size);
    }

    public void each(Cons<Consume> c) {
        Consume[] var2 = this.map;
        int var3 = var2.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            Consume cons = var2[var4];
            if (cons != null) {
                c.get(cons);
            }
        }

    }

    public void init() {
        this.results = Structs.filter(Consume.class, this.map, Objects::nonNull);
        this.optionalResults = Structs.filter(Consume.class, this.map, (m) -> {
            return m != null && m.isOptional();
        });
        for (Consume cons : this.results) {
            cons.applyItemFilter(this.itemFilters);
            cons.applyLiquidFilter(this.liquidfilters);
            if (cons instanceof GasConsume) {
                ((GasConsume) cons).applyGasFilter(this.gasFilter);
            } else {
            }
        }
    }

    @Nullable
    public Consume[] all() {
        return results;
    }

    public Consume[] optionals() {
        return optionalResults;
    }

    public <T extends Consume> T add(T consume) {
        if (consume instanceof GasConsume) {
            return (T) addGas(((GasConsume) consume));
        }
        map[consume.type().ordinal()] = (Consume) consume;
        return consume;
    }

    public void remove(ConsumeType type) {
        map[type.ordinal()] = null;
    }

    public boolean has(ConsumeType type) {
        return map[type.ordinal()] != null;
    }

    public <T extends Consume> T get(ConsumeType type) {
        if (map[type.ordinal()] == null) {
            throw new IllegalArgumentException("Block does not contain consumer of type '" + type + "'!");
        } else {
            return (T) map[type.ordinal()];
        }
    }

    public boolean hasGas() {
        return map[3] != null;
    }

    public Consume getGas() {
        return (Consume) get(3);
    }

    public Object get(int type) {
        if (map[type] == null) {
            throw new IllegalArgumentException("Block does not contain consumer of type '" + type + "'!");
        } else {
            return map[type];
        }
    }

    public <T extends GasConsume> T addGas(T consume) {
        map[3] = consume;
        return consume;
    }

    public void display(Stats stats) {
        for (Consume c : map) {
            if (c != null) {
                c.display(stats);
            }
        }

    }
}
