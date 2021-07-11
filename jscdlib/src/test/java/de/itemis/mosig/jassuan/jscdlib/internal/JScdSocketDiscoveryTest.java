package de.itemis.mosig.jassuan.jscdlib.internal;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static de.itemis.mosig.fluffy.tests.java.exceptions.ThrowablePrettyfier.pretty;
import static de.itemis.mosig.jassuan.jscdlib.problem.JScdProblems.JSCD_GENERAL_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import de.itemis.mosig.fluffy.tests.java.FluffySystemProperties;
import de.itemis.mosig.jassuan.jscdlib.problem.JScdException;

public class JScdSocketDiscoveryTest {

    private static final String JSCDLIB_SOCKET_FILE_PROP_KEY = "jscdlib.socket.file";
    private static final String GNUPGHOME_ENV_KEY = "GNUPGHOME";
    private static final String SOCKET_FILE_NAME = "S.scdaemon";

    @RegisterExtension
    FluffySystemProperties fluffyProps = new FluffySystemProperties();

    private JScdSocketDiscovery underTest;

    @BeforeEach
    public void setUp() {
        underTest = new JScdSocketDiscovery();
    }

    @Test
    public void given_no_env_and_no_prop_then_jscdexception() {
        assertThatThrownBy(() -> withEnvironmentVariable(GNUPGHOME_ENV_KEY, null).execute(() -> underTest.discover())).isInstanceOf(JScdException.class)
            .hasFieldOrPropertyWithValue("problem",
                JSCD_GENERAL_ERROR)
            .hasMessageContaining("Could not determine scdaemon socket file");
    }

    @Test
    public void windows_when_only_env_then_return_its_value_plus_filename_as_uri() throws Exception {
        String envValue = "C:\\Users\\itsme\\.gnupg";
        var expectedResult = Paths.get(envValue, SOCKET_FILE_NAME);
        var actualResult = withEnvironmentVariable(GNUPGHOME_ENV_KEY, envValue).execute(() -> underTest.discover());

        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    public void unix_when_only_env_then_return_its_value_plus_filename_as_uri() throws Exception {
        String envValue = "/home/itsme/.gnupg";
        var expectedResult = Paths.get(envValue, SOCKET_FILE_NAME);
        var actualResult = withEnvironmentVariable(GNUPGHOME_ENV_KEY, envValue).execute(() -> underTest.discover());

        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    public void when_only_env_and_value_cannot_be_converted_to_uri_then_jscdexception() {
        String envValue = "#?=)(/&";
        assertThatThrownBy(() -> withEnvironmentVariable(GNUPGHOME_ENV_KEY, envValue).execute(() -> underTest.discover())).isInstanceOf(JScdException.class)
            .hasFieldOrPropertyWithValue("problem",
                JSCD_GENERAL_ERROR)
            .hasMessageContaining("Converting value '" + envValue + File.separator + SOCKET_FILE_NAME + "' to path caused: "
                + pretty(new InvalidPathException(envValue, "Illegal char <?> at index 1")));
    }

    @Test
    public void windows_when_only_prop_then_return_its_value_as_uri() {
        String propValue = "C:\\Users\\itsme\\.gnupg\\" + SOCKET_FILE_NAME;
        var expectedResult = Paths.get(propValue);
        System.setProperty(JSCDLIB_SOCKET_FILE_PROP_KEY, propValue);
        var actualResult = underTest.discover();

        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    public void unix_when_only_prop_then_return_its_value_as_uri() {
        String propValue = "/home/itsme/.gnupg/" + SOCKET_FILE_NAME;
        var expectedResult = Paths.get(propValue);
        System.setProperty(JSCDLIB_SOCKET_FILE_PROP_KEY, propValue);
        var actualResult = underTest.discover();

        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    public void when_only_prop_and_value_cannot_be_converted_to_uri_then_jscdexception() {
        String propValue = "#?=)(/&";
        System.setProperty(JSCDLIB_SOCKET_FILE_PROP_KEY, propValue);
        assertThatThrownBy(() -> withEnvironmentVariable(GNUPGHOME_ENV_KEY, propValue).execute(() -> underTest.discover())).isInstanceOf(JScdException.class)
            .hasFieldOrPropertyWithValue("problem",
                JSCD_GENERAL_ERROR)
            .hasMessageContaining("Converting value '" + propValue + "' to path caused: "
                + pretty(new InvalidPathException(propValue, "Illegal char <?> at index 1")));
    }

    @Test
    public void prop_overrides_env_if_both_exist() throws Exception {
        var propValue = "propValue";
        var envValue = "envValue";
        var expectedResult = Paths.get(propValue);
        System.setProperty(JSCDLIB_SOCKET_FILE_PROP_KEY, propValue);

        var actualResult = withEnvironmentVariable(GNUPGHOME_ENV_KEY, envValue).execute(() -> underTest.discover());

        assertThat(actualResult).isEqualTo(expectedResult);
    }
}
