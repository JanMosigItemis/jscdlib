# jScdLib
A library that makes use of [FLA](https://openjdk.java.net/jeps/389) to be able to directly speak to scdaemon. This may be useful for client software that deals with security keys based on OpenPGP Smart Card standards.

# IDE Setup
* Add `--add-modules=jdk.incubator.foreign` to the startup JVM options of your IDE (e. g. eclipse.ini).
* Add the following to JVM options of tests in order to be able to run them from within the IDE:
```-Dforeign.restricted=permit --add-modules=jdk.incubator.foreign```
