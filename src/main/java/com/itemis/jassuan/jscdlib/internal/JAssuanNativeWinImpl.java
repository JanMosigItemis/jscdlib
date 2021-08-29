package com.itemis.jassuan.jscdlib.internal;

import static jdk.incubator.foreign.CLinker.C_INT;
import static jdk.incubator.foreign.CLinker.C_POINTER;

import com.itemis.jassuan.jscdlib.JAssuanNative;
import com.itemis.jassuan.jscdlib.internal.memory.NativeMethodHandle;

import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.LibraryLookup;
import jdk.incubator.foreign.MemoryAddress;

public class JAssuanNativeWinImpl extends NativeBase implements JAssuanNative {

    private final NativeMethodHandle<Long> assuanNew;
    private final NativeMethodHandle<Long> assuanRelease;
    private final NativeMethodHandle<Long> assuanSocketConnect;
    private final NativeMethodHandle<Long> assuanTransact;

    public JAssuanNativeWinImpl() {
        var lib = LibraryLookup.ofLibrary("libassuan6-0");
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
}

