package gas.type;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import gas.content.*;
import gas.entities.*;
import gas.gen.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.meta.*;


public class Gas extends UnlockableContent{
    protected static final Rand rand = new Rand();
    private final Seq<Gas> sameNaming = new Seq<>();
    private final String realName;
    public Color color;
    public float explosiveness;
    public float flammability;
    public float radioactivity;
    public Color barColor;
    public float temperature;
    public StatusEffect effect;
    public Color lightColor = color = Color.black;
    public float viscosity = 0.1f;
    public float transparency = 1f;
    public float condensePoint = -1;

    public Gas(String name, Color color){
        super(name);
        this.localizedName = Core.bundle.get("gas." + this.name + ".name", this.name);
        this.description = Core.bundle.get("gas." + this.name + ".description", description);
        this.details = Core.bundle.get("gas." + this.name + ".details", details);
        realName = name;
        for(Gas other : Gasses.all()){
            if(other.realName.equals(realName)){
                other.sameNaming.add(this);
                sameNaming.add(other);
            }
        }
        this.color = new Color(color);
    }

    public Gas(String name){
        this(name, Color.black);
    }

    public Seq<Gas> sameNaming(){
        return sameNaming.copy();
    }

    @Override
    public void init(){
        super.init();
        if(color == null && barColor != null) color = barColor;
        if(color == null && lightColor != null) color = lightColor;
        if(color == null) color = Color.black.cpy();
        if(lightColor == null) lightColor = Color.black.cpy();
        if(barColor == null) barColor = color;
    }

    @Override
    public ContentType getContentType(){
        return Gasses.gasType();
    }

    public void setStats(){
        this.stats.addPercent(Stat.explosiveness, this.explosiveness);
        this.stats.addPercent(Stat.flammability, this.flammability);
        this.stats.addPercent(Stat.radioactivity, this.radioactivity);
        this.stats.addPercent(Stat.viscosity, this.viscosity);
    }

    /** @return true if this gas will condense in this global environment. */
    public boolean willCondense(){
        return Attribute.heat.env() <= condensePoint;
    }

    public void drawCloud(Cloud cloud){
        if(willCondense()){
            drawLiquid(cloud);
        }else{
            drawGas(cloud);
        }
    }

    protected void drawGas(Cloud cloud){
        Draw.color(color);
        float amount = cloud.amount/4f+1f;
        int size = (int)amount;
        float lastProgress = amount % 1f;
        float lifetime = Fx.vapor.lifetime;
        for(int i = 0; i < size; i++){
            float time = (cloud.lifeTime + i * lifetime/10f /*/ size*2f*/) % lifetime;

            float fin = time / lifetime;
            float fout = 1f - fin;
            float finpow = Interp.pow3Out.apply(fin);
            Draw.color(fin, fin, fin);
            if(i == size - 1){
                Draw.color(color, fout*lastProgress);
            }else{
                Draw.color(color, fout);
            }

            Angles.randLenVectors(cloud.id + i, 3, 2f + finpow * 11f, (x, y) -> {
                Fill.circle(cloud.x + x, cloud.y + y, 0.6f + fin * 5f);
            });
        }
    }

    protected void drawLiquid(Cloud cloud){
        float amount = cloud.amount, x = cloud.x, y = cloud.y;
        float f = Mathf.clamp(amount / (Clouds.maxGas / 1.5f));
        float smag = cloud.tile.floor().isLiquid ? 0.8f : 0f, sscl = 25f;

        Draw.color(Tmp.c1.set(color).shiftValue(-0.05f));
        Fill.circle(x + Mathf.sin(Time.time + id * 532, sscl, smag), y + Mathf.sin(Time.time + id * 53, sscl, smag), f * 8f);

        float length = f * 6f;
        rand.setSeed(id);
        for(int i = 0; i < 3; i++){
            Tmp.v1.trns(rand.random(360f), rand.random(length));
            float vx = x + Tmp.v1.x, vy = y + Tmp.v1.y;

            Fill.circle(
            vx + Mathf.sin(Time.time + i * 532, sscl, smag),
            vy + Mathf.sin(Time.time + i * 53, sscl, smag),
            f * 5f);
        }

        Draw.color();

        if(lightColor.a > 0.001f && f > 0){
            Drawf.light(x, y, 30f * f, lightColor, color.a * f * 0.8f);
        }
    }

    @Override
    public void loadIcon(){
        super.loadIcon();
        fullIcon =
        Core.atlas.find("gas-" + name + "-full",
        Core.atlas.find(name + "-full",
        Core.atlas.find(name,
        Core.atlas.find("gas-" + name,
        Core.atlas.find(name + "1",
        fullIcon)))));

        uiIcon = Core.atlas.find("gas-" + name + "-ui", uiIcon);
    }
    public float animationScale=Liquid.animationScaleGas;
    public int getAnimationFrame(){
        return (int)(Time.time / animationScale * Liquid.animationFrames + id*5) % Liquid.animationFrames;
    }

    public Color barColor(){
        return this.barColor == null ? this.color : this.barColor;
    }
}
