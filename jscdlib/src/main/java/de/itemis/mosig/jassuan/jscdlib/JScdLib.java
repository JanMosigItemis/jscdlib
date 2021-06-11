package de.itemis.mosig.jassuan.jscdlib;

/**
 * Entrypoint for the JScdLib.
 */
public final class JScdLib {

    /**
     * <p>
     * Create a new handle. The handle will use the provided {@code nativeBridge} to call underlying
     * system libraries.
     * </p>
     * <p>
     * <b>Be aware:</b> The handle does hold resources and should therefore be closed when not
     * needed anymore in order to prevent resource leaks.
     * </p>
     *
     * @param nativeBridge
     * @return A new instance of {@link JScdHandle}.
     */
    public static JScdHandle createHandle(JAssuanNative nativeBridge) {
        return new JScdHandle(nativeBridge);
    }
}
