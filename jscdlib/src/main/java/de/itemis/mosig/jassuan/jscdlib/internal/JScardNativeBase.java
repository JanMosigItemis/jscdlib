package de.itemis.mosig.jassuan.jscdlib.internal;

import de.itemis.mosig.jassuan.jscdlib.problem.JScdException;
import de.itemis.mosig.jassuan.jscdlib.problem.JScdProblems;

public abstract class JScardNativeBase {

    long callNativeFunction(ThrowingSupplier<Long> nativeFunction) {
        long errorCode = JScdProblems.SCARD_S_SUCCESS.errorCode();
        try {
            errorCode = nativeFunction.get();
        } catch (Throwable e) {
            throw new JScdException(e);
        }
        return errorCode;
    }

    @FunctionalInterface
    static interface ThrowingSupplier<T> {
        T get() throws Throwable;
    }
}
