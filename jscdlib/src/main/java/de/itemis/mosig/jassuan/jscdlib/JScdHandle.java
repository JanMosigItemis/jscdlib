package de.itemis.mosig.jassuan.jscdlib;

import static de.itemis.mosig.jassuan.jscdlib.JAssuanNative.PCSC_SCOPE_SYSTEM;
import static de.itemis.mosig.jassuan.jscdlib.JAssuanNative.SCARD_ALL_READERS;
import static de.itemis.mosig.jassuan.jscdlib.JAssuanNative.SCARD_AUTOALLOCATE;
import static de.itemis.mosig.jassuan.jscdlib.problem.JScdProblems.SCARD_E_NO_READERS_AVAILABLE;
import static java.util.Objects.requireNonNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

import de.itemis.mosig.jassuan.jscdlib.internal.IntSegment;
import de.itemis.mosig.jassuan.jscdlib.internal.LongPointerSegment;
import de.itemis.mosig.jassuan.jscdlib.internal.StringPointerSegment;
import de.itemis.mosig.jassuan.jscdlib.problem.JScdException;
import de.itemis.mosig.jassuan.jscdlib.problem.JScdProblem;
import de.itemis.mosig.jassuan.jscdlib.problem.JScdProblems;
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

    private static final Logger LOG = LoggerFactory.getLogger(JScdHandle.class);
    private static final Set<JScdProblem> NON_FATAL_PROBLEMS = ImmutableSet.of(JScdProblems.SCARD_S_SUCCESS, JScdProblems.SCARD_E_NO_READERS_AVAILABLE);

    private final JAssuanNative nativeBridge;

    public JScdHandle(JAssuanNative nativeBridge) {
        this.nativeBridge = requireNonNull(nativeBridge, "nativeBridge");
    }

    public List<String> listReaders() {
        List<String> result = new ArrayList<>();
        LongPointerSegment ctxPtrSeg = new LongPointerSegment();
        StringPointerSegment readerListPtrSeg = new StringPointerSegment();
        MemoryAddress ptrToFirstEntryInReaderList = null;
        boolean ctxEstablished = false;
        boolean listReadersReturned = false;

        try (var readerListLength = new IntSegment()) {
            readerListLength.setValue(SCARD_AUTOALLOCATE);

            throwIfNoSuccess(nativeBridge.sCardEstablishContext(PCSC_SCOPE_SYSTEM, MemoryAddress.NULL, MemoryAddress.NULL, ctxPtrSeg.address()));
            ctxEstablished = true;

            var listReadersProblem = throwIfNoSuccess(
                nativeBridge.sCardListReadersA(ctxPtrSeg.getContainedAddress(), SCARD_ALL_READERS, readerListPtrSeg.address(), readerListLength.address()));
            listReadersReturned = true;
            ptrToFirstEntryInReaderList = readerListPtrSeg.getContainedAddress();
            if (listReadersProblem != SCARD_E_NO_READERS_AVAILABLE) {
                final int TRAILING_NULL = 1;
                var remainingLength = readerListLength.getValue() - TRAILING_NULL;
                while (remainingLength > 0) {
                    String currentReader = readerListPtrSeg.dereference();
                    result.add(currentReader);
                    var nextOffset = currentReader.getBytes(StandardCharsets.UTF_8).length + TRAILING_NULL;
                    readerListPtrSeg.pointTo(readerListPtrSeg.getContainedAddress().addOffset(nextOffset));
                    remainingLength -= nextOffset;
                }
            }
        } finally {
            if (ctxEstablished) {
                if (listReadersReturned) {
                    logIfNoSuccess(nativeBridge.sCardFreeMemory(ctxPtrSeg.getContainedAddress(), ptrToFirstEntryInReaderList),
                        "Possible ressource leak: Operation listReaders could not free memory.");
                }
                logIfNoSuccess(nativeBridge.sCardReleaseContext(ctxPtrSeg.getContainedAddress()),
                    "Possible ressource leak: Operation listReaders could not release scard context.");
            }
            safeClose(readerListPtrSeg);
            safeClose(ctxPtrSeg);
        }
        return Collections.unmodifiableList(result);
    }

    private JScdProblem throwIfNoSuccess(long errorCode) {
        var problem = JScdProblems.fromError(errorCode);
        if (!NON_FATAL_PROBLEMS.contains(problem)) {
            throw new JScdException(problem);
        }

        return problem;
    }

    private void logIfNoSuccess(long errorCode, String errMsg) {
        if (errorCode != JScdProblems.SCARD_S_SUCCESS.errorCode()) {
            var problem = JScdProblems.fromError(errorCode);

            LOG.warn(errMsg + " Reason: " + problem + ": " + problem.description());
        }
    }

    private void safeClose(AutoCloseable closeable) {
        try {
            closeable.close();
        } catch (Exception e) {
            var msg = e.getMessage() == null ? "No further information." : e.getMessage();
            LOG.warn("Possible ressource leak: Closing a ressource encountered a problem: " + e.getClass().getSimpleName() + ": " + msg);
        }
    }

    @Override
    public void close() throws Exception {

    }
}
