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
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Replace{
    }

    /** Indicates that a method should be final in all implementing classes. */
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Final{
    }

    /** Indicates that a field will be interpolated when synced. */
    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SyncField{
        /** If true, the field will be linearly interpolated. If false, it will be interpolated as an angle. */
        boolean value();
        /** If true, the field is clamped to 0-1. */
        boolean clamped() default false;
    }

    /** Indicates that a field will not be read from the server when syncing the local player state. */
    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SyncLocal{

    }

    /** Indicates that a component field is imported from other components. This means it doesn't actually exist. */

    /** Indicates that a component field is read-only. */
    @Target({ElementType.FIELD, ElementType.METHOD})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ReadOnly{
    }

    /** Indicates multiple inheritance on a component type. */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.SOURCE)
    public @interface Component{
        /** Whether to generate a base class for this components.
         * An entity cannot have two base classes, so only one component can have base be true. */
        boolean base() default false;
    }

    /** Indicates that a method is implemented by the annotation processor. */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface InternalImpl{
    }

    /** Indicates priority of a method in an entity. Methods with higher priority are done last. */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface MethodPriority{
        float value();
    }

    /** Indicates that a component def is present on all entities. */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.SOURCE)
    public @interface BaseComponent{
    }

    /** Creates a group that only examines entities that have all the components listed. */
    @Retention(RetentionPolicy.SOURCE)
    public @interface GroupDef{
        Class[] value();
        boolean collide() default false;
        boolean spatial() default false;
        boolean mapping() default false;
    }

    /** Indicates an entity definition. */
//    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface EntityDef{
        /** List of component interfaces */
        Class[] value() default {};
        /** Whether the class is final */
        boolean isFinal() default true;
        /** If true, entities are recycled. */
        boolean pooled() default false;
        /** Whether to serialize (makes the serialize method return this value).
         * If true, this entity is automatically put into save files.
         * If false, no serialization code is generated at all. */
        boolean serialize() default true;
        /** Whether to generate IO code. This is for advanced usage only. */
        boolean genio() default true;
        /** Whether I made a massive mistake by merging two different class branches */
        boolean legacy() default false;
    }

    /** Indicates an internal interface for entity components. */

    //endregion
    //region misc. utility

    /** Automatically loads block regions annotated with this. */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface Load{
        /**
         * The region name to load. Variables can be used:
         * "@" -> block name
         * "$size" -> block size
         * "#" "#1" "#2" -> index number, for arrays
         * */
        String value();
        /** 1D Array length, if applicable.  */
        int length() default 1;
        /** 2D array lengths. */
        int[] lengths() default {};
        /** Fallback string used to replace "@" (the block name) if the region isn't found. */
        String fallback() default "error";
    }

    /** Indicates that a method should always call its super version. */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface CallSuper{

    }

    /** Annotation that allows overriding CallSuper annotation. To be used on method that overrides method with CallSuper annotation from parent class. */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface OverrideCallSuper{
    }
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.SOURCE)
    public @interface TypeIOHandler{
    }
    @Retention(RetentionPolicy.SOURCE)
    public @interface DefaultValue {
        String value();

        Class[] imports() default {};
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface Remote {
        /**
         * Specifies the locations from which this method can be invoked.
         */
        Annotations.Loc targets() default Annotations.Loc.server;

        /**
         * Specifies which methods are generated. Only affects server-to-client methods.
         */
        Annotations.Variant variants() default Annotations.Variant.all;

        /**
         * The local locations where this method is called locally, when invoked.
         */
        Annotations.Loc called() default Annotations.Loc.none;

        /**
         * Whether to forward this packet to all other clients upon receival. Client only.
         */
        boolean forward() default false;

        /**
         * Whether the packet for this method is sent with UDP instead of TCP.
         * UDP is faster, but is prone to packet loss and duplication.
         */
        boolean unreliable() default false;

        int replaceLevel() default -1;

        /**
         * Priority of this event.
         */
        Annotations.PacketPriority priority() default Annotations.PacketPriority.normal;

    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface EntityInterface {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface Import {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface EntitySuperClass {
    }
    @Retention(RetentionPolicy.SOURCE)
    public @interface EntitySuperInterface {
    }
    @Retention(RetentionPolicy.SOURCE)
    public @interface CashAnnotation2{}
}
