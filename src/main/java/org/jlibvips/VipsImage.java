package org.jlibvips;

import com.sun.jna.Pointer;
import org.jlibvips.exceptions.CouldNotLoadPdfVipsException;
import org.jlibvips.exceptions.VipsException;
import org.jlibvips.jna.VipsBindings;
import org.jlibvips.jna.VipsBindingsSingleton;
import org.jlibvips.jna.glib.GLibBindingsSingleton;
import org.jlibvips.jna.glib.GLibLogHandler;
import org.jlibvips.jna.glib.GLogLevelFlags;
import org.jlibvips.operations.*;
import org.jlibvips.util.Varargs;
import org.jlibvips.util.VipsUtils;

import java.io.Closeable;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * An image, residing in memory or disk, managed by libvips. Instance methods provide transformations and queries on
 * it.
 * <p>
 * When loading simple images from the file system, call {@link VipsImage#fromFile(Path)}.
 * <p>
 * For large vectorised PDFs we recommend {@link VipsImage#fromPdf(Path)}.
 *
 * @author amp
 */
public class VipsImage implements Closeable {

    /**
     * This is the maximum resolution to which libvips can convert vectorised PDFs.
     * <p>
     * libvips builds on libpoppler which uses cairographics to draw the vectors on a canvas. cairos canvas is limited
     * by 32767x32767 rows and therefore PDF pages loaded as {@link VipsImage} cannot be larger than that.
     *
     * @see <a href="https://poppler.freedesktop.org/">libpoppler</a>
     * @see <a href="https://www.cairographics.org/">cairo</a>
     * @see <a href="https://gitlab.freedesktop.org/poppler/poppler/issues/511">Gitlab Issue</a>
     */
    public static final int POPPLER_CAIRO_LIMIT = 32767;

    public static VipsImage fromPdf(Path p, int page, float scale) {
        // Due to excessive testing we set 6 to be the maximum scale parameter and decrease by 0.1 until we reach a
        // scale working with the limit.
        VipsImage image = null;
        do {
            Pointer[] ptr = new Pointer[1];
            int ret = VipsBindingsSingleton.instance().vips_pdfload(p.toString(), ptr, "scale", scale, "page", page, null);
            if (ret != 0) {
                throw new CouldNotLoadPdfVipsException(ret);
            }
            if (image != null) {
                image.unref();
            }
            image = new VipsImage(ptr[0]);
            scale -= 0.1f;
        } while (image.getWidth() > POPPLER_CAIRO_LIMIT || image.getHeight() > POPPLER_CAIRO_LIMIT);
        return image;
    }

    /**
     * Loads a PDF document's page as {@link VipsImage} in the greatest possible resolution.
     *
     * @param p    {@link Path} to the PDF document.
     * @param page page number (starting at 0)
     * @return the PDF page as {@link VipsImage}.
     */
    public static VipsImage fromPdf(Path p, int page) {
        return fromPdf(p, page, 6.0f);
    }

    /**
     * Loads a PDF document's page as {@link VipsImage} in the greatest possible resolution.
     *
     * @param p {@link Path} to the PDF document.
     * @return the PDF page as {@link VipsImage}.
     */
    public static VipsImage fromPdf(Path p) {
        return fromPdf(p, 1);
    }

    /**
     * Creates a new {@link VipsImage} from a {@link Path} to an image or PDF file.
     *
     * @param p {@link Path} to image file.
     * @return {@link VipsImage} representation of file.
     * @see <a href="http://libvips.github.io/libvips/API/current/VipsImage.html#vips-image-new-from-file">Native Function</a>
     */
    public static VipsImage fromFile(Path p) {
        Pointer ptr = VipsBindingsSingleton.instance().vips_image_new_from_file(p.toString());
        return new VipsImage(ptr);
    }

    /**
     * Loads a {@link VipsImage} from the given String's content. Handy for quickly drawing SVGs.
     *
     * @param string image content
     * @return the {@link VipsImage}
     */
    public static VipsImage fromString(String string) {
        if (string == null)
            return null;
        byte[] buffer = string.getBytes();
        Pointer ptr = VipsBindingsSingleton.instance().vips_image_new_from_buffer(buffer, buffer.length, "");
        return new VipsImage(ptr);
    }

    public static VipsImage newBlack(Integer width, Integer height, Integer bands) {
        Pointer[] out = new Pointer[1];
        int ret = VipsBindingsSingleton.instance()
                .vips_black(out, width, height, new Varargs().add("bands", bands).toArray());
        if (ret == -1) {
            throw new VipsException("vips_black", ret);
        }
        return new VipsImage(out[0]);
    }

    public VipsImage newFromImage(double[] value) {
        Pointer ptr = VipsBindingsSingleton.instance().vips_image_new_from_image(this.ptr, value, value.length);
        return new VipsImage(ptr);
    }

    public static VipsIdentityOperation identity() {
        return new VipsIdentityOperation();
    }

    public ColorspaceOperation colorspace() {
        return new ColorspaceOperation(this);
    }

    public CopyOperation copy() {
        return new CopyOperation(this);
    }

    public CastOperation cast() {
        return new CastOperation(this);
    }

    public VipsImage cast(VipsBandFormat format) {
        return cast().cast(format);
    }

    public LinearOperation linear() {
        return new LinearOperation(this);
    }

    public VipsImage linear(double a, double b) {
        return linear().linear(a, b);
    }

    public VipsImage linear(double[] a, double[] b) {
        return linear().linear(a, b, a.length);
    }

    public RelationalOperation relationalOperation() {
        return new RelationalOperation(this);
    }

    public MaplutOperation maplut() {
        return new MaplutOperation(this);
    }

    public VipsImage eq(double... c) {
        return relationalOperation().relational_const(VipsOperationRelational.EQUAL, c);
    }

    public VipsImage notEq(double... c) {
        return relationalOperation().relational_const(VipsOperationRelational.NOTEQ, c);
    }

    public VipsImage less(double... c) {
        return relationalOperation().relational_const(VipsOperationRelational.LESS, c);
    }

    public VipsImage lessEq(double... c) {
        return relationalOperation().relational_const(VipsOperationRelational.LESSEQ, c);
    }

    public VipsImage more(double... c) {
        return relationalOperation().relational_const(VipsOperationRelational.MORE, c);
    }

    public VipsImage moreEq(double[] c) {
        return relationalOperation().relational_const(VipsOperationRelational.MOREEQ, c);
    }

    public VipsImage moreEq(double c) {
        return relationalOperation().relational_const(VipsOperationRelational.MOREEQ, c);
    }

    public VipsImage last(double... c) {
        return relationalOperation().relational_const(VipsOperationRelational.LAST, c);
    }

    public IfThenElseOperation ifThenElseOperation() {
        return new IfThenElseOperation(this);
    }

    public VipsImage ifthenelse(VipsImage inThen, VipsImage inElse, Boolean blend) {
        return ifThenElseOperation().ifthenelse(inThen, inElse, blend);
    }

    public VipsImage ifthenelse(VipsImage inThen, VipsImage inElse) {
        return ifThenElseOperation().ifthenelse(inThen, inElse, null);
    }

    public VipsImage ifthenelse(double[] inThen, double[] inElse) {
        return ifThenElseOperation().ifthenelse(newFromImage(inThen), newFromImage(inElse));
    }

    public VipsImage maplut(VipsImage lut) {
        return maplut().maplut(lut, null);
    }

    public BandJoinOperation bandjoin() {
        return new BandJoinOperation(this);
    }

    private Pointer toNBands(int bands) {
        int current = getBands();

        if (current >= bands) return this.ptr;

        int len = bands - current;
        double[] c = new double[len];
        for (int i = 0; i < len; i++) c[i] = 255;

        return bandjoin().bandjoin_const(c).ptr;
    }

    public VipsImage removeColor(double[] rgba) {
        VipsBindings vips = VipsBindingsSingleton.instance();

        Pointer base = toNBands(4);

        Pointer[] preCond = new Pointer[1];
        int retPreCond = vips.vips_relational_const(base, preCond, VipsUtils.toOrdinal(VipsOperationRelational.MOREEQ), rgba, 4);
        if (retPreCond == -1)
            return null;

        Pointer[] cond = new Pointer[1];
        int retCond = vips.vips_bandand(preCond[0], cond);
        if (retCond == -1)
            return null;

        double[] transColor = new double[]{0, 0, 0, 0};
        Pointer transp = vips.vips_image_new_from_image(this.ptr, transColor, 4);

        Pointer[] out = new Pointer[1];
        int ret = vips.vips_ifthenelse(cond[0], transp, base, out);
        if (ret == -1)
            return null;

        return new VipsImage(out[0]);
    }

    /**
     * Registers a new logging handler for the libvips GLib logs.
     *
     * @param levels          the subscribed {@link org.jlibvips.jna.glib.GLogLevelFlags}.
     * @param loggingFunction the logging function taking the flag as first and log message as second parameter.
     */
    public static void registerLogHandler(List<GLogLevelFlags> levels, BiConsumer<GLogLevelFlags, String> loggingFunction) {
        int flags = levels.stream()
                .map(GLogLevelFlags::getVal)
                .reduce((l1, l2) -> l1 | l2).orElse(GLogLevelFlags.G_LOG_LEVEL_DEBUG.getVal());
        GLibLogHandler handler = (d, f, m, p) -> loggingFunction.accept(GLogLevelFlags.fromVal(f), m);
        GLibBindingsSingleton.instance()
                .g_log_set_handler("VIPS", flags, handler, null);
    }

    private final Pointer ptr;

    /**
     * Initializes a new {@link VipsImage} with a pointer to a native <code>VipsImage</code> struct.
     *
     * @param ptr {@link Pointer} to <code>VipsImage</code> struct
     * @see <a href="http://libvips.github.io/libvips/API/current/VipsImage.html">VipsImage struct</a>
     */
    public VipsImage(final Pointer ptr) {
        this.ptr = ptr;
    }

    /**
     * Get the pointer to the <code>VipsImage</code> struct.
     *
     * @return {@link Pointer} to <code>VipsImage</code> struct
     * @see <a href="http://libvips.github.io/libvips/API/current/VipsImage.html">VipsImage struct</a>
     */
    public Pointer getPtr() {
        return ptr;
    }

    /**
     * Save an image as a set of tiles at various resolutions.
     *
     * <code>
     *     image.deepZoom(outDir)
     *      .layout(DeepZoomLayout.Google)
     *      .save()
     * </code>
     *
     * @param outDir {@link Path} to the output directory.
     * @return the {@link DeepZoomOperation}
     * @see <a href="http://libvips.github.io/libvips/API/current/VipsForeignSave.html#vips-dzsave">vips_dzsave()</a>
     */
    public DeepZoomOperation deepZoom(Path outDir) {
        return new DeepZoomOperation(this, outDir);
    }

    /**
     * Make a thumbnail of this {@link VipsImage}.
     *
     * <code>
     *     var thumbnail = image.thumbnail(100)
     *      .autoRotate()
     *      .create();
     * </code>
     *
     * @param width Fixed width in pixel of the new {@link VipsImage}
     * @return the {@link ThumbnailOperation}
     * @see <a href="http://libvips.github.io/libvips/API/current/libvips-resample.html#vips-thumbnail">vips_thumbnail()</a>
     * @see <a href="http://libvips.github.io/libvips/API/current/libvips-resample.html#vips-thumbnail-image">vips_thumbnail_image()</a>
     */
    public ThumbnailOperation thumbnail(int width) {
        return new ThumbnailOperation(this, width);
    }

    /**
     * Write an image to a file in Vips Image format.
     *
     * <code>
     *     java.nio.Path = image.v().save();
     * </code>
     *
     * <a href="http://libvips.github.io/libvips/API/current/VipsForeignSave.html#vips-vipssave">vips_vipssave</a>
     *
     * @return the {@link VipsSaveOperation}
     */
    public VipsSaveOperation v() {
        return new VipsSaveOperation(this);
    }

    /**
     * Write an image to a file in WebP format.
     *
     * <code>
     *     java.nio.Path path = image.webp().quality(100).save();
     * </code>
     *
     * <a href="http://libvips.github.io/libvips/API/current/VipsForeignSave.html#vips-webpsave">vips_webpsave()</a>
     *
     * @return the {@link WebpSaveOperation}
     */
    public WebpSaveOperation webp() {
        return new WebpSaveOperation(this);
    }

    /**
     * Write an image to a file in JPEG format.
     *
     * <code>
     *     java.nio.Path path = image.jpeg().quality(100).save();
     * </code>
     *
     * <a href="http://libvips.github.io/libvips/API/current/VipsForeignSave.html#vips-jpegsave">vips_jpegsave()</a>
     *
     * @return the {@link JpegSaveOperation}
     */
    public JpegSaveOperation jpeg() {
        return new JpegSaveOperation(this);
    }

    /**
     * Write an image to a file in PNG format.
     *
     * <code>
     *     java.nio.Path path = image.png().quality(100).save();
     * </code>
     *
     * <a href="http://libvips.github.io/libvips/API/current/VipsForeignSave.html#vips-pngsave">vips_pngsave()</a>
     *
     * @return the {@link PngSaveOperation}
     */
    public PngSaveOperation png() {
        return new PngSaveOperation(this);
    }

    /**
     * Write an image to a file in TIFF format.
     *
     * <code>
     *     java.nio.Path path = image.tiff().save();
     * </code>
     *
     * <a href="http://libvips.github.io/libvips/API/current/VipsForeignSave.html#vips-tiffsave">vips_tiffsave()</a>
     *
     * @return the {@link TiffSaveOperation}
     */
    public TiffSaveOperation tiff() {
        return new TiffSaveOperation(this);
    }

    /**
     * Get the width of this image.
     *
     * @return width in pixel
     * @see <a href="http://libvips.github.io/libvips/API/current/libvips-header.html#vips-image-get-width">vips_image_get_width()</a>
     */
    public int getWidth() {
        return VipsBindingsSingleton.instance().vips_image_get_width(ptr);
    }

    /**
     * Get the height of this image.
     *
     * @return height in pixel
     * @see <a href="http://libvips.github.io/libvips/API/current/libvips-header.html#vips-image-get-height">vips_image_get_height()</a>
     */
    public int getHeight() {
        return VipsBindingsSingleton.instance().vips_image_get_height(ptr);
    }

    /**
     * Get the bands of this image.
     *
     * @return number of bands
     * @see <a href="http://libvips.github.io/libvips/API/current/libvips-header.html#vips-image-get-bands">http://libvips.github.io/libvips/API/current/libvips-header.html#vips-image-get-bands()</a>
     */
    public int getBands() {
        return VipsBindingsSingleton.instance().vips_image_get_bands(ptr);
    }

    public VipsInterpretation getInterpretation() {
        int value = VipsBindingsSingleton.instance().vips_image_get_interpretation(ptr);
        return VipsInterpretation.fromValue(value)
                .orElseGet(() -> VipsInterpretation.ERROR);
    }

    public VipsBandFormat getFormat() {
        int ordinal = VipsBindingsSingleton.instance().vips_image_get_format(ptr);
        return VipsBandFormat.values()[ordinal];
    }

    public int xoffset() {
        return VipsBindingsSingleton.instance().vips_image_get_xoffset(ptr);
    }

    public int yoffset() {
        return VipsBindingsSingleton.instance().vips_image_get_xoffset(ptr);
    }

    public double xres() {
        return VipsBindingsSingleton.instance().vips_image_get_xres(ptr);
    }

    public double yres() {
        return VipsBindingsSingleton.instance().vips_image_get_yres(ptr);
    }

    /**
     * Insert sub into main at position.
     *
     * <a href="https://jcupitt.github.io/libvips/API/current/libvips-conversion.html#vips-insert">https://jcupitt.github.io/libvips/API/current/libvips-conversion.html#vips-insert</a>
     *
     * @param sub small image
     * @return the {@link VipsInsertOperation}
     */
    public VipsInsertOperation insert(VipsImage sub) {
        return new VipsInsertOperation(this, sub);
    }

    public VipsJoinOperation join(VipsImage other) {
        return new VipsJoinOperation(this, other);
    }

    /**
     * Paint pixels within left , top , width , height in image with ink . If fill is zero, just paint a 1-pixel-wide
     * outline.
     *
     * <a href="https://jcupitt.github.io/libvips/API/current/libvips-draw.html#vips-draw-rect">vips_draw_rect()</a>
     *
     * @return the {@link DrawRectOperation}
     */
    public DrawRectOperation rect() {
        return new DrawRectOperation(this);
    }

    /**
     * Extract an area from an image. The area must fit within in.
     *
     * <a href="https://jcupitt.github.io/libvips/API/current/libvips-conversion.html#vips-extract-area">vips_extract_area</a>
     *
     * @param left left edge of area to extract
     * @param top top edge of area to extract
     * @param width width of area to extract
     * @param height height of area to extract
     * @return the extracted {@link VipsImage}
     */
    public VipsImage extractArea(int left, int top, int width, int height) {
        Pointer[] out = new Pointer[1];
        int ret = VipsBindingsSingleton.instance().vips_extract_area(this.ptr, out, left, top, width, height);
        if(ret != 0) {
            throw new VipsException("vips_extract_area", ret);
        }
        return new VipsImage(out[0]);
    }

    /**
     * Resize an image.
     *
     * <a href="https://jcupitt.github.io/libvips/API/current/libvips-resample.html#vips-resize">vips_resize</a>
     *
     * @param scale scale factor
     * @return the {@link VipsResizeOperation}
     */
    public VipsResizeOperation resize(double scale) {
        return new VipsResizeOperation(this.ptr, scale);
    }

    /**
     * Reduce input by a pair of factors with a pair of 1D kernels. This will not work well for shrink factors greater than three.
     *
     * <a href="https://jcupitt.github.io/libvips/API/current/libvips-resample.html#vips-reduce">vips-reduce</a>
     *
     * @param hshrink horizontal shrink
     * @param vshrink vertical shrink
     * @return the {@link VipsReduceOperation}
     */
    public VipsReduceOperation reduce(double hshrink, double vshrink) {
        return new VipsReduceOperation(this.ptr, hshrink, vshrink);
    }

    public VipsEmbedOperation embed(int x, int y, int width, int height) {
        return new VipsEmbedOperation(this.ptr, x, y, width, height);
    }

    public Composite2Operation composite(VipsImage overlay, VipsBlendMode mode) {
        return new Composite2Operation(this.ptr, overlay.ptr, mode);
    }

    public VipsImage merge(VipsImage sec, VipsDirection direction, int dx, int dy) {
        Pointer[] out = new Pointer[1];
        int ret = VipsBindingsSingleton.instance()
                .vips_merge(this.ptr, sec.ptr, out, direction.ordinal(), dx, dy);
        if(ret != 0) {
            throw new VipsException("vips_merge", ret);
        }
        return new VipsImage(out[0]);
    }

    /**
     * Rotate the image.
     *
     * http://libvips.github.io/libvips/API/current/libvips-resample.html#vips-rotate
     *
     * @param angle {@link VipsAngle}
     * @return {@link VipsRotateOperation}
     */
    public VipsRotateOperation rotate(VipsAngle angle) {
        return new VipsRotateOperation(this, angle);
    }

    public SimilarityOperation similarity() {
        return new SimilarityOperation(this);
    }

    public FlattenOperation flatten() {
        return new FlattenOperation(this);
    }

    /**
     * When you are done with this image, use <code>unref()</code> to dispose of it.
     *
     * @see <a href="https://jcupitt.github.io/libvips/API/current/using-from-c.html">Reference counting</a>
     */
    public void unref() {
        VipsBindingsSingleton.instance().g_object_unref(ptr);
    }

    @Override
    public void close() {
        this.unref();
    }

    @Override
    public String toString() {
        return String.format("VipsImage { %dx%d %s, %d bands, %s }",
                getWidth(), getHeight(), getFormat(), getBands(), getInterpretation());
    }
}
