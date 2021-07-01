package de.itemis.mosig.jassuan.jscdlib.internal.memory;

import java.nio.charset.StandardCharsets;

import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.MemoryAddress;

public class StringPointerSegment extends PointerSegment<String> {

    public StringPointerSegment() {
        super();
    }

    public StringPointerSegment(MemoryAddress addr) {
        super(addr);
    }

    @Override
    public String dereference() {
        return CLinker.toJavaStringRestricted(getContainedAddress(), StandardCharsets.UTF_8);
    }
}
