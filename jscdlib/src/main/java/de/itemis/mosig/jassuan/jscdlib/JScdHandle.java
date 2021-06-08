package de.itemis.mosig.jassuan.jscdlib;

import static de.itemis.mosig.jassuan.jscdlib.JAssuanNative.SCARD_ALL_READERS;
import static de.itemis.mosig.jassuan.jscdlib.JAssuanNative.SCARD_AUTOALLOCATE;
import static java.nio.ByteOrder.nativeOrder;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static jdk.incubator.foreign.CLinker.toJavaStringRestricted;
import static jdk.incubator.foreign.MemoryAddress.ofLong;
import static jdk.incubator.foreign.MemoryLayouts.ADDRESS;
import static jdk.incubator.foreign.MemoryLayouts.BITS_32_LE;
import static jdk.incubator.foreign.MemorySegment.allocateNative;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * Instances of this class act as an entry point for all available JScdLib functionality.
 * </p>
 * <p>
 * The handle has the following purposes:
 * <ul>
 * <li>Encapsulate resource (de)allocation in the background.</li>
 * <li>Force client code to explicitly specify dependencies to instances of this class, rather than
 * hiding dependencies by using static library methods, i. e.
 *
 * <pre>
 * // good
 * public class JscdClient {
 *     private final JScdHandle handle;
 *
 *     public JScdClient(JScdHandle handle){
 *     this.handle=handle;
 *     }
 *
 *     public void doSomething() {
 *         handle.someFunctionality();
 *     }
 * }
 *
 *
 * // bad
 * public class JscdClient {
 *     public void doSomething() {
 *         JscdLib.someFunctionality();
 *     }
 * }
 * </pre>
 *
 * </li>
 * </ul>
 * </p>
 */
public final class JScdHandle implements AutoCloseable {

    private final JAssuanNative nativeBridge;

    public JScdHandle(JAssuanNative nativeBridge) {
        this.nativeBridge = requireNonNull(nativeBridge, "nativeBridge");
    }

    public List<String> listReaders() {
        List<String> result = new ArrayList<>();
        try (var cardCtxSegPtr = allocateNative(ADDRESS);
                var readerListSeg = allocateNative(ADDRESS);
                var readerListLengthSeg = allocateNative(BITS_32_LE)) {
            readerListLengthSeg.asByteBuffer().putInt(SCARD_AUTOALLOCATE);
            nativeBridge.sCardListReadersA(cardCtxSegPtr.address(), SCARD_ALL_READERS, readerListSeg.address(), readerListLengthSeg.address());
            var addr = readerListSeg.asByteBuffer().order(nativeOrder()).getLong();
            result.add(toJavaStringRestricted(ofLong(addr), UTF_8));
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public void close() throws Exception {

    }
}
