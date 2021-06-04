package de.itemis.mosig.jassuan.jscdlib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello world!
 */
public class JScdLib {
    private static final Logger LOG = LoggerFactory.getLogger(JScdLib.class);
    
    public static void main(String[] args) {
        var msg = "Hello World!";
        System.out.println("Sysout: " + msg);
        LOG.info("Logger: " + msg);
    }
}
