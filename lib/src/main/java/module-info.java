module de.itemis.mosig.jassuan {
    /*
     * Incubators are not resolved by default and thus must be explicitly specified here. The
     * foreign incubator holds code to access 'foreign' (i. e. non Java) code and memory.
     */
    requires jdk.incubator.foreign;
	exports de.itemis.mosig.jassuan;
}

