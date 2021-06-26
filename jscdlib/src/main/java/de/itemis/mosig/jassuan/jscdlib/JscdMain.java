package de.itemis.mosig.jassuan.jscdlib;

public class JscdMain {
    public static void main(String[] args) {
        var s = JScdLib.createHandle(new FlaNativeImpl());
        var availableReaders = s.listReaders();
        System.out.println("List of available smart card readers:");
        if (availableReaders.isEmpty()) {
            System.out.println("\tNone available");
        } else {
            for (int i = 0; i < availableReaders.size(); i++) {
                System.out.println("\t" + (i + 1) + ") " + availableReaders.get(i));
            }
        }
    }
}
