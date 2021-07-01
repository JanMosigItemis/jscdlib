package de.itemis.mosig.jassuan.jscdlib.internal.memory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;

public class PointerSegmentTest {

    private PointerSegment<Object> underTest;

    @AfterEach
    public void tearDown() {
        if (underTest != null && underTest.isAlive()) {
            underTest.close();
        }
    }

    @Test
    public void pointTo_addr_changes_contents() {
        var expectedAddr = MemoryAddress.ofLong(123L);
        underTest = constructSeg();
        underTest.pointTo(expectedAddr);

        assertThat(underTest.getContainedAddress()).isEqualTo(expectedAddr);
        assertThat(underTest.getValue()).isEqualTo(expectedAddr.toRawLongValue());
    }

    @Test
    public void pointTo_seg_changes_contents() {
        try (var testSeg = MemorySegment.allocateNative(3)) {
            var expectedAddr = testSeg.address();
            underTest = constructSeg();
            underTest.pointTo(testSeg);

            assertThat(underTest.getContainedAddress()).isEqualTo(expectedAddr);
            assertThat(underTest.getValue()).isEqualTo(expectedAddr.toRawLongValue());
        }
    }

    @Test
    public void no_arg_constructor_creates_new_seg_containing_zero() {
        underTest = constructSeg();
        assertThat(underTest.getValue()).isEqualTo(-1L);
    }

    @Test
    public void getContainedAddress_returns_address() {
        var expectedValue = 123L;
        try (var seg = new LongSegment()) {
            underTest = constructSeg(seg.address());
            underTest.setValue(expectedValue);
            assertThat(underTest.getContainedAddress().toRawLongValue()).isEqualTo(expectedValue);
        }
    }

    @Test
    public void constructor_with_addr_sets_addr_correctly() {
        long addr = 123L;
        MemoryAddress expectedAddr = MemoryAddress.ofLong(addr);
        underTest = constructSeg(expectedAddr);
        assertThat(underTest.address()).isEqualTo(expectedAddr);
    }

    @Test
    public void test_addr_constructor_does_not_accept_null() {
        assertThatThrownBy(() -> constructSeg(null)).isInstanceOf(NullPointerException.class).hasMessage("addr");
    }

    private PointerSegment<Object> constructSeg() {
        return new PointerSegment<Object>() {
            @Override
            public Object dereference() {
                return null;
            }
        };
    }

    private PointerSegment<Object> constructSeg(MemoryAddress addr) {
        return new PointerSegment<>(addr) {
            @Override
            public Object dereference() {
                return null;
            }
        };
    }
}
