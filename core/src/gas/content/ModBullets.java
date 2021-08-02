package gas.content;

import gas.entities.bullets.GasBulletType;
import mindustry.ctype.ContentList;
import mindustry.entities.bullet.BulletType;

public class ModBullets implements ContentList {
    public static BulletType
    //gas
    heavyMethaneShot;

    @Override
    public void load() {

        heavyMethaneShot = new GasBulletType(Gasses.methane) {{
            lifetime = 49f;
            speed = 4f;
            puddleSize = 8f;
            orbSize = 4f;
            drag = 0.001f;
            ammoMultiplier = 0.4f;
            statusDuration = 60f * 4f;
        }};
    }
}
