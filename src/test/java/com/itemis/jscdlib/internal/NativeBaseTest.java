package com.itemis.jscdlib.internal;

import static com.itemis.fluffyj.tests.exceptions.ExpectedExceptions.EXPECTED_CHECKED_EXCEPTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.itemis.jscdlib.problem.JScdException;

public class NativeBaseTest {

    private NativeBase underTest;

    @BeforeEach
    public void setUp() {
        underTest = new NativeBase() {};
    }

    @Test
    public void when_method_throws_then_runtime_exception() {
        var expected = new JScdException(EXPECTED_CHECKED_EXCEPTION);

        assertThatThrownBy(() -> underTest.callNativeFunction(() -> {
            throw EXPECTED_CHECKED_EXCEPTION;
        }))
            .isInstanceOf(JScdException.class)
            .hasMessage(expected.getMessage())
            .hasCause(EXPECTED_CHECKED_EXCEPTION);
    }

    @Test
    public void when_void_method_throws_then_runtime_exception() {
        var expected = new JScdException(EXPECTED_CHECKED_EXCEPTION);

        assertThatThrownBy(() -> underTest.callNativeVoidFunction(() -> {
            throw EXPECTED_CHECKED_EXCEPTION;
        }))
            .isInstanceOf(JScdException.class)
            .hasMessage(expected.getMessage())
            .hasCause(EXPECTED_CHECKED_EXCEPTION);
    }

    @Test
    public void return_wahtever_native_call_returns() {
        var expectedResult = new Object();
        assertThat(underTest.callNativeFunction(() -> expectedResult)).isEqualTo(expectedResult);
    }
}
