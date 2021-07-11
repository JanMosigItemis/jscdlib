package de.itemis.mosig.jassuan.jscdlib.internal;

import static de.itemis.mosig.fluffy.tests.java.exceptions.ThrowablePrettyfier.pretty;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.itemis.mosig.jassuan.jscdlib.problem.JScdException;
import de.itemis.mosig.jassuan.jscdlib.problem.JScdProblems;

public final class JScdSocketDiscovery {

    private static final Logger LOG = LoggerFactory.getLogger(JScdSocketDiscovery.class);

    private static final String JSCDLIB_SOCKET_FILE_PROP_KEY = "jscdlib.socket.file";
    private static final String GNUPGHOME_ENV_KEY = "GNUPGHOME";
    private static final String SOCKET_FILE_NAME = "S.scdaemon";

    public Path discover() {
        Path result = null;
        var rawResultPath = System.getProperty(JSCDLIB_SOCKET_FILE_PROP_KEY);
        if (rawResultPath == null) {
            rawResultPath = System.getenv(GNUPGHOME_ENV_KEY);
            if (rawResultPath == null) {
                LOG.error("Neither system property '" + JSCDLIB_SOCKET_FILE_PROP_KEY + "' nor environment variable '" + GNUPGHOME_ENV_KEY
                    + "' are set. Cannot determine scdaemon socket file name.");
                throw new JScdException(JScdProblems.JSCD_GENERAL_ERROR, "Could not determine scdaemon socket file");
            } else {
                LOG.debug("System property '" + JSCDLIB_SOCKET_FILE_PROP_KEY + "' is unset. Using value of environment variable '"
                    + GNUPGHOME_ENV_KEY + "' as dir of scdaemon socket file.");
                rawResultPath = rawResultPath + File.separator + SOCKET_FILE_NAME;
            }
        } else {
            LOG.debug("System property '" + JSCDLIB_SOCKET_FILE_PROP_KEY + "' set to '" + rawResultPath + "'. Using as scdaemon socket file.");
        }

        try {
            result = Paths.get(rawResultPath);
            LOG.debug("Using '" + result + "' as scdaemon socket file.");
        } catch (Exception e) {
            throw new JScdException(JScdProblems.JSCD_GENERAL_ERROR,
                "Converting value '" + rawResultPath + "' to path caused: " + pretty(e));
        }

        return result;
    }
}
