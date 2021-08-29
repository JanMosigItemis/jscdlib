package com.itemis.jscdlib.internal;

import com.itemis.jscdlib.problem.JScdException;

public abstract class NativeBase {

    final <T> T callNativeFunction(ThrowingSupplier<T> nativeFunction) {
        T result = null;
        try {
            result = nativeFunction.get();
        } catch (Throwable t) {
            throw new JScdException(t);
        }
        return result;
    }

    final void callNativeVoidFunction(ThrowingRunnable nativeFunction) {
        try {
            nativeFunction.run();
        } catch (Throwable t) {
            throw new JScdException(t);
        }
    }

    @FunctionalInterface
    static interface ThrowingSupplier<T> {
        T get() throws Throwable;
    }

    @FunctionalInterface
    static interface ThrowingRunnable {
        void run() throws Throwable;
    }
}
