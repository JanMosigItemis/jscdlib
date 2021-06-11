package de.itemis.mosig.jassuan.jscdlib.internal;

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

    public StringSegment(MemoryAddress addr) {
        super(addr, CLinker.toJavaStringRestricted(addr).getBytes(UTF_8).length + 1);
    }

    public StringSegment(MemorySegment segment) {
        super(segment);
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
