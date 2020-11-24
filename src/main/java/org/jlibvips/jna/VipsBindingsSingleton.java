package org.jlibvips.jna;

import com.sun.jna.Native;

public class VipsBindingsSingleton {


    private static String libraryPath = "vips";

    public static void configure(String lp) {
        libraryPath = lp;
    }

    private static VipsBindings INSTANCE;

    public static VipsBindings instance() {
        if(INSTANCE == null) {
            if(libraryPath == null || libraryPath.isEmpty()) {
                throw new IllegalStateException("Please call VipsBindingsSingleton.configure(...) before getting the instance.");
            }
            try {
                INSTANCE = Native.load(libraryPath, VipsBindings.class);
            } catch (UnsatisfiedLinkError e) {
                libraryPath = guessPath();
                INSTANCE = Native.load(libraryPath, VipsBindings.class);
            }
        }
        return INSTANCE;
    }

    private static String guessPath() {
        return "/usr/local/Cellar/vips/8.10.2_4/lib/libvips.dylib";
    }

    private VipsBindingsSingleton() {
    }

}
