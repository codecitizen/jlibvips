package org.jlibvips;

/**
 * Java representation of <code>VipsAngle</code> enum from libvips
 *
 * @see <a href="http://libvips.github.io/libvips/API/current/libvips-conversion.html#VipsAngle">VipsAngle</a>
 * @author amp
 */
public enum VipsAngle {
    D0,
    D90,
    D180,
    D270,
    Last;

    /**
     * Creates a {@link VipsAngle} based on degrees as integer.
     *
     * @param value degrees
     * @return the {@link VipsAngle}
     */
    public static VipsAngle fromInteger(int value) {
        switch (value) {
            case 0: return D0;
            case 90: return D90;
            case 180: return D180;
            case 270: return D270;
            default:
                throw new IllegalArgumentException("Allowed VipsAngle's are [0, 90, 180, 270].");
        }
    }


    public double toDouble() {
        switch (this) {
            case D90:
                return 90.0;
            case D180:
                return 180.0;
            case D270:
                return 270.0;
            default:
                return 0.0;
        }
    }
}
