package de.itemis.mosig.jassuan.jscdlib;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit test for simple App.
 */
public class JScdLibTest {
    private static final Logger LOG = LoggerFactory.getLogger(JScdLibTest.class);

    @Test
    public void test_something() {
        Assertions.assertThat(true).isTrue();

        JScdLib.main(null);
        LOG.debug("Log from test");
    }
}
