package com.itemis.jscdlib.internal.memory;

public interface Dereferenceable<T> extends AutoCloseable {

    T dereference();
}
