package org.jlibvips.operations;

import org.jlibvips.*;
import org.jlibvips.exceptions.VipsException;
import org.jlibvips.jna.VipsBindingsSingleton;
import org.jlibvips.util.Varargs;
import org.jlibvips.util.VipsUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Write a VIPS image to a file as TIFF.
 */
public class TiffSaveOperation implements SaveOperation {

    private final VipsImage image;

    private Integer quality;
    private VipsForeignTiffCompression tiffCompression;
    private Boolean tile;
    private Integer tileWidth;
    private Integer tileHeight;
    private Boolean pyramid;
    private VipsForeignTiffPredictor predictor;
    private String profile;
    private Integer bitdepth;
    private Boolean miniswhite;
    private VipsForeignTiffResunit resunit;
    private Double xres;
    private Double yres;
    private Boolean bigtiff;
    private Boolean properties;
    private VipsRegionShrink region_shrink;
    private VipsForeignDzDepth depth;
    private Integer level;
    private Boolean lossless;
    private Boolean subifd;

    public TiffSaveOperation(VipsImage image) {
        this.image = image;
    }

    @Override
    public Path save() throws IOException, VipsException {
        if (tileWidth % 128 != 0 || tileHeight % 128 != 0) {
            throw new VipsException("Wrong value for tileWidth or TileHeight", 1);
        }
        Path path = Files.createTempFile("jlibvips", ".tif");
        int ret = VipsBindingsSingleton.instance().vips_tiffsave(image.getPtr(), path.toString(),
                new Varargs().add("Q", quality)
                        .add("compression", VipsUtils.toOrdinal(tiffCompression))
                        .add("tile", VipsUtils.booleanToInteger(tile))
                        .add("tile_width", tileWidth)
                        .add("tile_height", tileHeight)
                        .add("pyramid", VipsUtils.booleanToInteger(pyramid))
                        .add("predictor", VipsUtils.toOrdinal(predictor))
                        .add("profile", profile)
                        .add("bitdepth", bitdepth)
                        .add("miniswhite", miniswhite)
                        .add("resunit", VipsUtils.toOrdinal(resunit))
                        .add("xres", xres)
                        .add("yres", yres)
                        .add("bigtiff", bigtiff)
                        .add("properties", properties)
                        .add("region_shrink", region_shrink)
                        .add("depth", depth)
                        .add("level", level)
                        .add("lossless", lossless)
                        .add("subifd", subifd)
                        .toArray());
        if (ret != 0) {
            throw new VipsException("vips_tiffsave", ret);
        }
        return path;
    }

    /**
     * Set the JPEG compression factor. Default 75.
     *
     * @param q quality factor
     * @return this
     */
    public TiffSaveOperation quality(Integer q) {
        this.quality = q;
        return this;
    }

    /**
     * Use compression to set the tiff compression. Currently jpeg, packbits, fax4,
     * lzw, none, deflate, webp and zstd are supported. The default is no compression.
     * JPEG compression is a good lossy compressor for photographs, packbits is good
     * for 1-bit images, and deflate is the best lossless compression TIFF can do.
     *
     * @param compression compression ENUM
     * @return this
     */
    public TiffSaveOperation compression(VipsForeignTiffCompression compression) {
        this.tiffCompression = compression;
        return this;
    }

    /**
     * Set true to write a tiled tiff
     *
     * @param tile true or false
     * @return this
     */
    public TiffSaveOperation tile(boolean tile) {
        this.tile = tile;
        return this;
    }

    /**
     * Set tile width, must be 2^N, i.e. 128, 256, 512 etc
     * Default is 128.
     *
     * @param tileWidth width of tiles
     * @return this
     */
    public TiffSaveOperation tileWidth(int tileWidth) {
        this.tileWidth = tileWidth;
        return this;
    }

    /**
     * Set tile width, must be 2^N, i.e. 128, 256, 512 etc
     * Default is 128.
     *
     * @param tileHeight height of tiles
     * @return this
     */
    public TiffSaveOperation tileHeight(int tileHeight) {
        this.tileHeight = tileHeight;
        return this;
    }

    /**
     * Set pyramid to write the image as a set of images, one per page, of decreasing size.
     * By default, the pyramid stops when the image is small enough to fit in one tile.
     * Use depth to stop when the image fits in one pixel, or to only write a single layer.
     *
     * @param pyramid true or false
     * @return this
     */
    public TiffSaveOperation pyramid(boolean pyramid) {
        this.pyramid = pyramid;
        return this;
    }

    /**
     * Use predictor to set the predictor for lzw and deflate compression.
     * It defaults to VIPS_FOREIGN_TIFF_PREDICTOR_HORIZONTAL, meaning horizontal
     * differencing. Please refer to the libtiff specifications for further
     * discussion of various predictors.
     *
     * @param predictor enum
     * @return this
     */
    public TiffSaveOperation predictor(VipsForeignTiffPredictor predictor) {
        this.predictor = predictor;
        return this;
    }

    /**
     * Use profile to give the filename of a profile to be embedded in the TIFF.
     * This does not affect the pixels which are written, just the way they are tagged.
     * See vips_profile_load() for details on profile naming.
     * <p>
     * If no profile is specified and the VIPS header contains an ICC profile named
     * VIPS_META_ICC_NAME, the profile from the VIPS header will be attached.
     *
     * @param profile name of profile
     * @return this
     */
    public TiffSaveOperation profile(String profile) {
        this.profile = profile;
        return this;
    }

