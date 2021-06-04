package de.itemis.mosig.jassuan.demo;

import org.junit.jupiter.api.Test;

public class JAssuanDemoTest {

    // @Test
    // public void testAssuanManual() {
    // System.out.println(System.getProperty("java.library.path"));
    // JAssuanDemo.assuan_manual();
    // }

    @Test
    public void testAssuanGenerated() {
        JAssuanDemo.assuan_generated();
    }
}
