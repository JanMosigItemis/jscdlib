package de.itemis.mosig.jassuan.jscdlib.internal;

public interface Dereferenceable<T> extends AutoCloseable {

    T dereference();
}