    /**
     * Set bitdepth to save 8-bit uchar images as 1, 2 or 4-bit TIFFs. In case of
     * depth 1: Values &gt;128 are written as white, values &lt;=128 as black. Normally
     * vips will write MINISBLACK TIFFs where black is a 0 bit, but if you set
     * miniswhite, it will use 0 for a white bit. Many pre-press applications
     * only work with images which use this sense. miniswhite only affects one-bit images,
     * it does nothing for greyscale images. In case of depth 2: The same holds
     * but values &lt; 64 are written as black. For 64 &lt;= values &lt; 128 they are written
     * as dark grey, for 128 &lt;= values &lt; 192 they are written as light gray and values
     * above are written as white. In case miniswhite is set to true this behavior is
     * inverted. In case of depth 4: values &lt; 16 are written as black, and so on for
     * the lighter shades. In case miniswhite is set to true this behavior is inverted.
     *
     * @param bitdepth the bit depth
     * @return this
     */
    public TiffSaveOperation bitdepth(Integer bitdepth) {
        this.bitdepth = bitdepth;
        return this;
    }

    /**
     * Normally vips will write MINISBLACK TIFFs where black is a 0 bit, but if you set
     * miniswhite , it will use 0 for a white bit. Many pre-press applications only work
     * with images which use this sense. miniswhite only affects one-bit images, it does
     * nothing for greyscale images.
     *
     * @param miniswhite true or false
     * @return this
     */
    public TiffSaveOperation miniswhite(boolean miniswhite) {
        this.miniswhite = miniswhite;
        return this;
    }

    /**
     * Use xres and yres to override the default horizontal and vertical resolutions.
     * By default these values are taken from the VIPS image header. libvips resolution
     * is always in pixels per millimetre.
     *
     * @param xres horizontal resolution in pixels/mm
     * @return this
     */
    public TiffSaveOperation xres(Double xres) {
        this.xres = xres;
        return this;
    }

    /**
     * Use xres and yres to override the default horizontal and vertical resolutions.
     * By default these values are taken from the VIPS image header. libvips resolution
     * is always in pixels per millimetre.
     *
     * @param yres horizontal resolution in pixels/mm
     * @return this
     */
    public TiffSaveOperation yres(Double yres) {
        this.yres = yres;
        return this;
    }

    /**
     * Set bigtiff to attempt to write a bigtiff. Bigtiff is a variant of the
     * TIFF format that allows more than 4GB in a file.
     *
     * @param bigtiff true or false
     * @return this
     */
    public TiffSaveOperation bigtiff(boolean bigtiff) {
        this.bigtiff = bigtiff;
        return this;
    }

    /**
     * Set properties to write all vips metadata to the IMAGEDESCRIPTION tag as xml.
     * If properties is not set, the value of VIPS_META_IMAGEDESCRIPTION is used instead.
     *
     * @param properties set TRUE to write an IMAGEDESCRIPTION tag
     * @return this
     */
    public TiffSaveOperation properties(boolean properties) {
        this.properties = properties;
        return this;
    }

    /**
     * Use region_shrink to set how images will be shrunk when generating pyramid:
     * by default each 2x2 block is just averaged, but you can set MODE or MEDIAN as well.
     *
     * @param region_shrink How to shrink each 2x2 region.
     * @return this
     */
    public TiffSaveOperation region_shrink(VipsRegionShrink region_shrink) {
        this.region_shrink = region_shrink;
        return this;
    }

    /**
     * By default, the pyramid stops when the image is small enough to fit in one tile.
     * Use depth to stop when the image fits in one pixel, or to only write a single layer.
     *
     * @param depth how deep to make the pyramid
     * @return this
     */
    public TiffSaveOperation depth(VipsForeignDzDepth depth) {
        this.depth = depth;
        return this;
    }

    /**
     * Use level to set the ZSTD compression level.
     *
     * @param level Zstd compression level
     * @return this
     */
    public TiffSaveOperation level(Integer level) {
        this.level = level;
        return this;
    }

    /**
     * Use lossless to set WEBP lossless mode on
     *
     * @param lossless WebP losssless mode
     * @return this
     */
    public TiffSaveOperation lossless(boolean lossless) {
        this.lossless = lossless;
        return this;
    }

    /**
     * Set subifd to save pyramid layers as sub-directories of the main image.
     * Setting this option can improve compatibility with formats like OME.
     *
     * @param subifd set TRUE to write pyr layers as sub-ifds
     * @return this
     */
    public TiffSaveOperation subifd(boolean subifd) {
        this.subifd = subifd;
        return this;
    }

    /**
     * Use resunit to override the default resolution unit.
     * The default resolution unit is taken from the header field
     * VIPS_META_RESOLUTION_UNIT. If this field is not set, then
     * VIPS defaults to cm.
     *
     * @param resunit set resolution unit
     * @return this
     */
    public TiffSaveOperation resunit(VipsForeignTiffResunit resunit) {
        this.resunit = resunit;
        return this;
    }
}
