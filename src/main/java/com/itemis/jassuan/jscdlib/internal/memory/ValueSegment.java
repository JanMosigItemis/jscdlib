package com.itemis.jassuan.jscdlib.internal.memory;

public interface ValueSegment<T> {

    T getValue();

    T setValue(T newValue);
}
