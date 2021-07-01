package de.itemis.mosig.jassuan.jscdlib.internal;

import static de.itemis.mosig.fluffy.tests.java.exceptions.ExpectedExceptions.EXPECTED_CHECKED_EXCEPTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.itemis.mosig.jassuan.jscdlib.problem.JScdException;

public class JScardNativeBaseTest {

    private JScardNativeBase underTest;

    @BeforeEach
    public void setUp() {
        underTest = new JScardNativeBase() {};
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
    public void when_method_returns_error_return_error() {
        var expectedReturnCode = 1L;
        assertThat(underTest.callNativeFunction(() -> expectedReturnCode)).isEqualTo(expectedReturnCode);
    }
}
