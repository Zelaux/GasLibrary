package gas;

import gas.content.Gasses;
import gas.type.Gas;

public class GasStack {
    public Gas gas;
    public float amount;

    public GasStack(Gas gas, float amount) {
        this.gas = gas;
        this.amount = amount;
    }

    protected GasStack() {
        gas = Gasses.oxygen;
    }

    public String toString() {
        return "GasStack{gas=" + gas + ", amount=" + amount + '}';
    }
}
