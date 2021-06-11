package de.itemis.mosig.jassuan.jscdlib.internal;

import static java.util.Objects.requireNonNull;

import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;

public abstract class PointerSegment<T> extends LongSegment implements Dereferenceable<T> {

    public PointerSegment() {
        super(new LongSegment());
    }

    public PointerSegment(MemoryAddress addr) {
        super(requireNonNull(addr, "addr"));
    }

    public final MemoryAddress getContainedAddress() {
        return MemoryAddress.ofLong(getValue());
    }

    public void pointTo(MemoryAddress addr) {
        requireNonNull(addr, "addr");
        setValue(addr.toRawLongValue());
    }

    public void pointTo(MemorySegment seg) {
        requireNonNull(seg, "seg");
        setValue(seg.address().toRawLongValue());
    }
}
