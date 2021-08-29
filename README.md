# jScdLib
A library that may be used to work with [scdaemon](https://www.gnupg.org/documentation/manuals/gnupg/Invoking-SCDAEMON.html#Invoking-SCDAEMON) and [SmartCards](https://en.wikipedia.org/wiki/Smart_card). This may be useful for client software that deals with security keys based on OpenPGP Smart Card standards.

# Build
mvn clean install

# Run
This library requires Java 16

# Demo
See `demo/README.md`

# IDE Setup
Due to the usage of Java16 incubator code, the following special setup is required:  
* Add `--add-modules=jdk.incubator.foreign` to the startup JVM options of your IDE (e. g. eclipse.ini).
* Add the following to JVM options of tests in order to be able to run them from within the IDE:
```-Dforeign.restricted=permit --add-modules=jdk.incubator.foreign```

# Usage
## List available SmartCard readers
```
try (var scardHandle = JScdLib.constructSCardHandle()) {
    scardHandle.listReaders().forEach(System.out::println);
}
```

## Send command to scdaemon
```
try (var assuanHandle = JScdLib.constructAssuanHandle()) {
    // SmartCard must be attached in order for this to show output.
    assuanHandle.sendCommand("SERIALNO", System.out::println, System.out::println);
}
```