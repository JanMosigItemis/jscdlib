package com.itemis.jscdlib.internal.memory;

import static java.util.Objects.requireNonNull;

import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayouts;
import jdk.incubator.foreign.MemorySegment;

public class LongSegment extends MemorySegmentDelegate implements ValueSegment<Long> {

    private static final long DEFAULT_VALUE = -1L;

    public LongSegment() {
        super(MemorySegment.allocateNative(MemoryLayouts.JAVA_LONG));
        setValue(DEFAULT_VALUE);
    }

    public LongSegment(MemoryAddress addrOfInitialValueSeg) {
        super(addrOfInitialValueSeg, MemoryLayouts.JAVA_LONG.byteSize());
    }

    public LongSegment(MemorySegment initialValueSeg) {
        super(initialValueSeg);
    }

    public LongSegment(long initialValue) {
        this();
        setValue(initialValue);
    }

    @Override
    public final Long getValue() {
        return getBuf().getLong();
    }

    @Override
    public final Long setValue(Long newValue) {
        requireNonNull(newValue, "newValue");
        var oldVal = getBuf().getLong();
        getBuf().putLong(newValue);
        return oldVal;
    }
}
