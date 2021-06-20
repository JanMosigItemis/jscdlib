package de.itemis.mosig.jassuan.jscdlib;

public class JscdMain {
    public static void main(String[] args) {
        var s = JScdLib.createHandle(new FlaNativeImpl());
        s.listReaders().forEach(System.out::println);
    }
}
