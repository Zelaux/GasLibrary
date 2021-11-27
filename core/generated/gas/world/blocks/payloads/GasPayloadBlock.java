package gas.world.blocks.payloads;

import gas.entities.comp.*;
import gas.type.*;
import gas.world.blocks.logic.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.experimental.*;
import gas.world.meta.*;
import mindustry.annotations.Annotations.*;
import gas.world.blocks.units.*;
import gas.world.blocks.defense.*;
import arc.util.*;
import mindustry.world.blocks.legacy.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import arc.math.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.meta.*;
import gas.world.blocks.distribution.*;
import gas.world.draw.*;
import mindustry.world.blocks.logic.*;
import mindustry.gen.*;
import gas.world.blocks.power.*;
import mindustry.world.*;
import gas.world.blocks.sandbox.*;
import mindustry.world.blocks.storage.*;
import gas.world.blocks.liquid.*;
import gas.entities.*;
import mindustry.world.blocks.campaign.*;
import gas.gen.*;
import gas.world.*;
import gas.world.blocks.defense.turrets.*;
import gas.world.blocks.gas.*;
import arc.math.geom.*;
import gas.world.blocks.campaign.*;
import mindustry.world.modules.*;
import gas.ui.*;
import mindustry.world.blocks.environment.*;
import gas.world.consumers.*;
import mindustry.world.blocks.payloads.*;
import mindustry.world.blocks.*;
import gas.world.blocks.production.GasGenericCrafter.*;
import arc.graphics.g2d.*;
import mindustry.world.consumers.*;
import gas.world.modules.*;
import gas.world.blocks.*;
import gas.*;
import gas.io.*;
import gas.world.blocks.payloads.*;
import mindustry.world.blocks.payloads.PayloadBlock.*;
import mindustry.world.blocks.units.*;
import gas.content.*;
import gas.world.blocks.storage.*;
import mindustry.graphics.*;
import gas.world.blocks.production.*;
import arc.util.io.*;
import mindustry.world.blocks.defense.*;
import gas.entities.bullets.*;
import gas.world.meta.values.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.sandbox.*;
import static mindustry.Vars.*;

public class GasPayloadBlock extends GasBlock {

    public float payloadSpeed = 0.7f, payloadRotateSpeed = 5f;

    @Load(value = "@-top", fallback = "factory-top-@size")
    public TextureRegion topRegion;

    @Load(value = "@-out", fallback = "factory-out-@size")
    public TextureRegion outRegion;

    @Load(value = "@-in", fallback = "factory-in-@size")
    public TextureRegion inRegion;

    public GasPayloadBlock(String name) {
        super(name);
        update = true;
        sync = true;
        group = BlockGroup.payloads;
        envEnabled |= Env.space;
    }

    public static boolean blends(Building build, int direction) {
        int size = build.block.size;
        int trns = build.block.size / 2 + 1;
        Building accept = build.nearby(Geometry.d4(direction).x * trns, Geometry.d4(direction).y * trns);
        return accept != null && accept.block.outputsPayload && // if size is the same, block must either be facing this one, or not be rotating
        ((accept.block.size == size && // check alignment
        Math.abs(accept.tileX() - build.tileX()) % size == 0 && Math.abs(accept.tileY() - build.tileY()) % size == 0 && ((accept.block.rotate && accept.tileX() + Geometry.d4(accept.rotation).x * size == build.tileX() && accept.tileY() + Geometry.d4(accept.rotation).y * size == build.tileY()) || !accept.block.rotate || !accept.block.outputFacing)) || // if the other block is smaller, check alignment
        (accept.block.size != size && (// check orientation; make sure it's aligned properly with this block.
        accept.rotation % 2 == 0 ? // check Y alignment
        Math.abs(accept.y - build.y) <= Math.abs(size * tilesize - accept.block.size * tilesize) / 2f : // check X alignment
        Math.abs(accept.x - build.x) <= Math.abs(size * tilesize - accept.block.size * tilesize) / 2f)) && // make sure it's facing this block
        (!accept.block.rotate || accept.front() == build || !accept.block.outputFacing));
    }

    public static void pushOutput(Payload payload, float progress) {
        float thresh = 0.55f;
        if (progress >= thresh) {
            boolean legStep = payload instanceof UnitPayload u && u.unit.type.allowLegStep;
            float size = payload.size(), radius = size / 2f, x = payload.x(), y = payload.y(), scl = Mathf.clamp(((progress - thresh) / (1f - thresh)) * 1.1f);
            Groups.unit.intersect(x - size / 2f, y - size / 2f, size, size, u -> {
                float dst = u.dst(payload);
                float rs = radius + u.hitSize / 2f;
                if (u.isGrounded() && u.type.allowLegStep == legStep && dst < rs) {
                    u.vel.add(Tmp.v1.set(u.x - x, u.y - y).setLength(Math.min(rs - dst, 1f)).scl(scl));
                }
            });
        }
    }

