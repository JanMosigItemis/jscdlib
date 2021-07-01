package de.itemis.mosig.jassuan.jscdlib.internal.memory;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.LibraryLookup;

public class NativeMethodHandleTest {

    @Test
    public void test_strlen_usecase() {
        var underTest = NativeMethodHandle
            .ofLib(LibraryLookup.ofDefault())
            .returnType(long.class)
            .func("strlen")
            .args(CLinker.C_POINTER)
            .create(CLinker.getInstance());

        String testStr = "Hello World!";

        try (var strSeg = new StringSegment(testStr)) {
            assertThat(underTest.call(strSeg.address())).as("Unexpected strlen result.").isEqualTo(testStr.length());
        }
    }
}
