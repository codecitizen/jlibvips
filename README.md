# jlibvips

A Java interface to [llibvips](http://libvips.github.io/libvips/), the fast image processing library with low memory needs.

**Dependency:**

```xml
<dependency>
  <groupId>io.github.codecitizen</groupId>
  <artifactId>jlibvips</artifactId>
  <version>1.3.0.RELEASE</version>
</dependency>
```

```groovy
implementation 'io.github.codecitizen:jlibvips:1.3.0.RELEASE'
```

**Configure Path to libvips Library:**

From code:
```java
VipsBindingsSingleton.configure("/usr/local/lib/libvips.so");
```

You may also set the lib path in environment variable `JLIBVIPS_LIB_PATH`, which is useful 
when running your app in multiple environments where lib position changes. Example for macOS:
```shell script
export JLIBVIPS_LIB_PATH="/usr/local/Cellar/vips/8.10.2_4/lib/libvips.dylib"
```

**Example: Generate a Thumbnail for a PDF.**

```java
package jlibvips.example;

import org.jlibvips.*;
import java.nio.file.Paths;
import java.nio.file.Files;

public class PDFThumbnailExample {
    
    public static void main(String[] args) {
        var image = VipsImage.formPdf(Paths.get(args[0]), 0); // Second Parameter is the Page Number
        var thumbnail = image.thumbnail()
            .autoRotate()
            .create();
        image.unref();
        var thumbnailFile = thumbnail.jpeg()
            .quality(100)
            .strip()
            .save();
        thumbnail.unref();
        
        System.out.printf("Thumbnail generated in '%s'.%n", thumbnailFile.toString());                
        System.out.println("Done!");
    }
    
}
```

**Example: Create a DZ Image Pyramid from a large PNG File.**


```java
package jlibvips.example;

import org.jlibvips.VipsImage;
import java.nio.file.Paths;

public class ImagePyramidExample {
    
    public static void main(String[] args) {
        var image = VipsImage.fromFile(Paths.get(args[0]));
        var directory = Files.createTempDirectory("example-pyramid");
        image.deepZoom(directory)
            .layout(DeepZoomLayouts.Google)
            .container(DeepZoomContainer.FileSystem)
            .suffix(".jpg[Q=100]")
            .save();
        image.unref();
        System.out.printf("Pyramid generated in folder '%s'.\n", directory.toString());
        System.out.println("Done.");
    }
    
}
```

**Example: Create a TIFF Image Pyramid from a large PNG File.**

```java
package jlibvips.example;

import org.jlibvips.VipsImage;
import java.nio.file.Paths;

public class ImagePyramidExample {
    
    public static void main(String[] args) {
        var image = VipsImage.fromFile(Paths.get(args[0]));
        Path dest = image
                .tiff()
                .tile(true)
                .tileHeight(256)
                .tileWidth(256)
                .compression(VipsForeignTiffCompression.VIPS_FOREIGN_TIFF_COMPRESSION_JPEG)
                .quality(80)
                .pyramid(true)
                .save();
        image.unref();
        System.out.printf("Pyramid generated in file '%s'.\n", dest.toString());
        System.out.println("Done.");
    }
    
}
```

**Example: Logging**

```java
package jlibvips.example;

import org.jlibvips.*;
import org.jlibvips.jna.glib.*;
import java.util.List;

public class LoggingExample {
  public static void main(String[] args) {
    // 1) Configure GLib JNA Mappings
    GLibBindingsSingleton.configure("path/to/glibc");
    // 2) Register Log Handler
    VipsImage.registerLogHandler(
            List.of(GLogLevelFlags.G_LOG_LEVEL_INFO,
                    GLogLevelFlags.G_LOG_LEVEL_DEBUG),
            (flag, message) -> System.out.printf("VIPS[%s]: %s", flag, message)
    );
    var image = VipsImage.formPdf(Paths.get(args[0]), 0); // Second Parameter is the Page Number
    var thumbnail = image.thumbnail()
        .autoRotate()
        .create();
    image.unref();
    thumbnail.jpeg()
        .quality(100)
        .strip()
        .save();
    thumbnail.unref();
  }
}
```

Should deliver an Output like:

```
VIPS[G_LOG_LEVEL_INFO]: selected loader is image source
VIPS[G_LOG_LEVEL_INFO]: input size is 500 x 500
VIPS[G_LOG_LEVEL_INFO]: converting to processing space scrgb
VIPS[G_LOG_LEVEL_INFO]: shrinkv by 2
VIPS[G_LOG_LEVEL_INFO]: shrinkh by 2
VIPS[G_LOG_LEVEL_INFO]: residual reducev by 0.4
VIPS[G_LOG_LEVEL_INFO]: reducev: 16 point mask
VIPS[G_LOG_LEVEL_INFO]: residual reduceh by 0.4
VIPS[G_LOG_LEVEL_INFO]: reduceh: 16 point mask
VIPS[G_LOG_LEVEL_INFO]: cropping to 100x100
VIPS[G_LOG_LEVEL_INFO]: convi: using C path
VIPS[G_LOG_LEVEL_INFO]: residual reducev by 0.32
VIPS[G_LOG_LEVEL_INFO]: reducev: 7 point mask
VIPS[G_LOG_LEVEL_INFO]: residual reduceh by 0.32
VIPS[G_LOG_LEVEL_INFO]: reduceh: 7 point mask
VIPS[G_LOG_LEVEL_INFO]: gaussblur mask width 17
VIPS[G_LOG_LEVEL_INFO]: convi: using C path
VIPS[G_LOG_LEVEL_INFO]: convi: using C path
```

The GLIBC lib path can also be set using env variable `JLIBVIPS_GLIBC_PATH`.