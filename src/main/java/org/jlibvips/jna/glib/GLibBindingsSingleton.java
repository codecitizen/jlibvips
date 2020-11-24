package org.jlibvips.jna.glib;

import com.sun.jna.Native;

public class GLibBindingsSingleton {

    private static final String ENV_GLIBC_PATH = "JLIBVIPS_GLIBC_PATH";
    private static String libraryPath = System.getenv(ENV_GLIBC_PATH) == null
            ? "/usr/local/opt/glib/lib/libglib-2.0.dylib"
            : System.getenv(ENV_GLIBC_PATH);

    public static void configure(String lp) {
        libraryPath = lp;
    }

    private static GLibBindings INSTANCE;

    public static GLibBindings instance() {
        if(INSTANCE == null) {
            INSTANCE = Native.load(libraryPath, GLibBindings.class);
        }
        return INSTANCE;
    }

    private GLibBindingsSingleton() {
    }

}
