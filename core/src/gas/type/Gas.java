package gas.type;

import arc.graphics.Color;
import arc.struct.Seq;
import gas.content.Gasses;
import gas.core.GasContentLoader;
import mindustry.ctype.ContentType;
import mindustry.ctype.UnlockableContent;
import mindustry.type.StatusEffect;
import mindustry.world.meta.Stat;

public class Gas  extends UnlockableContent {
    public Color color;
    public float explosiveness;
    public float flammability;
    public float radioactivity;
    public Color barColor;
    public float temperature;
    public StatusEffect effect;
    public Color lightColor=color=Color.black;
    public float viscosity=0.1f;
    public float transparency=1f;
    private final Seq<Gas> sameNaming=new Seq<>();
    private final String realName;
    public Seq<Gas> sameNaming() {
        return sameNaming.copy();
    }

    public Gas(String name) {
        super(name);
        realName=name;
        for (Gas other : Gasses.all()) {
            if (other.realName.equals(realName)){
                other.sameNaming.add(this);
                sameNaming.add(other);
            }
        }
    }

    @Override
    public void init() {
        super.init();
        if (color==null && barColor!=null)color=barColor;
        if (color==null && lightColor!=null)color=lightColor;
        if (color==null)color=Color.black.cpy();
        if (lightColor==null)lightColor=Color.black.cpy();
        if (barColor==null)barColor=color;
    }

    @Override
    public ContentType getContentType() {
        return Gasses.gasType();
    }
    public void setStats() {
        this.stats.addPercent(Stat.explosiveness, this.explosiveness);
        this.stats.addPercent(Stat.flammability, this.flammability);
        this.stats.addPercent(Stat.radioactivity, this.radioactivity);
        this.stats.addPercent(Stat.viscosity, this.viscosity);
    }

    public Color barColor() {
        return this.barColor == null ? this.color : this.barColor;
    }
}
