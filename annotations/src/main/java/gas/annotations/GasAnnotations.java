package gas.annotations;

import gas.annotations.GasAnnotations;
import mindustry.annotations.Annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class GasAnnotations {
    @Retention(RetentionPolicy.SOURCE)
    public @interface GasAddition {
        String analogue() default "\n";
        String description() default "\n";
    }

}
