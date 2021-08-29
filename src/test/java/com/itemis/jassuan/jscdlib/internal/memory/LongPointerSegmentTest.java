package com.itemis.jassuan.jscdlib.internal.memory;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;

public class LongPointerSegmentTest {

    private List<MemorySegment> allocatedSegs;

    @BeforeEach
    public void setUp() {
        allocatedSegs = new ArrayList<>();
    }

    @AfterEach
    public void tearDown() {
        allocatedSegs.forEach(seg -> {
            if (seg.isAlive()) {
                seg.close();
            }
        });
    }

    @Test
    public void dereference_string_seg_usecase() {
        try (var longSeg = new LongSegment()) {
            var underTest = allocatePtrSeg();
            var expectedValue = 123L;
            longSeg.setValue(expectedValue);

            underTest.pointTo(longSeg.address());

            assertThat(underTest.dereference()).isEqualTo(expectedValue);
        }
    }

    @Test
    public void no_arg_constructor_creates_new_seg_containing_zero() {
        var underTest = allocatePtrSeg();
        assertThat(underTest.getValue()).isEqualTo(-1L);
    }

    @ParameterizedTest
    @ValueSource(longs = {Long.MIN_VALUE, Long.MAX_VALUE})
    public void dereference_works_with_long_boundaries(long expectedValue) {
        var thisSegHoldsTheValue = allocateSeg();
        thisSegHoldsTheValue.setValue(expectedValue);
        var thisSegHoldsTheAddr = allocateSeg();
        thisSegHoldsTheAddr.setValue(thisSegHoldsTheValue.address().toRawLongValue());
        var underTest = allocatePtrSeg(thisSegHoldsTheAddr.address());

        assertThat(underTest.dereference()).isEqualTo(expectedValue);
    }

    @Test
    public void test_constructor_with_addr() {
        long addr = 123L;
        MemoryAddress expectedAddr = MemoryAddress.ofLong(addr);
        var underTest = allocatePtrSeg(expectedAddr);
        assertThat(underTest.address()).isEqualTo(expectedAddr);
    }

    private LongSegment allocateSeg() {
        var result = new LongSegment();
        allocatedSegs.add(result);
        return result;
    }

    private LongPointerSegment allocatePtrSeg() {
        var result = new LongPointerSegment();
        allocatedSegs.add(result);
        return result;
    }

    private LongPointerSegment allocatePtrSeg(MemoryAddress addr) {
        var result = new LongPointerSegment(addr);
        allocatedSegs.add(result);
        return result;
    }
}
