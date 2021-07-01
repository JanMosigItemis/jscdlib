package de.itemis.mosig.jassuan.jscdlib.internal.memory;

public interface Dereferenceable<T> extends AutoCloseable {

    T dereference();
}
