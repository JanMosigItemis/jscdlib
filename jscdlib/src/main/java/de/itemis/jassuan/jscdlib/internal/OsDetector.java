package de.itemis.jassuan.jscdlib.internal;

import static com.google.common.base.Strings.nullToEmpty;

/**
 * A little helper class, that uses the os.name system property to identify the current OS.
 */
public final class OsDetector {

    public boolean isWindows() {
        return getSanitizedOsName().startsWith("windows");
    }

    public boolean isMac() {
        return getSanitizedOsName().startsWith("mac");
    }

    public boolean isLinux() {
        return getSanitizedOsName().startsWith("linux");
    }

    public boolean isOther() {
        return !isWindows() && !isLinux() && !isMac();
    }

    private String getSanitizedOsName() {
        return nullToEmpty(System.getProperty("os.name")).toLowerCase().trim();
    }
}
