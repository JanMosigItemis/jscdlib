package de.itemis.mosig.jassuan.jscdlib;

import static de.itemis.mosig.jassuan.jscdlib.JScdLib.createHandle;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

import de.itemis.mosig.fluffy.tests.java.FluffyTestHelper;

public class JScdLibTest {

    @Test
    public void test_jscdlib_is_final() {
        FluffyTestHelper.assertFinal(JScdLib.class);
    }

    @Test
    public void test_createHandle_returns_handle() {
        assertThat(createHandle(mock(JAssuanNative.class))).as("Handle creation is broken.").isInstanceOf(JScdHandle.class);
    }
}
