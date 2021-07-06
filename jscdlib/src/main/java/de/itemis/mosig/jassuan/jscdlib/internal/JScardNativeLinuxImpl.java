package de.itemis.mosig.jassuan.jscdlib.internal;

import static jdk.incubator.foreign.CLinker.C_LONG;
import static jdk.incubator.foreign.CLinker.C_POINTER;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.itemis.mosig.jassuan.jscdlib.JScardNative;
import de.itemis.mosig.jassuan.jscdlib.internal.memory.NativeMethodHandle;
import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.LibraryLookup;
import jdk.incubator.foreign.MemoryAddress;

public class JScardNativeLinuxImpl extends NativeBase implements JScardNative {

    private static final Logger LOG = LoggerFactory.getLogger(JScardNativeLinuxImpl.class);

    private final NativeMethodHandle<Long> establishCtx;
    private final NativeMethodHandle<Long> listReaders;
    private final NativeMethodHandle<Long> freeMem;
    private final NativeMethodHandle<Long> releaseCtx;

    public JScardNativeLinuxImpl() {
        var lib = loadLib();
        establishCtx = NativeMethodHandle
            .ofLib(lib)
            .returnType(long.class)
            .func("SCardEstablishContext")
            .args(C_LONG, C_POINTER, C_POINTER, C_POINTER)
            .create(CLinker.getInstance());

        listReaders = NativeMethodHandle
            .ofLib(lib)
            .returnType(long.class)
            .func("SCardListReaders")
            .args(C_POINTER, C_POINTER, C_POINTER, C_POINTER)
            .create(CLinker.getInstance());

        freeMem = NativeMethodHandle
            .ofLib(lib)
            .returnType(long.class)
            .func("SCardFreeMemory")
            .args(C_POINTER, C_POINTER)
            .create(CLinker.getInstance());

        releaseCtx = NativeMethodHandle
            .ofLib(lib)
            .returnType(long.class)
            .func("SCardReleaseContext")
            .args(C_POINTER)
            .create(CLinker.getInstance());
    }

    @Override
    public long sCardEstablishContext(long dwScope, MemoryAddress pvReserved1, MemoryAddress pvReserved2, MemoryAddress phContext) {
        return callNativeFunction(() -> establishCtx.call(dwScope, pvReserved1, pvReserved2, phContext));
    }

    @Override
    public long sCardListReadersA(MemoryAddress hContext, MemoryAddress mszGroups, MemoryAddress mszReaders, MemoryAddress pcchReaders) {
        return callNativeFunction(() -> listReaders.call(hContext, mszGroups, mszReaders, pcchReaders));
    }

    @Override
    public long sCardFreeMemory(MemoryAddress hContext, MemoryAddress pvMem) {
        return callNativeFunction(() -> freeMem.call(hContext, pvMem));
    }

    @Override
    public long sCardReleaseContext(MemoryAddress hContext) {
        return callNativeFunction(() -> releaseCtx.call(hContext));
    }

    private final LibraryLookup loadLib() {
        LibraryLookup libCandidate = null;

        // See
        // https://github.com/gpg/gnupg/blob/25ae80b8eb6e9011049d76440ad7d250c1d02f7c/scd/scdaemon.c#L210
        try {
            libCandidate = LibraryLookup.ofLibrary("libpcsclite.so.1");
        } catch (IllegalArgumentException outerE) {
            String msg = "Could not get a handle on lib.";
            LOG.debug(msg, outerE);
            try {
                libCandidate = LibraryLookup.ofLibrary("libpcsclite.so");
            } catch (IllegalArgumentException innerE) {
                LOG.error(msg + " Giving up.", innerE);
                throw innerE;
            }
        }

        return libCandidate;
    }
}
