package de.itemis.mosig.jassuan.jscdlib.internal.memory;

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

public class IntPointerSegmentTest {

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
        try (var intSeg = new IntSegment()) {
            var underTest = allocatePtrSeg();
            var expectedValue = 123;
            intSeg.setValue(expectedValue);

            underTest.pointTo(intSeg.address());

            assertThat(underTest.dereference()).isEqualTo(expectedValue);
        }
    }

    @Test
    public void no_arg_constructor_creates_new_seg_containing_zero() {
        var underTest = allocatePtrSeg();
        assertThat(underTest.getValue()).isEqualTo(-1L);
    }

    @ParameterizedTest
    @ValueSource(ints = {Integer.MIN_VALUE, Integer.MAX_VALUE})
    public void dereference_works_with_int_boundaries(int expectedValue) {
        var thisSegHoldsTheValue = allocateSeg();
        thisSegHoldsTheValue.setValue(expectedValue);
        var thisSegHoldsTheAddr = allocatePtrSeg();
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

    private IntSegment allocateSeg() {
        var result = new IntSegment();
        allocatedSegs.add(result);
        return result;
    }

    private IntPointerSegment allocatePtrSeg() {
        var result = new IntPointerSegment();
        allocatedSegs.add(result);
        return result;
    }

    private IntPointerSegment allocatePtrSeg(MemoryAddress addr) {
        var result = new IntPointerSegment(addr);
        allocatedSegs.add(result);
        return result;
    }
}
