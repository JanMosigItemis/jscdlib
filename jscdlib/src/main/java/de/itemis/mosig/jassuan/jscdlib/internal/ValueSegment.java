package de.itemis.mosig.jassuan.jscdlib.internal;

public interface ValueSegment<T> {

    T getValue();

    T setValue(T newValue);
}
