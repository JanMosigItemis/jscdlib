package com.itemis.jassuan.jscdlib.internal.memory;

import static java.util.Objects.requireNonNull;

import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayouts;
import jdk.incubator.foreign.MemorySegment;

public class IntSegment extends MemorySegmentDelegate implements ValueSegment<Integer> {

    private static final int DEFAULT_VALUE = -1;

    public IntSegment() {
        super(MemorySegment.allocateNative(MemoryLayouts.JAVA_INT));
        setValue(DEFAULT_VALUE);
    }

    public IntSegment(MemoryAddress addr) {
        super(addr, MemoryLayouts.JAVA_INT.byteSize());
    }

    public IntSegment(MemorySegment segment) {
        super(segment);
    }

    public IntSegment(int initialValue) {
        this();
        setValue(initialValue);
    }

    @Override
    public final Integer getValue() {
        return getBuf().getInt();
    }

    @Override
    public final Integer setValue(Integer newValue) {
        requireNonNull(newValue, "newValue");
        var oldVal = getBuf().getInt();
        getBuf().putInt(newValue);
        return oldVal;
    }
}
