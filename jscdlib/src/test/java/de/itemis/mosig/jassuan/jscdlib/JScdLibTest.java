package de.itemis.mosig.jassuan.jscdlib;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.junit.jupiter.api.Test;

import de.itemis.mosig.fluffyj.tests.FluffyTestHelper;

public class JScdLibTest {

    @Test
    public void test_jscdlib_is_final() {
        FluffyTestHelper.assertFinal(JScdLib.class);
    }

    @Test
    public void t() throws Exception {
        var p = new ProcessBuilder("gpgconf", "--list-dirs").start();

        try (BufferedReader output = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            output.lines().forEach(System.out::println);
        }

        if (p.isAlive()) {
            p.destroyForcibly();
        }
    }
}
