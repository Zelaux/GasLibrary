package gas.annotations.remote;

import arc.struct.Seq;
import arc.util.Log;
import arc.util.Strings;
import gas.annotations.GasAnnotations.Remote;
import gas.annotations.GasBaseProcessor;
import mindustry.annotations.Annotations.Loc;
import mindustry.annotations.BaseProcessor;
import mindustry.annotations.util.Selement;
import mindustry.annotations.util.Smethod;
import mindustry.annotations.util.TypeIOResolver.ClassSerializer;
import gas.annotations.remote.*;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Modifier;


/**
 * The annotation processor for generating remote method call code.
 */
@SupportedAnnotationTypes({
        "gas.annotations.GasAnnotations.Remote",
//        "mindustry.annotations.Annotations.Remote",
        "gas.annotations.GasAnnotations.TypeIOHandler",
        "mindustry.annotations.Annotations.TypeIOHandler"
})
public class GasRemoteProcess extends GasBaseProcessor {
    /**
     * Simple class name of generated class name.
     */
    public static final String callLocation = "GasCall";

    @Override
    public void process(RoundEnvironment roundEnv) throws Exception {
        Log.info(getClass().getSimpleName() + ".work(" + round + ")");
        //get serializers
        //class serializers
        ClassSerializer serializer = ModTypeIOResolver.resolve(this);
        //last method ID used
        int lastMethodID = 0;
        //find all elements with the Remote annotation
        //all elements with the Remote annotation
        Seq<Smethod> elements = methods(Remote.class);
        //list of all method entries
        Seq<MethodEntry> methods = new Seq<>();

        Seq<Smethod> orderedElements = elements.copy();
        orderedElements.sortComparing(Selement::toString);

        //create methods
        for (Smethod element : orderedElements) {
            Remote annotation = element.annotation(Remote.class);

            //check for static
            if (!element.is(Modifier.STATIC) || !element.is(Modifier.PUBLIC)) {
                err("All @Remote methods must be public and static", element);
            }

            //can't generate none methods
            if (annotation.targets() == Loc.none) {
                err("A @Remote method's targets() cannot be equal to 'none'", element);
            }

            String packetName = Strings.capitalize(element.name()) + "CallPacket";
//            System.out.println("packet: "+packetName);
            int[] index = {1};

            while (methods.contains(m -> m.packetClassName.equals(packetName + (index[0] == 1 ? "" : index[0])))) {
                index[0]++;
            }

            //create and add entry
            MethodEntry method = new MethodEntry(
                    callLocation, BaseProcessor.getMethodName(element.e), packetName + (index[0] == 1 ? "" : index[0]),
                    annotation.targets(), annotation.variants(),
                    annotation.called(), annotation.unreliable(), annotation.forward(), lastMethodID++,
                    element, annotation.priority()
            );

            methods.add(method);
        }

        //generate the methods to invoke, as well as the packet classes
        CallGenerator.generate(serializer, methods);
    }
}
