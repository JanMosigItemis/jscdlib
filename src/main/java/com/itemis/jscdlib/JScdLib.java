package com.itemis.jscdlib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itemis.jscdlib.internal.AssuanLibNativeLinuxImpl;
import com.itemis.jscdlib.internal.AssuanLibNativeMacImpl;
import com.itemis.jscdlib.internal.AssuanLibNativeWinImpl;
import com.itemis.jscdlib.internal.ScardLibNativeLinuxImpl;
import com.itemis.jscdlib.internal.ScardLibNativeMacImpl;
import com.itemis.jscdlib.internal.ScardLibNativeWinImpl;
import com.itemis.jscdlib.internal.OsDetector;

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
     * Create a new handle. The handle will use the {@link ScardLibNative} implementation appropriate
     * for the current OS.
     * </p>
     * <p>
     * <b>Be aware:</b> The handle does hold resources and should therefore be closed when not
     * needed anymore in order to prevent resource leaks.
     * </p>
     *
     * @return A new instance of {@link SCardLibHandle}.
     */
    public static SCardLibHandle constructSCardHandle() {
        ScardLibNative nativeImpl = null;

        if (IS_WINDOWS) {
            nativeImpl = new ScardLibNativeWinImpl();
        } else if (IS_MAC) {
            nativeImpl = new ScardLibNativeMacImpl();
        } else {
            nativeImpl = new ScardLibNativeLinuxImpl();
        }

        return new SCardLibHandle(nativeImpl);
    }

    /**
     * <p>
     * Create a new handle. The handle will use the {@link AssuanLibNative} implementation appropriate
     * for the current OS.
     * </p>
     * <p>
     * <b>Be aware:</b> The handle does hold resources and should therefore be closed when not
     * needed anymore in order to prevent resource leaks.
     * </p>
     *
     * @return A new instance of {@link AssuanLibHandle}.
     */
    public static AssuanLibHandle constructAssuanHandle() {
        AssuanLibNative nativeImpl = null;

        if (IS_WINDOWS) {
            nativeImpl = new AssuanLibNativeWinImpl();
        } else if (IS_MAC) {
            nativeImpl = new AssuanLibNativeMacImpl();
        } else {
            nativeImpl = new AssuanLibNativeLinuxImpl();
        }

        return new AssuanLibHandle(nativeImpl, new JScdGpgConfSocketDiscovery(new JScdEnvSocketDiscovery()));
    }
}
