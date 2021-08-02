package gas.world.blocks.power;

import gas.content.Gasses;
import gas.gen.GasBuilding;
import gas.type.Gas;
import gas.world.consumers.ConsumeGasFilter;
import gas.annotations.GasAnnotations;
import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.util.Time;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.type.Item;
import mindustry.type.Liquid;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.consumers.ConsumeItemFilter;
import mindustry.world.consumers.ConsumeLiquidFilter;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

public class AllGenerator extends GasPowerGenerator {
    public float minItemEfficiency;
    public float itemDuration;
    public float minLiquidEfficiency;
    public float maxLiquidGenerate;
    public float minGasEfficiency;
    public float maxGasGenerate;
    public Effect generateEffect;
    public Effect explodeEffect;
    public Color heatColor;
    public @GasAnnotations.Load("@-top") TextureRegion topRegion;
    public @GasAnnotations.Load("@-liquid") TextureRegion liquidRegion;
    public @GasAnnotations.Load("@-gas") TextureRegion gasRegion;
    public boolean randomlyExplode;
    public boolean defaults;

    public AllGenerator(boolean hasItems, boolean hasLiquids, boolean hasGas, String name) {
        this(name);
        this.update = true;
        this.hasItems = hasItems;
        this.hasLiquids = hasLiquids;
        this.hasGas = hasGas;
        this.setDefaults();
    }

    public AllGenerator(String name) {
        super(name);
        this.minItemEfficiency = 0.2F;
        this.itemDuration = 70.0F;
        this.minLiquidEfficiency = 0.2F;
        this.minGasEfficiency = 0.2f;
        this.maxLiquidGenerate = 0.4F;
        this.maxGasGenerate = 0.4F;
        this.generateEffect = Fx.generatespark;
        this.explodeEffect = Fx.generatespark;
        this.heatColor = Color.valueOf("ff9b59");
        this.randomlyExplode = true;
        this.setDefaults();
        this.update=true;
    }


    protected void setDefaults() {
        if (defaults) return;
        if (this.hasItems) {
            (this.consumes.add(new ConsumeItemFilter((item) -> {
                return this.getItemEfficiency(item) >= this.minItemEfficiency;
            }))).update(false).optional(true, false);
        }

        if (this.hasLiquids) {
            (this.consumes.add(new ConsumeLiquidFilter((liquid) -> {
                return this.getLiquidEfficiency(liquid) >= this.minLiquidEfficiency;
            }, this.maxLiquidGenerate))).update(false).optional(true, false);
        }

        if (this.hasGas) {
            this.consumes.addGas(new ConsumeGasFilter((gas) -> {
                return this.getGasEfficiency(gas) >= this.minGasEfficiency;
            }, this.maxLiquidGenerate)).update(false).optional(true, false);
        }

        this.defaults = true;
    }

    public void init() {
        this.setDefaults();

        super.init();
    }

    public void setStats() {
        super.setStats();
        if (this.hasItems) {
            this.aStats.add(Stat.productionTime, this.itemDuration / 60.0F, StatUnit.seconds);
        }

    }

    protected float getItemEfficiency(Item item) {
        return 0.0F;
    }

    protected float getLiquidEfficiency(Liquid liquid) {
        return 0.0F;
    }

    protected float getGasEfficiency(Gas gas) {
        return 0.0F;
    }

    public class AllGeneratorBuild extends GasPowerGenerator.GasGeneratorBuild {
        public float explosiveness;
        public float heat;
        public float totalTime;

        @Override
        public Building init(Tile tile, Team team, boolean shouldAdd, int rotation) {
            Building building = super.init(tile, team, shouldAdd, rotation);
            return building;
        }

        @Override
        public GasBuilding create(Block block, Team team) {
            GasBuilding building = super.create(block, team);
            return building;
        }

        public AllGeneratorBuild() {
            super();
        }

        public boolean productionValid() {
            return this.generateTime > 0.0F;
        }

