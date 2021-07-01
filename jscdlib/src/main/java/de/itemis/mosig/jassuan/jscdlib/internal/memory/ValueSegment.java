package de.itemis.mosig.jassuan.jscdlib.internal.memory;

public interface ValueSegment<T> {

    T getValue();

    T setValue(T newValue);
}
