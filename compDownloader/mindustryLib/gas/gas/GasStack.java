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
/*block.gas-library-java-gas-source.name = Источник газов
block.gas-library-java-gas-source.description = Постоянно выдаёт газ. Только песочница.
block.gas-library-java-gas-void.name = Газовый вакуум
    block.gas-library-java-gas-void.description = Уничтожает любые газы. Только песочница.
*/
    protected GasStack() {
        gas = null;
    }

    public String toString() {
        return "GasStack{gas=" + gas + ", amount=" + amount + '}';
    }
}
