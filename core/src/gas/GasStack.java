package gas;

import gas.annotations.GasAnnotations;
import gas.type.Gas;
@GasAnnotations.GasAddition(description = "like ItemStack but for Gas")
public class GasStack {
    public Gas gas;
    public float amount;

    public GasStack(Gas gas, float amount) {
        this.gas = gas;
        this.amount = amount;
    }

    protected GasStack() {
        gas = null;
    }

    public String toString() {
        return "GasStack{gas=" + gas + ", amount=" + amount + '}';
    }
}
