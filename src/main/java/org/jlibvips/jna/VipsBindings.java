package org.jlibvips.jna;

import com.sun.jna.Library;
import com.sun.jna.Pointer;

public interface VipsBindings extends Library {

    Pointer vips_image_new_from_file(String fileName, Object...args);
    Pointer vips_image_new_from_buffer(byte[] buf, long length, String optionString, Object...args);

    int vips_pdfload(String fileName, Pointer[] pointer, Object...args);

    int vips_image_get_width(Pointer pointer);
    int vips_image_get_height(Pointer pointer);

    int vips_image_get_bands(Pointer pointer);

    int vips_dzsave(Pointer pointer, String outDir, Object...args);

    int vips_thumbnail_image(Pointer in, Pointer[] out, int width, Object...args);

    Pointer vips_array_double_new(double[] array, int n);

    int vips_vipssave(Pointer in, String filename, Object...args);
    int vips_jpegsave(Pointer in, String filename, Object...args);
    int vips_webpsave(Pointer in, String filename, Object...args);
    int vips_pngsave(Pointer in, String filename, Object...args);
    int vips_tiffsave(Pointer in, String filename, Object...args);

    int vips_insert(Pointer main, Pointer sub, Pointer[] out, int x, int y, Object...args);
    int vips_join(Pointer in1, Pointer in2, Pointer[] out, int direction, Object...args);

    int vips_draw_rect1(Pointer image, double ink, int left, int top, int width, int height, Object...args);
    int vips_draw_rect(Pointer image, Pointer ink, int n, int left, int top, int width, int height, Object...args);

    int vips_extract_area(Pointer image, Pointer[] out, int left, int top, int width, int height, Object...args);
    int vips_resize(Pointer in, Pointer[] out, double scale, Object...args);

    int vips_reduce(Pointer in, Pointer[] out, double hshrink, double vshrink, Object...args);
    int vips_embed(Pointer image, Pointer[] out, int x, int y, int width, int height, Object...args);
    int vips_composite2(Pointer base, Pointer overlay, Pointer[] out, int mode, Object...args);
    int vips_merge(Pointer ref, Pointer sec, Pointer[] out, int direction, int dx, int dy, Object...args);
    int vips_rotate(Pointer in, Pointer[] out, double angle, Object...args);
    int vips_similarity(Pointer in, Pointer[] out, Object...args);
    int vips_colourspace(Pointer in, Pointer[] out, Object space, Object... args);
    boolean vips_colourspace_issupported(Pointer in);
    int vips_bandand(Pointer in, Pointer[] out, Object... args);
    int vips_ifthenelse(Pointer cond, Pointer in1, Pointer in2, Pointer[] out, Object... args);
    Pointer vips_image_new_from_image(Pointer image, double[] c, int n);
    int vips_maplut(Pointer in, Pointer[] out, Pointer lut, Object... args);
    int vips_identity(Pointer[] out, Object... args);
    int vips_relational_const(Pointer in, Pointer[] out, Object relational, double[] c, int n, Object... args);
    int vips_relational_const1(Pointer in, Pointer[] out, Object relational, double c, Object... args);
    int vips_black(Pointer[] out, Integer width, Integer height, Object...args);

    int vips_linear(Pointer in, Pointer[] out, double[] a, double[] b, Integer n, Object... args);
    int vips_linear1(Pointer in, Pointer[] out, double a, double b, Object... args);
    int vips_bandjoin_const(Pointer in, Pointer[] out, double[] c, int n, Object... args);
    int vips_flatten(Pointer in, Pointer[] out, Object...args);
    int vips_cast(Pointer in, Pointer[] out, Object format, Object...args);
    int vips_image_get_format(Pointer in);
    int vips_image_get_interpretation(Pointer in);
    double vips_image_get_xres(Pointer in);
    double vips_image_get_yres(Pointer in);
    int vips_image_get_xoffset(Pointer in);
    int vips_image_get_yoffset(Pointer in);


    /**
     * When you are done with an image, use <code>g_object_unref()</code> to dispose of it. If you pass an image to an
     * operation and that operation needs to keep a copy of the image, it will ref it. So you can unref an image as soon
     * as you no longer need it, you don't need to hang on to it in case anyone else is still using it.
     *
     * @see <a href="https://developer.gnome.org/gobject/stable/gobject-The-Base-Object-Type.html#g-object-unref">g_object_unref()</a>
     */
    void g_object_unref(Pointer object);

    int vips_copy(Pointer in, Pointer[] out, Object...args);
}