    public class GasPayloadBlockBuild<T extends Payload> extends GasBuilding {

        @Nullable
        public T payload;

        // TODO redundant; already stored in payload?
        public Vec2 payVector = new Vec2();

        public float payRotation;

        public boolean carried;

        public boolean acceptUnitPayload(Unit unit) {
            return false;
        }

        @Override
        public boolean canControlSelect(Unit player) {
            return !player.spawnedByCore && this.payload == null && acceptUnitPayload(player) && player.tileOn() != null && player.tileOn().build == this;
        }

        @Override
        public void onControlSelect(Unit player) {
            float x = player.x, y = player.y;
            handleUnitPayload(player, p -> payload = (T) p);
            this.payVector.set(x, y).sub(this).clamp(-size * tilesize / 2f, -size * tilesize / 2f, size * tilesize / 2f, size * tilesize / 2f);
            this.payRotation = player.rotation;
        }

        @Override
        public boolean acceptPayload(Building source, Payload payload) {
            return this.payload == null;
        }

        @Override
        public void handlePayload(Building source, Payload payload) {
            this.payload = (T) payload;
            this.payVector.set(source).sub(this).clamp(-size * tilesize / 2f, -size * tilesize / 2f, size * tilesize / 2f, size * tilesize / 2f);
            this.payRotation = payload.rotation();
            updatePayload();
        }

        @Override
        public Payload getPayload() {
            return payload;
        }

        @Override
        public void pickedUp() {
            carried = true;
        }

        @Override
        public void drawTeamTop() {
            carried = false;
        }

        @Override
        public Payload takePayload() {
            T t = payload;
            payload = null;
            return t;
        }

        @Override
        public void onRemoved() {
            super.onRemoved();
            if (payload != null && !carried)
                payload.dump();
        }

        public boolean blends(int direction) {
            return GasPayloadBlock.blends(this, direction);
        }

        public void updatePayload() {
            if (payload != null) {
                payload.set(x + payVector.x, y + payVector.y, payRotation);
            }
        }

        /**
         * @return true if the payload is in position.
         */
        public boolean moveInPayload() {
            return moveInPayload(true);
        }

        /**
         * @return true if the payload is in position.
         */
        public boolean moveInPayload(boolean rotate) {
            if (payload == null)
                return false;
            updatePayload();
            if (rotate) {
                payRotation = Angles.moveToward(payRotation, rotate ? rotdeg() : 90f, payloadRotateSpeed * edelta());
            }
            payVector.approach(Vec2.ZERO, payloadSpeed * delta());
            return hasArrived();
        }

        public void moveOutPayload() {
            if (payload == null)
                return;
            updatePayload();
            Vec2 dest = Tmp.v1.trns(rotdeg(), size * tilesize / 2f);
            payRotation = Angles.moveToward(payRotation, rotdeg(), payloadRotateSpeed * edelta());
            payVector.approach(dest, payloadSpeed * delta());
            Building front = front();
            boolean canDump = front == null || !front.tile().solid();
            boolean canMove = front != null && (front.block.outputsPayload || front.block.acceptsPayload);
            if (canDump && !canMove) {
                pushOutput(payload, 1f - (payVector.dst(dest) / (size * tilesize / 2f)));
            }
            if (payVector.within(dest, 0.001f)) {
                payVector.clamp(-size * tilesize / 2f, -size * tilesize / 2f, size * tilesize / 2f, size * tilesize / 2f);
                if (canMove) {
                    if (movePayload(payload)) {
                        payload = null;
                    }
                } else if (canDump) {
                    dumpPayload();
                }
            }
        }

        public void dumpPayload() {
            // translate payload forward slightly
            float tx = Angles.trnsx(payload.rotation(), 0.1f), ty = Angles.trnsy(payload.rotation(), 0.1f);
            payload.set(payload.x() + tx, payload.y() + ty, payload.rotation());
            if (payload.dump()) {
                payload = null;
            } else {
                payload.set(payload.x() - tx, payload.y() - ty, payload.rotation());
            }
        }

        public boolean hasArrived() {
            return payVector.isZero(0.01f);
        }

        public void drawPayload() {
            if (payload != null) {
                updatePayload();
                Draw.z(Layer.blockOver);
                payload.draw();
            }
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(payVector.x);
            write.f(payVector.y);
            write.f(payRotation);
            Payload.write(payload, write);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            payVector.set(read.f(), read.f());
            payRotation = read.f();
            payload = Payload.read(read);
        }
    }
}
