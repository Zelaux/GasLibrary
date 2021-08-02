package gas.world.modules;

import gas.type.Gas;
import arc.math.Mathf;
import arc.math.WindowedMean;
import arc.util.Interval;
import arc.util.Nullable;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.ctype.ContentType;
import mindustry.world.modules.BlockModule;

import java.util.Arrays;

public class GasModule extends BlockModule {
    private static final int windowSize = 3;
    private static final int updateInterval = 60;
    private static final Interval flowTimer = new Interval(2);
    private static final float pollScl = 20.0F;
    private float[] gases;
    private float total;
    private Gas current;
    private float smoothLiquid;
    private boolean hadFlow;
    @Nullable
    private WindowedMean flow;
    private float lastAdded;
    private float currentFlowRate;

    public GasModule() {
        this.gases = new float[Vars.content.getBy(ContentType.typeid_UNUSED).size];
        this.current = Vars.content.getByID(ContentType.typeid_UNUSED,0);
    }

    public void update(boolean showFlow) {
        this.smoothLiquid = Mathf.lerpDelta(this.smoothLiquid, this.currentAmount(), 0.1F);
        if (showFlow) {
            if (flowTimer.get(1, 20.0F)) {
                if (this.flow == null) {
                    this.flow = new WindowedMean(3);
                }

                if (this.lastAdded > 1.0E-4F) {
                    this.hadFlow = true;
                }

                this.flow.add(this.lastAdded);
                this.lastAdded = 0.0F;
                if (this.currentFlowRate < 0.0F || flowTimer.get(60.0F)) {
                    this.currentFlowRate = this.flow.hasEnoughData() ? this.flow.mean() / 20.0F : -1.0F;
                }
            }
        } else {
            this.currentFlowRate = -1.0F;
            this.flow = null;
            this.hadFlow = false;
        }

    }

    public float getFlowRate() {
        return this.currentFlowRate * 60.0F;
    }

    public boolean hadFlow() {
        return this.hadFlow;
    }

    public float smoothAmount() {
        return this.smoothLiquid;
    }

    public float total() {
        return this.total;
    }

    public Gas current() {
        return this.current;
    }

    public void reset(Gas liquid, float amount) {
        Arrays.fill(this.gases, 0.0F);
        this.gases[liquid.id] = amount;
        this.total = amount;
        this.current = liquid;
    }

    public float currentAmount() {
        return this.gases[this.current.id];
    }

    public float get(Gas liquid) {
        return this.gases[liquid.id];
    }

    public void clear() {
        this.total = 0.0F;
        Arrays.fill(this.gases, 0.0F);
    }

    public void add(Gas liquid, float amount) {
        float[] var10000 = this.gases;
        short var10001 = liquid.id;
        var10000[var10001] += amount;
        this.total += amount;
        this.current = liquid;
        if (this.flow != null) {
            this.lastAdded += Math.max(amount, 0.0F);
        }

    }

    public void remove(Gas liquid, float amount) {
        this.add(liquid, -amount);
    }

    public void each(GasConsumer cons) {
        for (int i = 0; i < this.gases.length; ++i) {
            if (this.gases[i] > 0.0F) {
                cons.accept(Vars.content.getByID(ContentType.typeid_UNUSED,i), this.gases[i]);
            }
        }

    }

    public float sum(GasCalculator calc) {
        float sum = 0.0F;

        for (int i = 0; i < this.gases.length; ++i) {
            if (this.gases[i] > 0.0F) {
                sum += calc.get(Vars.content.getByID(ContentType.typeid_UNUSED,i), this.gases[i]);
            }
        }

        return sum;
    }

    public void write(Writes write) {
        int amount = 0;
        float[] var3 = this.gases;
        int var4 = var3.length;

        for (int var5 = 0; var5 < var4; ++var5) {
            float liquid = var3[var5];
            if (liquid > 0.0F) {
                ++amount;
            }
        }

        write.s(amount);

        for (int i = 0; i < this.gases.length; ++i) {
            if (this.gases[i] > 0.0F) {
                write.s(i);
                write.f(this.gases[i]);
            }
        }

    }

    public void read(Reads read, boolean legacy) {
        Arrays.fill(this.gases, 0.0F);
        this.total = 0.0F;
        int count = legacy ? read.ub() : read.s();

        for (int j = 0; j < count; ++j) {
            int gasid = legacy ? read.ub() : read.s();
            float amount = read.f();
            this.gases[gasid] = amount;
            if (amount > 0.0F) {
                this.current = Vars.content.getByID(ContentType.typeid_UNUSED,gasid);
            }

            this.total += amount;
        }

    }

    public interface GasConsumer {
        void accept(Gas var1, float var2);
    }

    public interface GasCalculator {
        float get(Gas var1, float var2);
    }
}
