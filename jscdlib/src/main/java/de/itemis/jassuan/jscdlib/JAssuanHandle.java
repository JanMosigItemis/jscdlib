package de.itemis.jassuan.jscdlib;

import static com.itemis.fluffyj.exceptions.ThrowablePrettyfier.pretty;
import static de.itemis.jassuan.jscdlib.JAssuanNative.ASSUAN_INVALID_PID;
import static de.itemis.jassuan.jscdlib.JAssuanNative.ASSUAN_SOCKET_CONNECT_FDPASSING;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static jdk.incubator.foreign.CLinker.C_INT;
import static jdk.incubator.foreign.CLinker.C_LONG_LONG;
import static jdk.incubator.foreign.CLinker.C_POINTER;
import static jdk.incubator.foreign.MemoryAddress.NULL;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Set;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

import de.itemis.jassuan.jscdlib.internal.memory.LongPointerSegment;
import de.itemis.jassuan.jscdlib.problem.JScdException;
import de.itemis.jassuan.jscdlib.problem.JScdProblem;
import de.itemis.jassuan.jscdlib.problem.JScdProblems;
import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.MemoryAddress;

public class JAssuanHandle implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(JAssuanHandle.class);

    private static final CLinker LINKER = CLinker.getInstance();
    private static final Set<JScdProblem> NON_FATAL_PROBLEMS = ImmutableSet.of(JScdProblems.SCARD_S_SUCCESS);

    private final LongPointerSegment ctxPtr;
    private final MemoryAddress ctxAddr;
    private final JAssuanNative nativeBridge;

    private volatile boolean isClosed = false;

    /**
     * Create a new instance and initialize resources. Be aware that after construction, this object
     * holds resources, so it might be a bad idea to construct many instances of this object in
     * advance.
     *
     * @param nativeBridge OS dependent implementation to use when calling low level library
     *        functions.
     * @param socketDiscovery Used to determine the scdaemon socket file to use for communication to
     *        the daemon.
     */
    public JAssuanHandle(JAssuanNative nativeBridge, JScdSocketDiscovery socketDiscovery) {
        this.nativeBridge = requireNonNull(nativeBridge, "nativeBridge");
        requireNonNull(socketDiscovery, "socketDiscovery");

        try {
            ctxPtr = new LongPointerSegment();
            throwIfNoSuccess(nativeBridge.assuanNew(ctxPtr.address()));
            ctxAddr = ctxPtr.getContainedAddress();

            var name = CLinker.toCString(socketDiscovery.discover().toString(), UTF_8).address();
            throwIfNoSuccess(nativeBridge.assuanSocketConnect(ctxAddr, name, ASSUAN_INVALID_PID, ASSUAN_SOCKET_CONNECT_FDPASSING));
        } catch (Throwable t) {
            close();
            throw t;
        }
    }

    /**
     * <p>
     * Send a command to the scdaemon.
     * </p>
     * <p>
     * Be aware that for some commands, scdaemon uses the response callback and for others the
     * status callback.
     * </p>
     * <p>
     * Be also aware that consumers are called synchronously, i. e. this method won't return if one
     * of the consumers blocks.
     * </p>
     *
     * @param command The command to send.
     * @param responseConsumer Called when scdaemon responds with data.
     * @param statusConsumer Called when scdaemon responds with status lines.
     */
    public void sendCommand(String command, Consumer<String> responseConsumer, Consumer<String> statusConsumer) {
        var callback = new TransactCallback(responseConsumer, statusConsumer);

        MethodHandle data_cbJava = null;
        MethodHandle inquire_cbJava = null;
        MethodHandle status_cbJava = null;
        try {
            data_cbJava = MethodHandles.lookup().bind(callback, "data_cb",
                MethodType.methodType(int.class, MemoryAddress.class, MemoryAddress.class, long.class));
            inquire_cbJava = MethodHandles.lookup().bind(callback, "inquire_cb",
                MethodType.methodType(int.class, MemoryAddress.class, MemoryAddress.class));
            status_cbJava = MethodHandles.lookup().bind(callback, "status_cb",
                MethodType.methodType(int.class, MemoryAddress.class, MemoryAddress.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException("Implementation problem: Could not find callback method: ");
        }

        try (var data_cbC = LINKER.upcallStub(data_cbJava, FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER, C_LONG_LONG));
                var inquire_cbC = LINKER.upcallStub(inquire_cbJava, FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER));
                var status_cbC = LINKER.upcallStub(status_cbJava, FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER))) {
            var cmdAddr = CLinker.toCString(command, UTF_8).address();
            throwIfNoSuccess(nativeBridge.assuanTransact(ctxAddr, cmdAddr, data_cbC.address(), NULL, inquire_cbC.address(), NULL, status_cbC.address(), NULL));
        }
    }

    @Override
    public final void close() {
        if (ctxAddr != null) {
            if (!isClosed) {
                synchronized (this) {
                    if (!isClosed) {
                        isClosed = true;
                        try {
                            nativeBridge.assuanRelease(ctxAddr);
                        } catch (Throwable t) {
                            LOG.warn(
                                "Possible ressource leak: Operation assuanRelease could not release assuan context. Reason: " + pretty(t));
                        } finally {
                            safeClose(ctxPtr);
                        }
                    }
                }
            }
        }
    }

    private JScdProblem throwIfNoSuccess(long errorCode) {
        var problem = JScdProblems.fromError(errorCode);
        if (!NON_FATAL_PROBLEMS.contains(problem)) {
            throw new JScdException(problem);
        }

        return problem;
    }

    private void safeClose(AutoCloseable closeable) {
        try {
            closeable.close();
        } catch (Exception e) {
            var msg = e.getMessage() == null ? "No further information." : e.getMessage();
            LOG.warn("Possible ressource leak: Closing a ressource encountered a problem: " + e.getClass().getSimpleName() + ": " + msg);
        }
    }

    private static final class TransactCallback {
        private final int SUCCESS = (int) JScdProblems.SCARD_S_SUCCESS.errorCode();

        private final Consumer<String> responseConsumer;
        private final Consumer<String> statusConsumer;

        public TransactCallback(Consumer<String> responseConsumer, Consumer<String> statusConsumer) {
            this.responseConsumer = responseConsumer;
            this.statusConsumer = statusConsumer;
        }

        // False positive. Used as a function pointer callback by C-code.
        @SuppressWarnings("unused")
        public int data_cb(MemoryAddress allLines, MemoryAddress currentLine, long lineLength) {
            System.out.println("data_cb");
            responseConsumer.accept(CLinker.toJavaStringRestricted(currentLine, UTF_8));
            return SUCCESS;
        }

        // False positive. Used as a function pointer callback by C-code.
        @SuppressWarnings("unused")
        public int inquire_cb(MemoryAddress allLines, MemoryAddress currentLine) {
            System.out.println("inquire_cb");
            return SUCCESS;
        }

        // False positive. Used as a function pointer callback by C-code.
        @SuppressWarnings("unused")
        public int status_cb(MemoryAddress allLines, MemoryAddress currentLine) {
            System.out.println("status_cb");
            statusConsumer.accept(CLinker.toJavaStringRestricted(currentLine, UTF_8));
            return SUCCESS;
        }
    }
}
