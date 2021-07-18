package de.itemis.jassuan.jscdlib;

public class JscdMain {
    public static void main(String[] args) {
        System.out.println("assuan_socket_connect: " + (0x6008050 & 0xFFFF) + " " + (32768 | 80));
        try (var scardHandle = JScdLib.constructSCardHandle(); var assuanHandle = JScdLib.constructAssuanHandle()) {
            var availableReaders = scardHandle.listReaders();
            System.out.println("List of available smart card readers:");
            if (availableReaders.isEmpty()) {
                System.out.println("\tNone available");
            } else {
                for (int i = 0; i < availableReaders.size(); i++) {
                    System.out.println("\t" + (i + 1) + ") " + availableReaders.get(i));
                }
            }

            System.out.println("\nCurrently attached smart card: ");
            assuanHandle.sendCommand("SERIALNO", System.out::println, System.out::println);
            assuanHandle.sendCommand("GETATTR LOGIN-DATA", System.out::println, System.out::println);
        } catch (Exception e) {
            e.printStackTrace();
            var msg = e.getMessage() == null ? "No further information." : e.getMessage();
            System.err.println("Error: " + e.getClass().getSimpleName() + ": " + msg);
        }
    }
}
