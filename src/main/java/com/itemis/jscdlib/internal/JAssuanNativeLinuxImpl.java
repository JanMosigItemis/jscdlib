package com.itemis.jscdlib.internal;

import static jdk.incubator.foreign.CLinker.C_INT;
import static jdk.incubator.foreign.CLinker.C_POINTER;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itemis.jscdlib.JAssuanNative;
import com.itemis.jscdlib.internal.memory.NativeMethodHandle;

import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.LibraryLookup;
import jdk.incubator.foreign.MemoryAddress;

public class JAssuanNativeLinuxImpl extends NativeBase implements JAssuanNative {

    private static final Logger LOG = LoggerFactory.getLogger(JAssuanNativeLinuxImpl.class);

    private final NativeMethodHandle<Long> assuanNew;
    private final NativeMethodHandle<Long> assuanRelease;
    private final NativeMethodHandle<Long> assuanSocketConnect;
    private final NativeMethodHandle<Long> assuanTransact;

    public JAssuanNativeLinuxImpl() {
        var lib = loadLib();

        assuanNew = NativeMethodHandle
            .ofLib(lib)
            .returnType(long.class)
            .func("assuan_new")
            .args(C_POINTER)
            .create(CLinker.getInstance());

        assuanRelease = NativeMethodHandle
            .ofLib(lib)
            .returnType(long.class)
            .func("assuan_release")
            .args(C_POINTER)
            .create(CLinker.getInstance());

        assuanSocketConnect = NativeMethodHandle
            .ofLib(lib)
            .returnType(long.class)
            .func("assuan_socket_connect")
            .args(C_POINTER, C_POINTER, C_INT, C_INT)
            .create(CLinker.getInstance());

        assuanTransact = NativeMethodHandle
            .ofLib(lib)
            .returnType(long.class)
            .func("assuan_transact")
            .args(C_POINTER, C_POINTER, C_POINTER, C_POINTER, C_POINTER, C_POINTER, C_POINTER, C_POINTER)
            .create(CLinker.getInstance());
    }

    @Override
    public long assuanNew(MemoryAddress p_ctx) {
        return callNativeFunction(() -> assuanNew.call(p_ctx));
    }

    @Override
    public void assuanRelease(MemoryAddress ctx) {
        callNativeVoidFunction(() -> assuanRelease.call(ctx));
    }

    @Override
    public long assuanSocketConnect(MemoryAddress ctx, MemoryAddress name, int server_pid, int flags) {
        return callNativeFunction(() -> assuanSocketConnect.call(ctx, name, server_pid, flags));
    }

    @Override
    public long assuanTransact(MemoryAddress ctx, MemoryAddress command, MemoryAddress data_cb, MemoryAddress data_cb_arg, MemoryAddress inquire_cb,
            MemoryAddress inquire_cb_arg, MemoryAddress status_cb, MemoryAddress status_cb_arg) {
        return callNativeFunction(() -> assuanTransact.call(ctx, command, data_cb, data_cb_arg, inquire_cb, inquire_cb_arg, status_cb, status_cb_arg));
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

