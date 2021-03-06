package gas.world.blocks.production;

import mindustry.entities.units.*;
import gas.entities.comp.*;
import mindustry.entities.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import mindustry.content.*;
import gas.content.*;
import mindustry.world.blocks.defense.turrets.*;
import gas.world.meta.*;
import mindustry.annotations.Annotations.*;
import gas.world.blocks.units.*;
import mindustry.world.blocks.heat.*;
import arc.util.*;
import mindustry.world.blocks.legacy.*;
import mindustry.gen.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import arc.math.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.meta.*;
import gas.world.blocks.heat.*;
import gas.world.blocks.defense.*;
import gas.world.draw.*;
import mindustry.world.blocks.payloads.*;
import mindustry.world.blocks.distribution.*;
import gas.world.blocks.payloads.*;
import mindustry.world.*;
import gas.world.blocks.sandbox.*;
import mindustry.world.blocks.storage.*;
import gas.world.blocks.liquid.*;
import arc.struct.*;
import mindustry.game.*;
import gas.entities.*;
import mindustry.world.blocks.campaign.*;
import gas.world.blocks.defense.turrets.*;
import gas.world.blocks.distribution.*;
import gas.world.*;
import mindustry.world.consumers.*;
import gas.world.blocks.gas.*;
import gas.world.blocks.campaign.*;
import mindustry.world.modules.*;
import gas.ui.*;
import mindustry.world.blocks.environment.*;
import gas.world.consumers.*;
import arc.graphics.g2d.*;
import mindustry.world.blocks.*;
import gas.world.blocks.production.GasGenericCrafter.*;
import arc.*;
import mindustry.world.blocks.logic.*;
import gas.world.modules.*;
import gas.world.blocks.*;
import arc.graphics.*;
import gas.*;
import gas.io.*;
import mindustry.ui.*;
import gas.gen.*;
import gas.world.blocks.storage.*;
import mindustry.world.blocks.units.*;
import mindustry.graphics.*;
import gas.world.blocks.production.*;
import mindustry.world.blocks.production.Drill.*;
import arc.util.io.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import mindustry.logic.*;
import mindustry.world.blocks.power.*;
import mindustry.type.*;
import mindustry.world.blocks.sandbox.*;
import gas.world.blocks.power.*;
import static mindustry.Vars.*;

public class GasDrill extends GasBlock {

    public float hardnessDrillMultiplier = 50f;

    protected final ObjectIntMap<Item> oreCount = new ObjectIntMap<>();

    protected final Seq<Item> itemArray = new Seq<>();

    /**
     * Maximum tier of blocks this drill can mine.
     */
    public int tier;

    /**
     * Base time to drill one ore, in frames.
     */
    public float drillTime = 300;

    /**
     * How many times faster the drill will progress when boosted by liquid.
     */
    public float liquidBoostIntensity = 1.6f;

    /**
     * Speed at which the drill speeds up.
     */
    public float warmupSpeed = 0.015f;

    /**
     * Special exemption item that this drill can't mine.
     */
    @Nullable
    public Item blockedItem;

    // return variables for countOre
    @Nullable
    protected Item returnItem;

    protected int returnCount;

    /**
     * Whether to draw the item this drill is mining.
     */
    public boolean drawMineItem = true;

    /**
     * Effect played when an item is produced. This is colored.
     */
    public Effect drillEffect = Fx.mine;

    /**
     * Drill effect randomness. Block size by default.
     */
    public float drillEffectRnd = -1f;

    /**
     * Speed the drill bit rotates at.
     */
    public float rotateSpeed = 2f;

    /**
     * Effect randomly played while drilling.
     */
    public Effect updateEffect = Fx.pulverizeSmall;

    /**
     * Chance the update effect will appear.
     */
    public float updateEffectChance = 0.02f;

    public boolean drawRim = false;

    public boolean drawSpinSprite = true;

    public Color heatColor = Color.valueOf("ff5512");

    @Load("@-rim")
    public TextureRegion rimRegion;

    @Load("@-rotator")
    public TextureRegion rotatorRegion;

    @Load("@-top")
    public TextureRegion topRegion;

    @Load(value = "@-item", fallback = "drill-item-@size")
    public TextureRegion itemRegion;

