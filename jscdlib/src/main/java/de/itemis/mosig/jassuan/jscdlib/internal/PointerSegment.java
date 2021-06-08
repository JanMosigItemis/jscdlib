package de.itemis.mosig.jassuan.jscdlib.internal;

import static java.util.Objects.requireNonNull;

import java.nio.ByteOrder;

import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayouts;
import jdk.incubator.foreign.MemorySegment;

public abstract class PointerSegment<T> extends MemorySegmentDelegate implements Dereferenceable<T> {

    private static final ByteOrder BYTE_ORDER = ByteOrder.nativeOrder();

    public PointerSegment() {
        super(MemorySegment.allocateNative(MemoryLayouts.ADDRESS));
    }

    public final void pointTo(long addr) {
        getSegment().asByteBuffer().order(BYTE_ORDER).putLong(addr);

    }

    public final void pointTo(MemoryAddress addr) {
        requireNonNull(addr, "addr");
        long longAddr = addr.toRawLongValue();
        pointTo(longAddr);
    }

    public final MemoryAddress getContainedAddress() {
        return MemoryAddress.ofLong(getSegment().asByteBuffer().order(BYTE_ORDER).getLong());
    }
}
