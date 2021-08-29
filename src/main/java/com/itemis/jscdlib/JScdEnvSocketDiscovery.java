package com.itemis.jscdlib;

import static com.itemis.fluffyj.exceptions.ThrowablePrettyfier.pretty;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itemis.jscdlib.problem.JScdException;
import com.itemis.jscdlib.problem.JScdProblems;

/**
 * <p>
 * Use the environment to discover scdaemon's socket file path.
 * </p>
 * <p>
 * System properties have priority over environment variables, i. e. if both are available, the
 * system property wins.
 * </p>
 * <p>
 * Supported values:
 * <ul>
 * <li>System property {@value #JSCDLIB_SOCKET_FILE_PROP_KEY} - Absolute path to socket file</li>
 * <li>Environment variable {@value #GNUPGHOME_ENV_KEY} - Will be appended with
 * {@value #SOCKET_FILE_NAME} to form a path to a probable socket file.</li>
 * </ul>
 * </p>
 */
public final class JScdEnvSocketDiscovery implements JScdSocketDiscovery {

    private static final Logger LOG = LoggerFactory.getLogger(JScdEnvSocketDiscovery.class);

    /**
     * System property that must hold the absolute path to the socket file. Supersedes
     * {@link #GNUPGHOME_ENV_KEY}.
     */
    public static final String JSCDLIB_SOCKET_FILE_PROP_KEY = "jscdlib.socket.file";

    /**
     * Name of environment variable that holds the path to the GPG installation root. Is superseded
     * by {@link #JSCDLIB_SOCKET_FILE_PROP_KEY}
     */
    public static final String GNUPGHOME_ENV_KEY = "GNUPGHOME";

    /**
     * Expected default name of scdaemon's socket file.
     */
    public static final String SOCKET_FILE_NAME = "S.scdaemon";

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
