package de.itemis.jassuan.jscdlib.internal;

import static de.itemis.fluffyj.exceptions.ThrowablePrettyfier.pretty;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.itemis.jassuan.jscdlib.JScdSocketDiscovery;
import de.itemis.jassuan.jscdlib.problem.JScdException;
import de.itemis.jassuan.jscdlib.problem.JScdProblems;

public final class JScdSocketDiscoveryFallback implements JScdSocketDiscovery {

    private static final Logger LOG = LoggerFactory.getLogger(JScdSocketDiscoveryFallback.class);

    private static final String JSCDLIB_SOCKET_FILE_PROP_KEY = "jscdlib.socket.file";
    private static final String GNUPGHOME_ENV_KEY = "GNUPGHOME";
    private static final String SOCKET_FILE_NAME = "S.scdaemon";

    @Override
    public Path discover() {
        Path result = null;
        LOG.debug("Trying system property '" + JSCDLIB_SOCKET_FILE_PROP_KEY + "'");
        var rawResultPath = System.getProperty(JSCDLIB_SOCKET_FILE_PROP_KEY);
        if (rawResultPath == null) {
            LOG.debug("Not found. Trying environment variable '" + GNUPGHOME_ENV_KEY + "'");
            rawResultPath = System.getenv(GNUPGHOME_ENV_KEY);
            if (rawResultPath == null) {
                LOG.error("Neither system property '" + JSCDLIB_SOCKET_FILE_PROP_KEY + "' nor environment variable '" + GNUPGHOME_ENV_KEY
                    + "' are set. Cannot determine scdaemon socket file name.");
                throw new JScdException(JScdProblems.JSCD_GENERAL_ERROR, "Could not determine scdaemon socket file");
            } else {
                LOG.debug("Using value of environment variable '"
                    + GNUPGHOME_ENV_KEY + "' as dir of scdaemon socket file.");
                rawResultPath = rawResultPath + File.separator + SOCKET_FILE_NAME;
            }
        } else {
            LOG.debug("Found system property '" + JSCDLIB_SOCKET_FILE_PROP_KEY + "' set to '" + rawResultPath + "'.");
        }

        try {
            result = Paths.get(rawResultPath);
            if (Files.isRegularFile(result)) {
                LOG.debug("Found valid socket file: " + result);
            } else {
                LOG.error(result + " is not a valid socket file.");
                throw new FileNotFoundException(result + " is not a valid socket file.");
            }
        } catch (Exception e) {
            throw new JScdException(JScdProblems.JSCD_GENERAL_ERROR,
                "Converting value '" + rawResultPath + "' to path caused: " + pretty(e));
        }

        return result;
    }
}
