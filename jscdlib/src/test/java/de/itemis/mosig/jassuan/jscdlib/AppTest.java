package de.itemis.mosig.jassuan.jscdlib;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit test for simple App.
 */
public class AppTest {
    private static final Logger LOG = LoggerFactory.getLogger(AppTest.class);

    @Test
    public void test_something() {
        assertThat(true).isTrue();

        App.main(null);
        LOG.debug("Log from test");
    }
}
