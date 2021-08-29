package com.itemis.jscdlib;

import static com.itemis.jscdlib.JScardNative.PCSC_SCOPE_SYSTEM;
import static com.itemis.jscdlib.JScardNative.SCARD_ALL_READERS;
import static com.itemis.jscdlib.JScardNative.SCARD_AUTOALLOCATE;
import static com.itemis.jscdlib.problem.JScdProblems.SCARD_E_NO_READERS_AVAILABLE;
import static java.util.Objects.requireNonNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.itemis.jscdlib.internal.memory.IntSegment;
import com.itemis.jscdlib.internal.memory.LongPointerSegment;
import com.itemis.jscdlib.internal.memory.StringPointerSegment;
import com.itemis.jscdlib.problem.JScdException;
import com.itemis.jscdlib.problem.JScdProblem;
import com.itemis.jscdlib.problem.JScdProblems;

import jdk.incubator.foreign.MemoryAddress;

/**
 * <p>
 * Provides convenient Java versions of SCard based functionality.
 * 
 * @see <a href=
 *      "https://docs.microsoft.com/en-us/windows/win32/api/winscard/">https://docs.microsoft.com/en-us/windows/win32/api/winscard/</a>
 *      </p>
 */
public final class JSCardHandle implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(JSCardHandle.class);
    private static final Set<JScdProblem> NON_FATAL_PROBLEMS = ImmutableSet.of(JScdProblems.SCARD_S_SUCCESS, JScdProblems.SCARD_E_NO_READERS_AVAILABLE);

    private final JScardNative nativeBridge;

    public JSCardHandle(JScardNative nativeBridge) {
        this.nativeBridge = requireNonNull(nativeBridge, "nativeBridge");
    }

    /**
     * <p>
     * Query the OS for available smart card readers.
     * </p>
     * <p>
     * <b>Be aware:</b> The current implementation of this method allocates and destroys resources
     * each time this method is called, which is a very costly operation. Thus it is not a good idea
     * to call this method very often in small amount of time.
     * </p>
     * 
     * @return A list of available reader names or empty list if none is available.
     * @throws JScdException if something went wrong.
     */
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
