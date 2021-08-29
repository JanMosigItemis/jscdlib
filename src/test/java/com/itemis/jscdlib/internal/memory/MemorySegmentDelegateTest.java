package com.itemis.jscdlib.internal.memory;

import static jdk.incubator.foreign.MemoryAddress.ofLong;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.ref.Cleaner;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.NativeScope;
import jdk.incubator.foreign.SequenceLayout;

public class MemorySegmentDelegateTest {

    private static final byte[] EXPECTED_CONTENTS = new byte[] {1, 2, 3};
    private static final long EXPECTED_BYTE_SIZE = EXPECTED_CONTENTS.length;

    private MemorySegment origSeg;
    private MemorySegmentDelegate underTest;

    @BeforeEach
    public void setUp() {
        origSeg = Mockito.mock(MemorySegment.class);

        underTest = new MemorySegmentDelegate(origSeg);
    }

    @Test
    public void test_segment_is_created_from_address() {

        try (var seg = MemorySegment.allocateNative(EXPECTED_BYTE_SIZE)) {
            seg.asByteBuffer().put(EXPECTED_CONTENTS);
            var underTest = new MemorySegmentDelegate(seg.address(), EXPECTED_BYTE_SIZE);

            assertThat(underTest.address()).as("segment objects must have same address").isEqualTo(seg.address());
            assertThat(underTest.getBuf()).as("segment objects must hold the same value").isEqualByComparingTo(ByteBuffer.wrap(EXPECTED_CONTENTS));
        }
    }

    @Test
    public void constructor_does_not_accept_null_addr() {
        assertThatThrownBy(() -> new MemorySegmentDelegate(null, EXPECTED_BYTE_SIZE)).isInstanceOf(NullPointerException.class).hasMessage("addr");
    }

    @Test
    public void constructor_does_not_accept_zero_byteSize() {
        assertThatThrownBy(() -> new MemorySegmentDelegate(ofLong(EXPECTED_BYTE_SIZE), 0)).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("byteSize");
    }

    @Test
    public void constructor_does_not_accept_negative_byteSize() {
        assertThatThrownBy(() -> new MemorySegmentDelegate(ofLong(EXPECTED_BYTE_SIZE), -1)).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("byteSize");
    }

    @Test
    public void constructor_does_not_accept_null_addr_obj() {
        assertThatThrownBy(() -> new MemorySegmentDelegate(MemoryAddress.NULL, EXPECTED_BYTE_SIZE)).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("NULL");
    }

    @Test
    public void constructor_does_not_accept_null_segment() {
        assertThatThrownBy(() -> new MemorySegmentDelegate(null)).isInstanceOf(NullPointerException.class).hasMessage("segment");
    }

    @Test
    public void test_getBuf() {
        var expectedBuf = ByteBuffer.wrap(new byte[] {1, 2, 3}).order(ByteOrder.nativeOrder());
        when(origSeg.asByteBuffer()).thenReturn(expectedBuf);
        ByteBuffer actualBuf = underTest.getBuf();
        assertThat(actualBuf).isEqualByComparingTo(expectedBuf);
        assertThat(actualBuf.order()).isSameAs(ByteOrder.nativeOrder());
    }

    @Test
    public void test_getSegment() {
        assertThat(underTest.getSegment()).isSameAs(origSeg);
    }

    @Test
    public void test_close() {
        underTest.close();
        verify(origSeg, times(1)).close();
    }

    @Test
    public void test_address() {
        underTest.address();
        verify(origSeg, times(1)).address();
    }

    @Test
    public void test_spliterator() {
        var arg = mock(SequenceLayout.class);
        underTest.spliterator(arg);
        verify(origSeg, times(1)).spliterator(arg);
    }

    @Test
    public void test_ownerThread() {
        underTest.ownerThread();
        verify(origSeg, times(1)).ownerThread();
    }

    @Test
    public void test_byteSize() {
        underTest.byteSize();
        verify(origSeg, times(1)).byteSize();
    }

    @Test
    public void test_withAccessModes() {
        var arg = 123;
        underTest.withAccessModes(arg);
        verify(origSeg, times(1)).withAccessModes(arg);
    }

    @Test
    public void test_hasAccessModes() {
        var arg = 123;
        underTest.hasAccessModes(arg);
        verify(origSeg, times(1)).hasAccessModes(arg);
    }

    @Test
    public void test_accessModes() {
        underTest.accessModes();
        verify(origSeg, times(1)).accessModes();
    }

    @Test
    public void test_asSlice() {
        var arg = 123;
        var otherArg = 321;
        underTest.asSlice(arg, otherArg);
        verify(origSeg, times(1)).asSlice(arg, otherArg);
    }

    @Test
    public void test_isMapped() {
        underTest.isMapped();
        verify(origSeg, times(1)).isMapped();
    }

    @Test
    public void test_isAlive() {
        underTest.isAlive();
        verify(origSeg, times(1)).isAlive();
    }

    @Test
    public void test_handoff_thread() {
        var arg = mock(Thread.class);
        underTest.handoff(arg);
        verify(origSeg, times(1)).handoff(arg);
    }

    @Test
    public void test_handoff_scope() {
        var arg = mock(NativeScope.class);
        underTest.handoff(arg);
        verify(origSeg, times(1)).handoff(arg);
    }

    @Test
    public void test_share() {
        underTest.share();
        verify(origSeg, times(1)).share();
    }

    @Test
    public void test_registerCleaner() {
        var arg = mock(Cleaner.class);
        underTest.registerCleaner(arg);
        verify(origSeg, times(1)).registerCleaner(arg);
    }

    @Test
    public void test_fill() {
        byte arg = 123;
        underTest.fill(arg);
        verify(origSeg, times(1)).fill(arg);
    }

    @Test
    public void test_copyFrom() {
        var arg = mock(MemorySegment.class);
        underTest.copyFrom(arg);
        verify(origSeg, times(1)).copyFrom(arg);
    }

    @Test
    public void test_mismatch() {
        var arg = mock(MemorySegment.class);
        underTest.mismatch(arg);
        verify(origSeg, times(1)).mismatch(arg);
    }

    @Test
    public void test_asByteBuffer() {
        underTest.asByteBuffer();
        verify(origSeg, times(1)).asByteBuffer();
    }

    @Test
    public void test_toByteArray() {
        underTest.toByteArray();
        verify(origSeg, times(1)).toByteArray();
    }

    @Test
    public void test_toShortArray() {
        underTest.toShortArray();
        verify(origSeg, times(1)).toShortArray();
    }

    @Test
    public void test_toCharArray() {
        underTest.toCharArray();
        verify(origSeg, times(1)).toCharArray();
    }

    @Test
    public void test_toIntArray() {
        underTest.toIntArray();
        verify(origSeg, times(1)).toIntArray();
    }

    @Test
    public void test_toFloatArray() {
        underTest.toFloatArray();
        verify(origSeg, times(1)).toFloatArray();
    }

    @Test
    public void test_toLongArray() {
        underTest.toLongArray();
        verify(origSeg, times(1)).toLongArray();
    }

    @Test
    public void test_toDoubleArray() {
        underTest.toDoubleArray();
        verify(origSeg, times(1)).toDoubleArray();
    }
}