    public GasDrill(String name) {
        super(name);
        update = true;
        solid = true;
        group = BlockGroup.drills;
        hasLiquids = true;
        liquidCapacity = 5f;
        hasItems = true;
        ambientSound = Sounds.drill;
        ambientSoundVolume = 0.018f;
        // drills work in space I guess
        envEnabled |= Env.space;
    }

    @Override
    public void init() {
        super.init();
        if (drillEffectRnd < 0)
            drillEffectRnd = size;
    }

    @Override
    public void drawPlanConfigTop(BuildPlan plan, Eachable<BuildPlan> list) {
        if (!plan.worldContext)
            return;
        Tile tile = plan.tile();
        if (tile == null)
            return;
        countOre(tile);
        if (returnItem == null || !drawMineItem)
            return;
        Draw.color(returnItem.color);
        Draw.rect(itemRegion, plan.drawx(), plan.drawy());
        Draw.color();
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("drillspeed", (GasDrillBuild e) -> new Bar(() -> Core.bundle.format("bar.drillspeed", Strings.fixed(e.lastDrillSpeed * 60 * e.timeScale(), 2)), () -> Pal.ammo, () -> e.warmup));
    }

    public Item getDrop(Tile tile) {
        return tile.drop();
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation) {
        if (isMultiblock()) {
            for (var other : tile.getLinkedTilesAs(this, tempTiles)) {
                if (canMine(other)) {
                    return true;
                }
            }
            return false;
        } else {
            return canMine(tile);
        }
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        Tile tile = world.tile(x, y);
        if (tile == null)
            return;
        countOre(tile);
        if (returnItem != null) {
            float width = drawPlaceText(Core.bundle.formatFloat("bar.drillspeed", 60f / (drillTime + hardnessDrillMultiplier * returnItem.hardness) * returnCount, 2), x, y, valid);
            float dx = x * tilesize + offset - width / 2f - 4f, dy = y * tilesize + offset + size * tilesize / 2f + 5, s = iconSmall / 4f;
            Draw.mixcol(Color.darkGray, 1f);
            Draw.rect(returnItem.fullIcon, dx, dy - 1, s, s);
            Draw.reset();
            Draw.rect(returnItem.fullIcon, dx, dy, s, s);
            if (drawMineItem) {
                Draw.color(returnItem.color);
                Draw.rect(itemRegion, tile.worldx() + offset, tile.worldy() + offset);
                Draw.color();
            }
        } else {
            Tile to = tile.getLinkedTilesAs(this, tempTiles).find(t -> t.drop() != null && (t.drop().hardness > tier || t.drop() == blockedItem));
            Item item = to == null ? null : to.drop();
            if (item != null) {
                drawPlaceText(Core.bundle.get("bar.drilltierreq"), x, y, valid);
            }
        }
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.drillTier, StatValues.blocks(b -> b instanceof Floor f && !f.wallOre && f.itemDrop != null && f.itemDrop.hardness <= tier && f.itemDrop != blockedItem));
        stats.add(Stat.drillSpeed, 60f / drillTime * size * size, StatUnit.itemsSecond);
        if (liquidBoostIntensity != 1) {
            stats.add(Stat.boostEffect, liquidBoostIntensity * liquidBoostIntensity, StatUnit.timesSpeed);
        }
    }

    @Override
    public TextureRegion[] icons() {
        return new TextureRegion[] { region, rotatorRegion, topRegion };
    }

    protected void countOre(Tile tile) {
        returnItem = null;
        returnCount = 0;
        oreCount.clear();
        itemArray.clear();
        for (var other : tile.getLinkedTilesAs(this, tempTiles)) {
            if (canMine(other)) {
                oreCount.increment(getDrop(other), 0, 1);
            }
        }
        for (var item : oreCount.keys()) {
            itemArray.add(item);
        }
        itemArray.sort((item1, item2) -> {
            int type = Boolean.compare(!item1.lowPriority, !item2.lowPriority);
            if (type != 0)
                return type;
            int amounts = Integer.compare(oreCount.get(item1, 0), oreCount.get(item2, 0));
            if (amounts != 0)
                return amounts;
            return Integer.compare(item1.id, item2.id);
        });
        if (itemArray.size == 0) {
            return;
        }
        returnItem = itemArray.peek();
        returnCount = oreCount.get(itemArray.peek(), 0);
    }

    public boolean canMine(Tile tile) {
        if (tile == null || tile.block().isStatic())
            return false;
        Item drops = tile.drop();
        return drops != null && drops.hardness <= tier && drops != blockedItem;
    }

    public class GasDrillBuild extends GasBuilding {

        public float progress;

        public float warmup;

        public float timeDrilled;

        public float lastDrillSpeed;

        public int dominantItems;

        public Item dominantItem;

        @Override
        public boolean shouldConsume() {
            return items.total() < itemCapacity && enabled;
        }

        @Override
        public boolean shouldAmbientSound() {
            return efficiency > 0.01f && items.total() < itemCapacity;
        }

        @Override
        public float ambientVolume() {
            return efficiency * (size * size) / 4f;
        }

        @Override
        public void drawSelect() {
            if (dominantItem != null) {
                float dx = x - size * tilesize / 2f, dy = y + size * tilesize / 2f, s = iconSmall / 4f;
                Draw.mixcol(Color.darkGray, 1f);
                Draw.rect(dominantItem.fullIcon, dx, dy - 1, s, s);
                Draw.reset();
                Draw.rect(dominantItem.fullIcon, dx, dy, s, s);
            }
        }

        @Override
        public void pickedUp() {
            dominantItem = null;
        }

        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
            countOre(tile);
            dominantItem = returnItem;
            dominantItems = returnCount;
        }

        @Override
        public void updateTile() {
            if (dominantItem == null) {
                return;
            }
            if (timer(timerDump, dumpTime)) {
                dump(items.has(dominantItem) ? dominantItem : null);
            }
            timeDrilled += warmup * delta();
            if (items.total() < itemCapacity && dominantItems > 0 && efficiency > 0) {
                float speed = Mathf.lerp(1f, liquidBoostIntensity, optionalEfficiency) * efficiency;
                lastDrillSpeed = (speed * dominantItems * warmup) / (drillTime + hardnessDrillMultiplier * dominantItem.hardness);
                warmup = Mathf.approachDelta(warmup, speed, warmupSpeed);
                progress += delta() * dominantItems * speed * warmup;
                if (Mathf.chanceDelta(updateEffectChance * warmup))
                    updateEffect.at(x + Mathf.range(size * 2f), y + Mathf.range(size * 2f));
            } else {
                lastDrillSpeed = 0f;
                warmup = Mathf.approachDelta(warmup, 0f, warmupSpeed);
                return;
            }
            float delay = drillTime + hardnessDrillMultiplier * dominantItem.hardness;
            if (dominantItems > 0 && progress >= delay && items.total() < itemCapacity) {
                offload(dominantItem);
                progress %= delay;
                if (wasVisible)
                    drillEffect.at(x + Mathf.range(drillEffectRnd), y + Mathf.range(drillEffectRnd), dominantItem.color);
            }
        }

        @Override
        public double sense(LAccess sensor) {
            if (sensor == LAccess.progress && dominantItem != null)
                return Mathf.clamp(progress / (drillTime + hardnessDrillMultiplier * dominantItem.hardness));
            return super.sense(sensor);
        }

        @Override
        public void drawCracks() {
        }

        public void drawDefaultCracks() {
            super.drawCracks();
        }

        @Override
        public void draw() {
            float s = 0.3f;
            float ts = 0.6f;
            Draw.rect(region, x, y);
            Draw.z(Layer.blockCracks);
            drawDefaultCracks();
            Draw.z(Layer.blockAfterCracks);
            if (drawRim) {
                Draw.color(heatColor);
                Draw.alpha(warmup * ts * (1f - s + Mathf.absin(Time.time, 3f, s)));
                Draw.blend(Blending.additive);
                Draw.rect(rimRegion, x, y);
                Draw.blend();
                Draw.color();
            }
            if (drawSpinSprite) {
                Drawf.spinSprite(rotatorRegion, x, y, timeDrilled * rotateSpeed);
            } else {
                Draw.rect(rotatorRegion, x, y, timeDrilled * rotateSpeed);
            }
            Draw.rect(topRegion, x, y);
            if (dominantItem != null && drawMineItem) {
                Draw.color(dominantItem.color);
                Draw.rect(itemRegion, x, y);
                Draw.color();
            }
        }

        @Override
        public byte version() {
            return 1;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(progress);
            write.f(warmup);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            if (revision >= 1) {
                progress = read.f();
                warmup = read.f();
            }
        }
    }
}