        public void updateTile() {
            float calculationDelta = this.delta();
            this.heat = Mathf.lerpDelta(this.heat, this.generateTime >= 0.001F ? 1.0F : 0.0F, 0.05F);
            if (!this.consValid()) {
                this.productionEfficiency = 0.0F;
            } else {
                Gas gas = null;

                Liquid liquid = null;

                for (Liquid other : Vars.content.liquids()) {
                    if (hasLiquids && this.liquids.get(other) >= 0.001F && getLiquidEfficiency(other) >= minLiquidEfficiency) {
                        liquid = other;
                        break;
                    }
                }

                for (Gas other : Gasses.all()) {
                    if (hasGas && this.gasses.get(other) >= 0.001F && getGasEfficiency(other) >= minGasEfficiency) {
                        gas = other;
                        break;
                    }
                }

                totalTime += heat * Time.delta;
                if (hasGas && gas != null && gasses.get(gas) >= 0.001F) {
                    float baseGasEfficiency = getGasEfficiency(gas);
                    float maximumPossible = maxGasGenerate * calculationDelta;
                    float used = Math.min(gasses.get(gas) * calculationDelta, maximumPossible);
                    gasses.remove(gas, used * power.graph.getUsageFraction());
                    productionEfficiency = baseGasEfficiency * used / maximumPossible;
                    if (used > 0.001F && Mathf.chance(0.05D * (double) this.delta())) {
                        generateEffect.at(this.x + Mathf.range(size*8f-5f), this.y + Mathf.range(size*8f-5f));
                    }
                } else if (hasLiquids && liquid != null && this.liquids.get(liquid) >= 0.001F) {
                    float baseLiquidEfficiency = getLiquidEfficiency(liquid);
                    float maximumPossible = maxLiquidGenerate * calculationDelta;
                    float used = Math.min(this.liquids.get(liquid) * calculationDelta, maximumPossible);
                    this.liquids.remove(liquid, used * this.power.graph.getUsageFraction());
                    this.productionEfficiency = baseLiquidEfficiency * used / maximumPossible;
                    if (used > 0.001F && Mathf.chance(0.05D * (double) this.delta())) {
                        generateEffect.at(this.x + Mathf.range(3.0F), this.y + Mathf.range(3.0F));
                    }
                } else if (hasItems) {
                    if (this.generateTime <= 0.0F && this.items.total() > 0) {
                        generateEffect.at(this.x + Mathf.range(3.0F), this.y + Mathf.range(3.0F));
                        Item item = this.items.take();
                        this.productionEfficiency = getItemEfficiency(item);
                        this.explosiveness = item.explosiveness;
                        this.generateTime = 1.0F;
                    }

                    if (this.generateTime > 0.0F) {
                        this.generateTime -= Math.min(1.0F / itemDuration * this.delta() * this.power.graph.getUsageFraction(), this.generateTime);
                        if (randomlyExplode && Vars.state.rules.reactorExplosions && Mathf.chance((double) this.delta() * 0.06D * (double) Mathf.clamp(this.explosiveness - 0.5F))) {
                            Core.app.post(() -> {
                                this.damage(Mathf.random(11.0F));
                                explodeEffect.at(this.x + Mathf.range((float) (size * 8) / 2.0F), this.y + Mathf.range((float) (size * 8) / 2.0F));
                            });
                        }
                    } else {
                        this.productionEfficiency = 0.0F;
                    }
                }
            }
        }


        public void drawLight() {
            Drawf.light(this.team, this.x, this.y, (60.0F + Mathf.absin(10.0F, 5.0F)) * (float) size, Color.orange, 0.5F * this.heat);
        }

        public void draw() {
            super.draw();
            if (hasItems) {
                Draw.color(heatColor);
                Draw.alpha(this.heat * 0.4F + Mathf.absin(Time.time, 8.0F, 0.6F) * this.heat);
                Draw.rect(topRegion, this.x, this.y);
                Draw.reset();
            }

            if (hasLiquids) {
                Drawf.liquid(liquidRegion, this.x, this.y, this.liquids.total() / liquidCapacity, this.liquids.current().color);
            }

            if (hasGas) {
                Drawf.liquid(gasRegion, this.x, this.y, this.gasses.total() / gasCapacity, this.gasses.current().color);
            }
        }
    }
}
