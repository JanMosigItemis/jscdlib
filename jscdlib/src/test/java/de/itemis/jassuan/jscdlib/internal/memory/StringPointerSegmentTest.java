package de.itemis.jassuan.jscdlib.internal.memory;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.MemoryAddress;

public class StringPointerSegmentTest {
    private StringPointerSegment underTest;

    @AfterEach
    public void tearDown() {
        if (underTest != null && underTest.isAlive()) {
            underTest.close();
        }
    }

    @Test
    public void dereference_string_seg_usecase() {
        try (var stringSeg = new StringSegment()) {
            underTest = new StringPointerSegment();
            var expectedValue = "expectedValue";
            stringSeg.setValue(expectedValue);

            underTest.pointTo(stringSeg.address());

            assertThat(underTest.dereference()).isEqualTo(expectedValue);
        }
    }

    @Test
    public void no_arg_constructor_creates_new_seg_containing_zero() {
        underTest = new StringPointerSegment();
        assertThat(underTest.getValue()).isEqualTo(-1L);
    }

    @Test
    public void dereference_returns_string_pointed_to() {
        var testString = "testString";
        try (var segHoldsStr = CLinker.toCString(testString, StandardCharsets.UTF_8); var segHoldsStrAddr = new LongSegment()) {
            segHoldsStrAddr.setValue(segHoldsStr.address().toRawLongValue());
            underTest = new StringPointerSegment(segHoldsStrAddr.address());

            assertThat(underTest.dereference()).isEqualTo(testString);
        }
    }

    @Test
    public void test_constructor_with_addr_sets_contents_correctly() {
        long addr = 123L;
        MemoryAddress expectedAddr = MemoryAddress.ofLong(addr);
        underTest = new StringPointerSegment(expectedAddr);
        assertThat(underTest.address()).isEqualTo(expectedAddr);
    }
}
