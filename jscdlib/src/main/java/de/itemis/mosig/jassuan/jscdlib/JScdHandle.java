package de.itemis.mosig.jassuan.jscdlib;

import static de.itemis.mosig.jassuan.jscdlib.JAssuanNative.PCSC_SCOPE_SYSTEM;
import static de.itemis.mosig.jassuan.jscdlib.JAssuanNative.SCARD_ALL_READERS;
import static de.itemis.mosig.jassuan.jscdlib.JAssuanNative.SCARD_AUTOALLOCATE;
import static java.util.Objects.requireNonNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.itemis.mosig.jassuan.jscdlib.internal.IntSegment;
import de.itemis.mosig.jassuan.jscdlib.internal.LongPointerSegment;
import de.itemis.mosig.jassuan.jscdlib.internal.StringPointerSegment;
import jdk.incubator.foreign.MemoryAddress;

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
        LongPointerSegment ctxPtrSeg = null;
        StringPointerSegment readerListPtrSeg = null;
        MemoryAddress ptrToFirstEntryInReaderList = null;
        try (var readerListLength = new IntSegment()) {
            ctxPtrSeg = new LongPointerSegment();
            readerListPtrSeg = new StringPointerSegment();
            readerListLength.setValue(SCARD_AUTOALLOCATE);
            nativeBridge.sCardEstablishContext(PCSC_SCOPE_SYSTEM, MemoryAddress.NULL, MemoryAddress.NULL, ctxPtrSeg.address());
            nativeBridge.sCardListReadersA(ctxPtrSeg.getContainedAddress(), SCARD_ALL_READERS, readerListPtrSeg.address(), readerListLength.address());
            ptrToFirstEntryInReaderList = readerListPtrSeg.getContainedAddress();
            final int TRAILING_NULL = 1;
            var remainingLength = readerListLength.getValue() - TRAILING_NULL;
            while (remainingLength > 0) {
                String currentReader = readerListPtrSeg.dereference();
                result.add(currentReader);
                var nextOffset = currentReader.getBytes(StandardCharsets.UTF_8).length + TRAILING_NULL;
                readerListPtrSeg.pointTo(readerListPtrSeg.getContainedAddress().addOffset(nextOffset));
                remainingLength -= nextOffset;
            }
        } finally {
            nativeBridge.sCardFreeMemory(ctxPtrSeg.getContainedAddress(), ptrToFirstEntryInReaderList);
            readerListPtrSeg.close();
            nativeBridge.sCardReleaseContext(ctxPtrSeg.getContainedAddress());
            ctxPtrSeg.close();
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public void close() throws Exception {

    }
}
