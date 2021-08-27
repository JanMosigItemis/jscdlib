package de.itemis.jassuan.jscdlib;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.itemis.jassuan.jscdlib.problem.JScdException;

/**
 * Uses gpgconf to find out the socket dir. Also supports a fallback if not possible.
 */
public final class JScdGpgConfSocketDiscovery implements JScdSocketDiscovery {

    private static final Logger LOG = LoggerFactory.getLogger(JScdGpgConfSocketDiscovery.class);

    private static final String SOCKET_FILE_NAME = "S.scdaemon";
    private static final String SOCKET_DIR_LINE_PREFIX = "socketdir:";

    private final JScdSocketDiscovery fallback;

    /**
     * Create an instance.
     *
     * @param fallback Use this instance if it is not possible to find out via gpgconf.
     */
    public JScdGpgConfSocketDiscovery(JScdSocketDiscovery fallback) {
        this.fallback = requireNonNull(fallback, "fallback");
    }

    @Override
    public Path discover() {
        List<Path> socketDirCandidates = null;
        try {
            LOG.debug("Now trying to call gpgconf in order to find out scdaemon socket dir.");
            var gpgConfProcess = new ProcessBuilder("gpgconf", "--list-dirs").start();

            try (BufferedReader output = new BufferedReader(new InputStreamReader(gpgConfProcess.getInputStream(), UTF_8))) {
                socketDirCandidates = output.lines().filter(line -> line.startsWith(SOCKET_DIR_LINE_PREFIX))
                    .map(line -> {
                        var subLine = line.substring(SOCKET_DIR_LINE_PREFIX.length());
                        var sanitizedSubLine = URLDecoder.decode(subLine, UTF_8);
                        return Paths.get(sanitizedSubLine);
                    }).toList();
            }

            if (gpgConfProcess.isAlive()) {
                gpgConfProcess.destroyForcibly();
            }
        } catch (Throwable t) {
            throw new JScdException(t);
        }

        for (var socketDirCandidate : socketDirCandidates) {
            Path socketFilePath = socketDirCandidate.resolve(SOCKET_FILE_NAME);
            LOG.debug("Identified socket file candidate: " + socketFilePath);
            if (Files.isRegularFile(socketFilePath)) {
                LOG.debug("Found valid socket file: " + socketFilePath);
                return socketFilePath;
            } else {
                LOG.debug("Not a regular file: " + socketFilePath);
            }
        }

        return fallback.discover();
    }
}
