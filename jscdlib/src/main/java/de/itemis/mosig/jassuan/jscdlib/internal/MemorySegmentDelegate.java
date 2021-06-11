package de.itemis.mosig.jassuan.jscdlib.internal;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.lang.ref.Cleaner;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Spliterator;

import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.NativeScope;
import jdk.incubator.foreign.SequenceLayout;

public class MemorySegmentDelegate implements MemorySegment {
    static final ByteOrder BYTE_ORDER = ByteOrder.nativeOrder();

    private MemorySegment segment;

    public MemorySegmentDelegate(MemoryAddress addr, long byteSize) {
        requireNonNull(addr, "addr");
        checkArgument(addr != MemoryAddress.NULL, "Instances of this class do not work with the NULL address.");
        checkArgument(byteSize > 0, "byteSize must be greater than 0");
        this.segment = addr.asSegmentRestricted(byteSize);
    }

    public MemorySegmentDelegate(MemorySegment segment) {
        this.segment = requireNonNull(segment, "segment");
    }

    final ByteBuffer getBuf() {
        return getSegment().asByteBuffer().order(BYTE_ORDER);
    }

    final void setSegment(MemorySegment newSegment) {
        this.segment = newSegment;
    }

    final MemorySegment getSegment() {
        return segment;
    }

    @Override
    public void close() {
        segment.close();
    }

    @Override
    public final MemoryAddress address() {
        return segment.address();
    }

    @Override
    public final Spliterator<MemorySegment> spliterator(SequenceLayout layout) {
        return segment.spliterator(layout);
    }

    @Override
    public final Thread ownerThread() {
        return segment.ownerThread();
    }

    @Override
    public final long byteSize() {
        return segment.byteSize();
    }

    @Override
    public final MemorySegment withAccessModes(int accessModes) {
        return segment.withAccessModes(accessModes);
    }

    @Override
    public final boolean hasAccessModes(int accessModes) {
        return segment.hasAccessModes(accessModes);
    }

    @Override
    public final int accessModes() {
        return segment.accessModes();
    }

    @Override
    public final MemorySegment asSlice(long offset, long newSize) {
        return segment.asSlice(offset, newSize);
    }

    @Override
    public final boolean isMapped() {
        return segment.isMapped();
    }

    @Override
    public final boolean isAlive() {
        return segment.isAlive();
    }

    @Override
    public final MemorySegment handoff(Thread thread) {
        return segment.handoff(thread);
    }

    @Override
    public final MemorySegment handoff(NativeScope nativeScope) {
        return segment.handoff(nativeScope);
    }

    @Override
    public final MemorySegment share() {
        return segment.share();
    }

    @Override
    public final MemorySegment registerCleaner(Cleaner cleaner) {
        return segment.registerCleaner(cleaner);
    }

    @Override
    public final MemorySegment fill(byte value) {
        return segment.fill(value);
    }

    @Override
    public final void copyFrom(MemorySegment src) {
        segment.copyFrom(src);
    }

    @Override
    public final long mismatch(MemorySegment other) {
        return segment.mismatch(other);
    }

    @Override
    public final ByteBuffer asByteBuffer() {
        return segment.asByteBuffer();
    }

    @Override
    public final byte[] toByteArray() {
        return segment.toByteArray();
    }

    @Override
    public final short[] toShortArray() {
        return segment.toShortArray();
    }

    @Override
    public final char[] toCharArray() {
        return segment.toCharArray();
    }

    @Override
    public final int[] toIntArray() {
        return segment.toIntArray();
    }

    @Override
    public final float[] toFloatArray() {
        return segment.toFloatArray();
    }

    @Override
    public final long[] toLongArray() {
        return segment.toLongArray();
    }

    @Override
    public final double[] toDoubleArray() {
        return segment.toDoubleArray();
    }
}
