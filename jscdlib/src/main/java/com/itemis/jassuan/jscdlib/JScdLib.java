package com.itemis.jassuan.jscdlib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itemis.jassuan.jscdlib.internal.JAssuanNativeLinuxImpl;
import com.itemis.jassuan.jscdlib.internal.JAssuanNativeMacImpl;
import com.itemis.jassuan.jscdlib.internal.JAssuanNativeWinImpl;
import com.itemis.jassuan.jscdlib.internal.JScardNativeLinuxImpl;
import com.itemis.jassuan.jscdlib.internal.JScardNativeMacImpl;
import com.itemis.jassuan.jscdlib.internal.JScardNativeWinImpl;
import com.itemis.jassuan.jscdlib.internal.OsDetector;

/**
 * Entrypoint for the JScdLib.
 */
public final class JScdLib {

    private static final Logger LOG = LoggerFactory.getLogger(JScdLib.class);

    private static final boolean IS_WINDOWS;
    private static final boolean IS_MAC;

    static {
        var osDetector = new OsDetector();
        if (osDetector.isWindows()) {
            LOG.debug("Identified OS type Windows");
            IS_WINDOWS = true;
            IS_MAC = false;
        } else if (osDetector.isMac()) {
            LOG.debug("Identified OS type Mac");
            IS_WINDOWS = false;
            IS_MAC = true;
        } else {
            LOG.debug("Identified OS type other. Assuming Linux or compatible.");
            IS_WINDOWS = false;
            IS_MAC = false;
        }
    }

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

        if (IS_WINDOWS) {
            nativeImpl = new JScardNativeWinImpl();
        } else if (IS_MAC) {
            nativeImpl = new JScardNativeMacImpl();
        } else {
            nativeImpl = new JScardNativeLinuxImpl();
        }

        return new JSCardHandle(nativeImpl);
    }

    /**
     * <p>
     * Create a new handle. The handle will use the {@link JAssuanNative} implementation appropriate
     * for the current OS.
     * </p>
     * <p>
     * <b>Be aware:</b> The handle does hold resources and should therefore be closed when not
     * needed anymore in order to prevent resource leaks.
     * </p>
     *
     * @return A new instance of {@link JAssuanHandle}.
     */
    public static JAssuanHandle constructAssuanHandle() {
        JAssuanNative nativeImpl = null;

        if (IS_WINDOWS) {
            nativeImpl = new JAssuanNativeWinImpl();
        } else if (IS_MAC) {
            nativeImpl = new JAssuanNativeMacImpl();
        } else {
            nativeImpl = new JAssuanNativeLinuxImpl();
        }

        return new JAssuanHandle(nativeImpl, new JScdGpgConfSocketDiscovery(new JScdEnvSocketDiscovery()));
    }
}
