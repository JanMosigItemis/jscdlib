package com.itemis.jscdlib.internal.memory;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;

public class StringSegment extends MemorySegmentDelegate implements ValueSegment<String> {

    private static final String INITIAL_VALUE = "";

    public StringSegment() {
        super(CLinker.toCString(INITIAL_VALUE, UTF_8));
    }

    public StringSegment(MemoryAddress addrOfInitialValueSeg) {
        super(addrOfInitialValueSeg, CLinker.toJavaStringRestricted(addrOfInitialValueSeg).getBytes(UTF_8).length + 1);
    }

    public StringSegment(MemorySegment initialValueSeg) {
        super(initialValueSeg);
    }

    public StringSegment(String initialValue) {
        super(CLinker.toCString(requireNonNull(initialValue, "initialValue"), UTF_8));
    }

    @Override
    public final String getValue() {
        return CLinker.toJavaString(getSegment(), UTF_8);
    }

    @Override
    public final String setValue(String newValue) {
        requireNonNull(newValue, "newValue");
        var oldVal = getValue();
        close();
        setSegment(CLinker.toCString(newValue, UTF_8));
        return oldVal;
    }
}
