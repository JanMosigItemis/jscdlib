package de.itemis.mosig.jassuan;

import static java.net.http.HttpClient.newHttpClient;
import static java.net.http.HttpRequest.newBuilder;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * Unit test for simple App.
 */
public class AppTest {
    private static final Logger LOG = LoggerFactory.getLogger(AppTest.class);

    private WireMockServer wireMockServer;
    private int port = -1;

    @BeforeEach
    public void setUp() {
        wireMockServer = new WireMockServer(new WireMockConfiguration().dynamicPort().usingFilesUnderDirectory("src/test/resources/wiremock"));
        wireMockServer.start();
        port = wireMockServer.port();
    }

    @AfterEach
    public void tearDown() {
        wireMockServer.resetAll();
        wireMockServer.stop();
        port = -1;
    }

    @Test
    public void test_something() throws Exception {
        assertThat(true).isTrue();

        App.main(null);
        LOG.debug("Log from test");

        var request = newBuilder().uri(new URI("http", null, "localhost", port, "/some/thing", null, null)).GET().build();
        var response = newHttpClient().send(request, ofString(StandardCharsets.UTF_8));
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("Hello World!");
    }
}
