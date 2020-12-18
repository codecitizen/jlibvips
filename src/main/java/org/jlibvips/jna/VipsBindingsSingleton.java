package org.jlibvips.jna;

import com.sun.jna.Native;

public class VipsBindingsSingleton {

    private static final String ENV_LIB_PATH = "JLIBVIPS_LIB_PATH";
    private static String libraryPath = System.getenv(ENV_LIB_PATH) == null ? "vips" : System.getenv(ENV_LIB_PATH);

    public static void configure(String lp) {
        libraryPath = lp;
    }

    private static VipsBindings INSTANCE;

    public static VipsBindings instance() {
        if(INSTANCE == null) {
            if(libraryPath == null || libraryPath.isEmpty()) {
                throw new IllegalStateException("Please call VipsBindingsSingleton.configure(...) or set env var JLIBVIPS_LIB_PATH before getting the instance.");
            }
            try {
                INSTANCE = Native.load(libraryPath, VipsBindings.class);
            } catch (UnsatisfiedLinkError e) {
                throw new IllegalStateException("Please call VipsBindingsSingleton.configure(...) or set env var JLIBVIPS_LIB_PATH before getting the instance.");
            }
        }
        return INSTANCE;
    }

    private VipsBindingsSingleton() {
    }

}
