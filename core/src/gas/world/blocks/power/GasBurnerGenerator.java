package gas.world.blocks.power;

import gas.type.Gas;
import gas.annotations.GasAnnotations;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import mindustry.graphics.Drawf;
import mindustry.type.Item;
import mindustry.type.Liquid;
@GasAnnotations.GasAddition(analogue = "auto")
public class GasBurnerGenerator extends AllGenerator {
    public @GasAnnotations.Load(value = "@-turbine#", length = 2) TextureRegion[] turbineRegions;
    public @GasAnnotations.Load("@-cap") TextureRegion capRegion;
    public float turbineSpeed = 2.0F;


    public GasBurnerGenerator(boolean hasItems, boolean hasLiquids, boolean hasGas, String name) {
        super(hasItems, hasLiquids, hasGas, name);
    }

    public GasBurnerGenerator(String name) {
        super(name);
    }

    protected float getLiquidEfficiency(Liquid liquid) {
        return liquid.flammability;
    }

    protected float getItemEfficiency(Item item) {
        return item.flammability;
    }

    @Override
    protected float getGasEfficiency(Gas gas) {
        return gas.flammability;
    }

    public TextureRegion[] icons() {
        return this.turbineRegions[0].found() ? new TextureRegion[]{this.region, this.turbineRegions[0], this.turbineRegions[1], this.capRegion} : super.icons();
    }

    public class AllBurnerGeneratorBuild extends AllGenerator.AllGeneratorBuild {
        public void draw() {
            super.draw();
            if (GasBurnerGenerator.this.turbineRegions[0].found()) {
                Draw.rect(GasBurnerGenerator.this.turbineRegions[0], this.x, this.y, this.totalTime * GasBurnerGenerator.this.turbineSpeed);
                Draw.rect(GasBurnerGenerator.this.turbineRegions[1], this.x, this.y, -this.totalTime * GasBurnerGenerator.this.turbineSpeed);
                Draw.rect(GasBurnerGenerator.this.capRegion, this.x, this.y);
                if (GasBurnerGenerator.this.hasLiquids) {
                    Drawf.liquid(GasBurnerGenerator.this.liquidRegion, this.x, this.y, this.liquids.total() / GasBurnerGenerator.this.liquidCapacity, this.liquids.current().color);
                }
                if (GasBurnerGenerator.this.hasGas) {
                    Drawf.liquid(GasBurnerGenerator.this.gasRegion, this.x, this.y, this.gasses.total() / GasBurnerGenerator.this.gasCapacity, this.gasses.current().color);
                }
            }

        }
    }
}