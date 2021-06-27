package de.itemis.mosig.jassuan.jscdlib;

import static jdk.incubator.foreign.CLinker.C_LONG_LONG;
import static jdk.incubator.foreign.CLinker.C_POINTER;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import de.itemis.mosig.jassuan.jscdlib.problem.JScdException;
import de.itemis.mosig.jassuan.jscdlib.problem.JScdProblems;
import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.LibraryLookup;
import jdk.incubator.foreign.LibraryLookup.Symbol;
import jdk.incubator.foreign.MemoryAddress;

public class FlaNativeImpl implements JAssuanNative {

    private static final CLinker LINKER = CLinker.getInstance();
    private static final LibraryLookup S_CARD_LIB = LibraryLookup.ofLibrary("winscard");

    private static final Symbol ESTABLISH_CTX_FUNC = loadSymbol(S_CARD_LIB, "SCardEstablishContext");
    private static final FunctionDescriptor ESTABLISH_CTX_FUNC_DESCR = FunctionDescriptor.of(C_LONG_LONG, C_LONG_LONG, C_POINTER, C_POINTER, C_POINTER);
    private static final MethodType ESTABLISH_CTX_METHOD_TYPE =
        MethodType.methodType(long.class, long.class, MemoryAddress.class, MemoryAddress.class, MemoryAddress.class);
    private static final MethodHandle ESTABLISH_CTX_METHOD = LINKER.downcallHandle(ESTABLISH_CTX_FUNC, ESTABLISH_CTX_METHOD_TYPE, ESTABLISH_CTX_FUNC_DESCR);

    private static final Symbol LIST_READERS_FUNC = loadSymbol(S_CARD_LIB, "SCardListReadersA");
    private static final FunctionDescriptor LIST_READERS_FUNC_DESCR = FunctionDescriptor.of(C_LONG_LONG, C_POINTER, C_POINTER, C_POINTER, C_POINTER);
    private static final MethodType LIST_READERS_METHOD_TYPE =
        MethodType.methodType(long.class, MemoryAddress.class, MemoryAddress.class, MemoryAddress.class, MemoryAddress.class);
    private static final MethodHandle LIST_READERS_METHOD = LINKER.downcallHandle(LIST_READERS_FUNC, LIST_READERS_METHOD_TYPE, LIST_READERS_FUNC_DESCR);

    private static final Symbol FREE_MEM_FUNC = loadSymbol(S_CARD_LIB, "SCardFreeMemory");
    private static final FunctionDescriptor FREE_MEM_FUNC_DESCR = FunctionDescriptor.of(C_LONG_LONG, C_POINTER, C_POINTER);
    private static final MethodType FREE_MEM_METHOD_TYPE = MethodType.methodType(long.class, MemoryAddress.class,
        MemoryAddress.class);
    private static final MethodHandle FREE_MEM_METHOD = LINKER.downcallHandle(FREE_MEM_FUNC, FREE_MEM_METHOD_TYPE, FREE_MEM_FUNC_DESCR);

    private static final Symbol RELEASE_CTX_FUNC = loadSymbol(S_CARD_LIB, "SCardReleaseContext");
    private static final FunctionDescriptor RELEASE_CTX_FUNC_DESCR = FunctionDescriptor.of(C_LONG_LONG, C_POINTER);
    private static final MethodType RELEASE_CTX_METHOD_TYPE = MethodType.methodType(long.class, MemoryAddress.class);
    private static final MethodHandle RELEASE_CTX_METHOD = LINKER.downcallHandle(RELEASE_CTX_FUNC, RELEASE_CTX_METHOD_TYPE,
        RELEASE_CTX_FUNC_DESCR);

    @Override
    public long sCardEstablishContext(long dwScope, MemoryAddress pvReserved1, MemoryAddress pvReserved2, MemoryAddress phContext) {
        return callNativeFunction(() -> (long) ESTABLISH_CTX_METHOD.invokeExact(dwScope, pvReserved1, pvReserved2, phContext));
    }

    @Override
    public long sCardListReadersA(MemoryAddress hContext, MemoryAddress mszGroups, MemoryAddress mszReaders, MemoryAddress pcchReaders) {
        return callNativeFunction(() -> (long) LIST_READERS_METHOD.invokeExact(hContext, mszGroups, mszReaders, pcchReaders));
    }

    @Override
    public long sCardFreeMemory(MemoryAddress hContext, MemoryAddress pvMem) {
        return callNativeFunction(() -> (long) FREE_MEM_METHOD.invokeExact(hContext, pvMem));
    }

    @Override
    public long sCardReleaseContext(MemoryAddress hContext) {
        return callNativeFunction(() -> (long) RELEASE_CTX_METHOD.invokeExact(hContext));
    }

    private static Symbol loadSymbol(LibraryLookup lib, String symbolName) {
        return lib.lookup(symbolName)
            .orElseThrow(() -> new RuntimeException("Could not find symbol '" + symbolName + "' in library '" + lib.toString() + "'."));
    }

    private long callNativeFunction(ThrowingSupplier<Long> nativeFunction) {
        long errorCode = JScdProblems.SCARD_S_SUCCESS.errorCode();
        try {
            errorCode = nativeFunction.get();
        } catch (Throwable e) {
            throw new JScdException(e);
        }
        return errorCode;
    }

    @FunctionalInterface
    private static interface ThrowingSupplier<T> {
        T get() throws Throwable;
    }

}
