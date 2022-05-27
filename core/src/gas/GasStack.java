package gas;

import arc.struct.*;
import gas.annotations.*;
import gas.content.*;
import gas.type.*;

@GasAnnotations.GasAddition(description = "like LiquidStack but for Gas")
public class GasStack implements Comparable<GasStack>{
    public static GasStack[] empty = {};
    public Gas gas;
    public float amount;

    public GasStack(Gas gas, float amount){
        this.gas = gas;
        this.amount = amount;
    }

    protected GasStack(){
        gas = Gasses.getByID(0);
    }

    public static GasStack[] mult(GasStack[] stacks, float amount){
        GasStack[] copy = new GasStack[stacks.length];
        for(int i = 0; i < copy.length; i++){
            copy[i] = new GasStack(stacks[i].gas, stacks[i].amount * amount);
        }
        return copy;
    }

    public static GasStack[] with(Object... items){
        GasStack[] stacks = new GasStack[items.length / 2];
        for(int i = 0; i < items.length; i += 2){
            stacks[i / 2] = new GasStack((Gas)items[i], ((Number)items[i + 1]).floatValue());
        }
        return stacks;
    }

    public static Seq<GasStack> list(Object... items){
        Seq<GasStack> stacks = new Seq<>(items.length / 2);
        for(int i = 0; i < items.length; i += 2){
            stacks.add(new GasStack((Gas)items[i], ((Number)items[i + 1]).floatValue()));
        }
        return stacks;
    }

    public static GasStack of(Gas gas, float amount){
        return new GasStack(gas, amount);
    }

    public GasStack set(Gas gas, float amount){
        this.gas = gas;
        this.amount = amount;
        return this;
    }

    public GasStack copy(){
        return new GasStack(gas, amount);
    }

    public boolean equals(GasStack other){
        return other != null && other.gas == gas && other.amount == amount;
    }

    @Override
    public int compareTo(GasStack liquidStack){
        return gas.compareTo(liquidStack.gas);
    }

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(!(o instanceof GasStack stack)) return false;
        return amount == stack.amount && gas == stack.gas;
    }

    @Override
    public String toString(){
        return "GasStack{" +
        "gas=" + gas +
        ", amount=" + amount +
        '}';
    }
}
