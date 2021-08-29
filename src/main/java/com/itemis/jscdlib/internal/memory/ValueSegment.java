package com.itemis.jscdlib.internal.memory;

public interface ValueSegment<T> {

    T getValue();

    T setValue(T newValue);
}
