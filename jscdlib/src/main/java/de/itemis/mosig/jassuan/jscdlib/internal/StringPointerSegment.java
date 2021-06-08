package de.itemis.mosig.jassuan.jscdlib.internal;

import java.nio.charset.StandardCharsets;

import jdk.incubator.foreign.CLinker;

public class StringPointerSegment extends PointerSegment<String> {

    @Override
    public String dereference() {
        return CLinker.toJavaStringRestricted(getContainedAddress(), StandardCharsets.UTF_8);
    }
}
