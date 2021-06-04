module de.itemis.mosig.jassuan.demo {
    /*
     * Incubators are not resolved by default and thus must be explicitly specified here. The
     * foreign incubator holds code to access 'foreign' (i. e. non Java) code and memory.
     */
    requires jdk.incubator.foreign;
    requires de.itemis.mosig.jassuan;
}
