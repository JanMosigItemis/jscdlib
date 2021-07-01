package de.itemis.mosig.jassuan.jscdlib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.itemis.mosig.jassuan.jscdlib.internal.JScardNativeLinuxImpl;
import de.itemis.mosig.jassuan.jscdlib.internal.JScardNativeMacImpl;
import de.itemis.mosig.jassuan.jscdlib.internal.JScardNativeWinImpl;
import de.itemis.mosig.jassuan.jscdlib.internal.OsDetector;

/**
 * Entrypoint for the JScdLib.
 */
public final class JScdLib {

    private static final Logger LOG = LoggerFactory.getLogger(JScdLib.class);

    /**
     * <p>
     * Create a new handle. The handle will use the {@link JScardNative} implementation appropriate
     * for the current OS.
     * </p>
     * <p>
     * <b>Be aware:</b> The handle does hold resources and should therefore be closed when not
     * needed anymore in order to prevent resource leaks.
     * </p>
     *
     * @return A new instance of {@link JSCardHandle}.
     */
    public static JSCardHandle constructSCardHandle() {
        JScardNative nativeImpl = null;

        var osDetector = new OsDetector();
        if (osDetector.isWindows()) {
            LOG.debug("Identified OS type Windows");
            nativeImpl = new JScardNativeWinImpl();
        } else if (osDetector.isMac()) {
            LOG.debug("Identified OS type Mac");
            nativeImpl = new JScardNativeMacImpl();
        } else {
            LOG.debug("Identified OS type other. Using Linux implementation.");
            nativeImpl = new JScardNativeLinuxImpl();
        }

        return new JSCardHandle(nativeImpl);
    }
}
