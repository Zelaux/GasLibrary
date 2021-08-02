package gas.world.modules;

import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.io.TypeIO;
import mindustry.world.blocks.payloads.Payload;
import mindustry.world.modules.BlockModule;

public class PayloadModule extends BlockModule {
    public static final PayloadModule empty = new PayloadModule();

    protected final Seq<Payload> items = new Seq<>();

    public PayloadModule copy() {
        PayloadModule out = new PayloadModule();
        out.set(this);
        return out;
    }

    public void set(PayloadModule other) {
        items.clear();
        items.addAll(other.items);
    }

    public int count() {
        return items.size;
    }

    public void each(PayloadConsumer cons) {
        for (Payload item : items) {
            cons.accept(item);
        }
    }

    public void each(PayloadConsumer2 cons) {
        for (int i = 0; i < items.size; i++) {
            cons.accept(items.get(i), i);
        }
    }

    public void update() {
        Seq<Payload> copy = items.copy();
        items.clear();
        for (Payload payload : copy) {
            if (payload != null && !items.contains(payload)) items.add(payload);
        }
    }

    public boolean has(Payload item) {
        return items.contains(item);
    }

    public boolean empty() {
        return items.isEmpty();
    }


    public boolean any() {
        return count() > 0;
    }

    @Nullable
    public Payload first() {
        if (items.size == 0) return null;
        return items.first();
    }

    @Nullable
    public Payload take() {
        Payload first = first();
        if (first != null) items.remove(first);
        return first;
    }

    /**
     * Begins a speculative take operation. This returns the item that would be returned by #take(), but does not change state.
     */
    @Nullable
    public Payload takeIndex(int index) {
        if (count() == 0 || index < 0 || index >= items.size) return null;
        Payload payload = items.get(index);
        items.remove(payload);
        return payload;
    }

    public void add(Iterable<Payload> payloads) {
        for (Payload payload : payloads) {
            add(payload);
        }
    }

    public void add(PayloadModule payloads) {
        items.addAll(payloads.items);
    }

    public void add(Payload payload) {
        items.addAll(payload);
    }


    public void remove(Payload payload) {
        items.remove(payload);
    }

    public void remove(Payload[] payloads) {
        for (Payload payload : payloads) remove(payload);
    }

    public void remove(Iterable<Payload> payloads) {
        for (Payload payload : payloads) remove(payload);
    }

    public void clear() {
        items.clear();
    }

    @Override
    public void write(Writes write) {

        write.i(items.size);
        for (Payload item : items) {
            TypeIO.writePayload(write, item);
        }
    }

    @Override
    public void read(Reads read, boolean legacy) {
        //just in case, reset items
        int size = read.i();
        items.clear();
        for (int i = 0; i < size; i++) {
            Payload payload = TypeIO.readPayload(read);
            if (payload != null) items.addAll(payload);
        }

    }

    public int total() {
        return count();
    }

    public interface PayloadConsumer {
        void accept(Payload payload);
    }

    public interface PayloadConsumer2 {
        void accept(Payload payload, int index);
    }
}

