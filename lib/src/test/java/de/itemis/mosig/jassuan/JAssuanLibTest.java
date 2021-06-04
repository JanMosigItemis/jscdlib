package de.itemis.mosig.jassuan;

import org.junit.jupiter.api.Test;
import de.itemis.mosig.jassuan.assuan_h;

/**
 * Unit test for simple App.
 */
public class JAssuanLibTest {

	/**
	 * Check if the classloader is able to load this class.
	 * If so, the binding to libassuan works appropriately.
	 **/
    @Test
    public void testLibLoadingWorks() {
        assuan_h.class.getSimpleName();
    }
}
