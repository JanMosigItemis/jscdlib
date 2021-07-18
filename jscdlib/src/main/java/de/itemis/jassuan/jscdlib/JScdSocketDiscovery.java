package de.itemis.jassuan.jscdlib;

import java.nio.file.Path;

import de.itemis.jassuan.jscdlib.problem.JScdException;

/**
 * Some JScdLib functions need a path to scdaemon's socket file. Instances of this interface know
 * how to provide one.
 */
public interface JScdSocketDiscovery {

    /**
     * @return A valid {@link Path} to scdaemon's socket file.
     * @throws JScdException if no path could be discovered.
     */
    Path discover();
}
