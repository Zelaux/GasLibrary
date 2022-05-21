package gas.world.modules;

import arc.math.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import gas.content.*;
import gas.type.*;
import mindustry.world.modules.*;

import java.util.*;

import static mindustry.Vars.content;

public class GasModule extends BlockModule{
    private static final int windowSize = 3;
    private static final Interval flowTimer = new Interval(2);
    private static final float pollScl = 20f;
    private static final Bits cacheBits = new Bits();
    private static WindowedMean[] cacheFlow;
    private static float[] cacheSums;
    private static float[] displayFlow;
    private float[] gasses = new float[Gasses.all().size];
    private Gas current = Gasses.getByID(0);

    private @Nullable
    WindowedMean[] flow;

    public void updateFlow(){
        if(flowTimer.get(1, pollScl)){
            if(flow == null){
                if(cacheFlow == null || cacheFlow.length != gasses.length){
                    cacheFlow = new WindowedMean[gasses.length];
                    for(int i = 0; i < gasses.length; i++){
                        cacheFlow[i] = new WindowedMean(windowSize);
                    }
                    cacheSums = new float[gasses.length];
                    displayFlow = new float[gasses.length];
                }else{
                    for(int i = 0; i < gasses.length; i++){
                        cacheFlow[i].reset();
                    }
                    Arrays.fill(cacheSums, 0);
                    cacheBits.clear();
                }

                Arrays.fill(displayFlow, -1);

                flow = cacheFlow;
            }

            boolean updateFlow = flowTimer.get(30);

            for(int i = 0; i < gasses.length; i++){
                flow[i].add(cacheSums[i]);
                if(cacheSums[i] > 0){
                    cacheBits.set(i);
                }
                cacheSums[i] = 0;

                if(updateFlow){
                    displayFlow[i] = flow[i].hasEnoughData() ? flow[i].mean() / pollScl : -1;
                }
            }
        }
    }

    public void stopFlow(){
        flow = null;
    }

    /** @return current gas's flow rate in u/s; any value < 0 means 'not ready'. */
    public float getFlowRate(Gas gas){
        return flow == null ? -1f : displayFlow[gas.id] * 60;
    }

    public boolean hasFlowGas(Gas gas){
        return flow != null && cacheBits.get(gas.id);
    }

    /** Last received or loaded gas. Only valid for gas modules with 1 type of gas. */
    public Gas current(){
        return current;
    }

    public void reset(Gas gas, float amount){
        Arrays.fill(gasses, 0f);
        gasses[gas.id] = amount;
        current = gas;
    }

    public float currentAmount(){
        return gasses[current.id];
    }

    public float get(Gas gas){
        return gasses[gas.id];
    }

    public void clear(){
        Arrays.fill(gasses, 0);
    }

    public void add(Gas gas, float amount){
        gasses[gas.id] += amount;
        current = gas;

        if(flow != null){
            cacheSums[gas.id] += Math.max(amount, 0);
        }
    }

    public void handleFlow(Gas gas, float amount){
        if(flow != null){
            cacheSums[gas.id] += Math.max(amount, 0);
        }
    }

    public void remove(Gas gas, float amount){
        //cap to prevent negative removal
        add(gas, Math.max(-amount, -gasses[gas.id]));
    }

    public void each(GasConsumer cons){
        for(int i = 0; i < gasses.length; i++){
            if(gasses[i] > 0){
                cons.accept(Gasses.getByID(i), gasses[i]);
            }
        }
    }

    public float sum(GasCalculator calc){
        float sum = 0f;
        for(int i = 0; i < gasses.length; i++){
            if(gasses[i] > 0){
                sum += calc.get(Gasses.getByID(i), gasses[i]);
            }
        }
        return sum;
    }

    @Override
    public void write(Writes write){
        int amount = 0;
        for(float gas : gasses){
            if(gas > 0) amount++;
        }

        write.s(amount); //amount of liquids

        for(int i = 0; i < gasses.length; i++){
            if(gasses[i] > 0){
                write.s(i); //gas ID
                write.f(gasses[i]); //gas amount
            }
        }
    }

    @Override
    public void read(Reads read, boolean legacy){
        Arrays.fill(gasses, 0);
        int count = legacy ? read.ub() : read.s();

        for(int j = 0; j < count; j++){
            Gas liq = Gasses.getByID(legacy ? read.ub() : read.s());
            int liquidid = liq.id;
            float amount = read.f();
            gasses[liquidid] = amount;
            if(amount > 0){
                current = liq;
            }
        }
    }

    public interface GasConsumer{
        void accept(Gas gas, float amount);
    }

    public interface GasCalculator{
        float get(Gas gas, float amount);
    }
}
