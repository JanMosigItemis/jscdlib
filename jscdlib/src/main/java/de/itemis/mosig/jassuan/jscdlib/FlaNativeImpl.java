package de.itemis.mosig.jassuan.jscdlib;

import static jdk.incubator.foreign.CLinker.C_LONG_LONG;
import static jdk.incubator.foreign.CLinker.C_POINTER;

import java.lang.invoke.MethodType;

import de.itemis.mosig.jassuan.jscdlib.problem.JScdProblems;
import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.LibraryLookup;
import jdk.incubator.foreign.LibraryLookup.Symbol;
import jdk.incubator.foreign.MemoryAddress;

public class FlaNativeImpl implements JAssuanNative {

    CLinker linker = CLinker.getInstance();
    LibraryLookup cLib = LibraryLookup.ofLibrary("winscard");

    // var freeMemFunc = loadSymbol(cLib, "SCardFreeMemory");
    // var freeMemFuncDescr = FunctionDescriptor.of(C_LONG_LONG, C_POINTER, C_POINTER);
    // var freeMemMethodType = MethodType.methodType(long.class, MemoryAddress.class,
    // MemoryAddress.class);
    // var freeMemMethod = linker.downcallHandle(freeMemFunc, freeMemMethodType, freeMemFuncDescr);
    //
    // var releaseCtxFunc = loadSymbol(cLib, "SCardReleaseContext");
    // var releaseCtxFuncDescr = FunctionDescriptor.of(C_LONG_LONG, C_POINTER);
    // var releaseCtxMethodType = MethodType.methodType(long.class, MemoryAddress.class);
    // var releaseCtxMethod = linker.downcallHandle(releaseCtxFunc, releaseCtxMethodType,
    // releaseCtxFuncDescr);

    @Override
    public long sCardEstablishContext(long dwScope, MemoryAddress pvReserved1, MemoryAddress pvReserved2, MemoryAddress phContext) {
        var establishCtxFunc = loadSymbol(cLib, "SCardEstablishContext");
        var establishCtxFuncDescr = FunctionDescriptor.of(C_LONG_LONG, C_LONG_LONG, C_POINTER, C_POINTER, C_POINTER);
        var establishCtxMethodType = MethodType.methodType(long.class, long.class, MemoryAddress.class, MemoryAddress.class, MemoryAddress.class);
        var establishCtxMethod = linker.downcallHandle(establishCtxFunc, establishCtxMethodType, establishCtxFuncDescr);

        long errorCode = JScdProblems.SCARD_S_SUCCESS.errorCode();
        try {
            errorCode = (long) establishCtxMethod.invokeExact(dwScope, pvReserved1, pvReserved2, phContext);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return errorCode;
    }

    @Override
    public long sCardListReadersA(MemoryAddress hContext, MemoryAddress mszGroups, MemoryAddress mszReaders, MemoryAddress pcchReaders) {
        var listReadersFunc = loadSymbol(cLib, "SCardListReadersA");
        var listReadersFuncDescr = FunctionDescriptor.of(C_LONG_LONG, C_POINTER, C_POINTER, C_POINTER, C_POINTER);
        var listReadersMethodType = MethodType.methodType(long.class, MemoryAddress.class, MemoryAddress.class, MemoryAddress.class, MemoryAddress.class);
        var listReadersMethod = linker.downcallHandle(listReadersFunc, listReadersMethodType, listReadersFuncDescr);

        long errorCode = JScdProblems.SCARD_S_SUCCESS.errorCode();
        try {
            errorCode = (long) listReadersMethod.invokeExact(hContext, mszGroups, mszReaders, pcchReaders);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return errorCode;
    }

    @Override
    public long sCardFreeMemory(MemoryAddress hContext, MemoryAddress pvMem) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long sCardReleaseContext(MemoryAddress hContext) {
        // TODO Auto-generated method stub
        return 0;
    }

    private static Symbol loadSymbol(LibraryLookup lib, String symbolName) {
        return lib.lookup(symbolName)
            .orElseThrow(() -> new RuntimeException("Could not find symbol '" + symbolName + "' in library '" + lib.toString() + "'."));
    }

}
