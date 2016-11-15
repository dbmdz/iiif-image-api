package de.digitalcollections.iiif.image.backend.impl.repository.v2;

import de.digitalcollections.iiif.image.backend.impl.cache.BufferedImageCache;
import de.digitalcollections.iiif.image.backend.impl.cache.PersistenceException;
import de.digitalcollections.iiif.image.backend.impl.repository.imageio.v2.JAIImage;
import de.digitalcollections.iiif.image.model.api.enums.ImageBitDepth;
import de.digitalcollections.iiif.image.model.api.enums.ImageFormat;
import de.digitalcollections.iiif.image.model.api.exception.ResolvingException;
import de.digitalcollections.iiif.image.model.api.exception.UnsupportedFormatException;
import de.digitalcollections.iiif.image.model.api.v2.Image;
import de.digitalcollections.iiif.image.model.api.v2.RegionParameters;
import de.digitalcollections.iiif.image.model.api.v2.ResizeParameters;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.StopWatch;

@Repository(value = "ImageRepositoryImageIoImpl-v2.0.0")
public class ImageRepositoryImageIoImpl extends AbstractImageRepositoryImpl {

  private static final Logger LOGGER = LoggerFactory.getLogger(ImageRepositoryImageIoImpl.class);

  @Autowired
  private BufferedImageCache bufferedImageCache;

  @Override
  protected Image createImage(String identifier, RegionParameters regionParameters) throws ResolvingException, UnsupportedFormatException, IOException {
    StopWatch sw = new StopWatch();
    final JAIImage image;
    try {
      if (regionParameters != null && regionParameters.isAbsolute()) {
        // getting absolute region from cached byte array is faster
        sw.start();
        byte[] imageData = getImageData(identifier);
        image = new JAIImage(imageData, regionParameters);
        sw.stop();
      } else if (bufferedImageCache != null && bufferedImageCache.isCached(identifier)) {
        // region parameters are null or relative (cropping is done later in service)
        sw.start();
        BufferedImage bi = bufferedImageCache.get(identifier);
        image = new JAIImage(bi, "JPG");
        sw.stop();
        LOGGER.
                info("{} ms: got image {} from bufferedImageCache {}", sw.getLastTaskTimeMillis(), identifier, bufferedImageCache.
                        getCacheDir());
      } else {
        sw.start();
        byte[] imageData = getImageData(identifier);
        image = new JAIImage(imageData);
        if (bufferedImageCache != null) {
          bufferedImageCache.put(identifier, image.getImage());
        }
        sw.stop();
        LOGGER.info("{} ms: created image {} and put into bufferedImageCache", sw.
                getLastTaskTimeMillis(), identifier);
      }
    } catch (PersistenceException ex) {
      throw new IOException(ex);
    }
    return image;
  }

  @Override
  public boolean supportsInputFormat(ImageFormat inFormat) {
    return Stream.of(ImageIO.getReaderFormatNames())
        .filter(name -> {
          try {
            return JAIImage.getFormatFromString(name).equals(inFormat);
          } catch (UnsupportedFormatException e) {
            return false;
          }
        })
        .findFirst()
        .isPresent();
  }

  @Override
  public boolean supportsOutputFormat(ImageFormat outFormat) {
    return Stream.of(ImageIO.getWriterFormatNames())
        .filter(name -> {
          try {
            return JAIImage.getFormatFromString(name).equals(outFormat);
          } catch (UnsupportedFormatException e) {
            return false;
          }
        })
        .findFirst()
        .isPresent();
  }

  @Override
  public boolean supportsCropOperation(RegionParameters region) {
    // No limitations on cropping
    return true;
  }

  @Override
  public boolean supportsScaleOperation(Dimension imageDims, ScaleParameters scaleParams) {
    // No limitations on scaling
    return true;
  }

  @Override
  public boolean supportsBitDepth(ImageBitDepth bitDepth) {
    switch (bitDepth) {
      case BITONAL:
      case GRAYSCALE:
      case COLOR:
        return true;
      default:
        return false;
    }
  }
}
