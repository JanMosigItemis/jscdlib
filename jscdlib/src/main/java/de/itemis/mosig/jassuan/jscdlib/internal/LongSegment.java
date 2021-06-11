package de.itemis.mosig.jassuan.jscdlib.internal;

import static java.util.Objects.requireNonNull;

import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayouts;
import jdk.incubator.foreign.MemorySegment;

public class LongSegment extends MemorySegmentDelegate implements ValueSegment<Long> {

    private static final long INITIAL_VALUE = 0L;

    public LongSegment() {
        super(MemorySegment.allocateNative(MemoryLayouts.JAVA_LONG));
        setValue(INITIAL_VALUE);
    }

    public LongSegment(MemoryAddress addr) {
        super(addr, MemoryLayouts.JAVA_LONG.byteSize());
    }

    public LongSegment(MemorySegment segment) {
        super(segment);
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
