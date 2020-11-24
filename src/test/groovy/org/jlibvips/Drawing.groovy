package org.jlibvips


import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.StandardCopyOption

import static org.jlibvips.TestUtils.copyResourceToFS

class Drawing extends Specification {

    def "Draw a colored rect with SVG."() {
        given:
        def svg = """<svg height="100" width="100">
    <rect width="300" height="100" style="fill:rgb(0,0,255);stroke-width:3;stroke:rgb(0,0,0)" />
  </svg>"""
        def imagePath = copyResourceToFS "500x500.jpg"
        def image = VipsImage.fromFile imagePath
        when:
        def svgImage = VipsImage.fromString svg
        def newImage = image.insert(svgImage)
                .x(10)
                .y(10)
                .create()
        then:
        newImage != null
        cleanup:
        image.unref()
        svgImage.unref()
        newImage.unref()
        Files.deleteIfExists imagePath
    }

    def "Convert colored image to b-w."() {
        given:
        def imagePath = copyResourceToFS "900x700.png"
        def image = VipsImage.fromFile imagePath
        when:
        def bwImage = image
                .colorspace().space(VipsInterpretation.B_W)
        then:
        bwImage != null
        cleanup:
        Files.deleteIfExists imagePath
    }

    def "Convert white to transparent."() {
        given:
        def imagePath = copyResourceToFS "900x700.png"
        def image = VipsImage.fromFile imagePath
        when:
        def bwImage = image.removeColor(0xFFAAAAAA)
        then:
        bwImage != null
        cleanup:
        Files.deleteIfExists imagePath
    }


    def "Should tint."() {
        given:
        def imagePath = copyResourceToFS "fuel.png"
        def image = VipsImage.fromFile imagePath
        def whiteRgb = color(255, 255, 255)
        def transparent = color(0, 0, 0, 0)
        def dir = Files.createTempDirectory("tint")
        when:
        def lut = VipsImage.identity().create()
        lut = lut.moreEq(200).ifthenelse(lut.newFromImage(transparent), lut.newFromImage(tint))
                .copy().interpretation(VipsInterpretation.sRGB).copy()
        def tinted = image
                .flatten().background(whiteRgb).flatten()
                .colorspace().space(VipsInterpretation.B_W)
                .maplut(lut);

        then:
        tinted != null
        println tinted
        def dest = dir.resolve( "${colorName}.png")
        Files.move(tinted.png().save(), dest, StandardCopyOption.REPLACE_EXISTING)
        cleanup:
        Files.deleteIfExists imagePath
        where:
        colorName | tint
        "orange"  | color(255, 165, 0, 255)
        "pink"    | color(255, 192, 203, 255)
        "violet"  | color(238, 130, 238, 255)
        "yellow"  | color(255, 255, 0, 255)
        "red"     | color(255, 0, 0, 255)
        "green"   | color(0, 128, 0, 255)
        "blue"    | color(0, 0, 255, 255)
        "black"   | color(0, 0, 0, 255)
        "white"   | color(255, 255, 255, 255)

    }

    double[] color(Double... c) {
        return c
    }
}
