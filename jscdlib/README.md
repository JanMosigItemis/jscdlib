# jScdLib
A library that makes use of [FLA](https://openjdk.java.net/jeps/389) to be able to directly speak to scdaemon. This may be useful for client software that deals with security keys based on OpenPGP Smart Card standards.

## Build
mvn clean install

## fluffy-tests required
This project has a dependency to fluffy-tests. You may download it here: [https://github.com/JanMosigItemis/fluffy-tests-java](https://github.com/JanMosigItemis/fluffy-tests-java)

## IDE Setup
* Add `--add-modules=jdk.incubator.foreign` to the startup JVM options of your IDE (e. g. eclipse.ini).
* Add the following to JVM options of tests in order to be able to run them from within the IDE:
```-Dforeign.restricted=permit --add-modules=jdk.incubator.foreign```
