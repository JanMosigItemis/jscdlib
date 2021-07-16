package de.itemis.mosig.jassuan.jscdlib.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import de.itemis.mosig.fluffyj.tests.FluffyTestSystemProperties;

public class OsDetectorTest {

    private static final String OS_NAME_KEY = "os.name";

    @RegisterExtension
    FluffyTestSystemProperties sysProps = new FluffyTestSystemProperties();

    private OsDetector underTest;

    @BeforeEach
    public void setUp() {
        underTest = new OsDetector();
    }

    @Test
    public void identifiesWindows() {
        System.setProperty(OS_NAME_KEY, "Windows Something");

        assertThat(underTest.isWindows()).isTrue();
        assertThat(underTest.isMac()).isFalse();
        assertThat(underTest.isLinux()).isFalse();
        assertThat(underTest.isOther()).isFalse();
    }

    @Test
    public void identifiesLinux() {
        System.setProperty(OS_NAME_KEY, "Linux Something");

        assertThat(underTest.isWindows()).isFalse();
        assertThat(underTest.isMac()).isFalse();
        assertThat(underTest.isLinux()).isTrue();
        assertThat(underTest.isOther()).isFalse();
    }

    @Test
    public void identifiesMac() {
        System.setProperty(OS_NAME_KEY, "Mac Something");

        assertThat(underTest.isWindows()).isFalse();
        assertThat(underTest.isMac()).isTrue();
        assertThat(underTest.isLinux()).isFalse();
        assertThat(underTest.isOther()).isFalse();
    }

    @Test
    public void identifiesOther() {
        System.setProperty(OS_NAME_KEY, "Other Something");

        assertThat(underTest.isWindows()).isFalse();
        assertThat(underTest.isMac()).isFalse();
        assertThat(underTest.isLinux()).isFalse();
        assertThat(underTest.isOther()).isTrue();
    }
}
