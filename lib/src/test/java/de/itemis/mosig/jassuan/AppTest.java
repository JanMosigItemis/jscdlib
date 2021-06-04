package de.itemis.mosig.jassuan;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.itemis.mosig.jassuan.assuan_h;

/**
 * Unit test for simple App.
 */
public class AppTest {
    private static final Logger LOG = LoggerFactory.getLogger(AppTest.class);

	/**
	 * Check if the classloader is able to load this class.
	 * If so, the binding to libassuan works appropriately.
	 **/
    @Test
    public void test_something() {
        assuan_h.class.getSimpleName();
    }
}
