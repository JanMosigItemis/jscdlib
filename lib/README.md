# jAssuan
This is a direct 1:1 Java "port" of [libassuan](https://gnupg.org/software/libassuan/index.html).  
  
It has been created by the means of [jextract](https://openjdk.java.net/projects/panama) and therefore *requires JDK17*. 

## Build
Make sure, mvn is run with a JDK17. If in doubt, use `JAVA_HOME=/path/to/jdk17 mvn clean install`

## Usage
The main entry class is `de.itemis.mosig.jassuan.assuan_h`. It contains all functions known from the original `assuan.h`.  
  
However, in order to use it, it may be required to change line 13 of `de.itemis.mosig.jassuan.assuan_h.java`. This line contains the JNI conformant name of libassuan, installed in the system, where jassuan is used. This detail makes the code platform dependent and is a known issue of `jextract`. Hopefully it will be fixed with the final release of JDK17.  
  
In case you are using the Java module system, you need to specify `requires de.itemis.mosig.jassuan` in your module descriptor.
