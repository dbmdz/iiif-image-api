package de.digitalcollections.iiif.image.backend.impl.repository.v2_0_0;

import de.digitalcollections.iiif.image.backend.impl.cache.BufferedImageCache;
import de.digitalcollections.iiif.image.backend.impl.cache.PersistenceException;
import de.digitalcollections.iiif.image.backend.impl.repository.imageio.v2_0_0.JAIImage;
import de.digitalcollections.iiif.image.model.api.v2_0_0.Image;
import de.digitalcollections.iiif.image.model.api.v2_0_0.RegionParameters;
import de.digitalcollections.iiif.image.model.api.enums.ImageBitDepth;
import de.digitalcollections.iiif.image.model.api.enums.ImageFormat;
import de.digitalcollections.iiif.image.model.api.exception.ResolvingException;
import de.digitalcollections.iiif.image.model.api.exception.UnsupportedFormatException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.StopWatch;

@Repository(value = "ImageRepositoryImageIoImpl")
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
  public Set<ImageFormat> getSupportedInputFormats() {
    Set<ImageFormat> formats = new HashSet<>();
    for (String supportedFormat : ImageIO.getReaderFormatNames()) {
      try {
        formats.add(JAIImage.getFormatFromString(supportedFormat));
      } catch (UnsupportedFormatException ignored) {
      }
    }
    return formats;
  }

  @Override
  public Set<ImageFormat> getSupportedOutputFormats() {
    Set<ImageFormat> formats = new HashSet<>();
    for (String supportedFormat : ImageIO.getWriterFormatNames()) {
      try {
        formats.add(JAIImage.getFormatFromString(supportedFormat));
      } catch (UnsupportedFormatException ignored) {
      }
    }
    return formats;
  }

  @Override
  public Set<ImageBitDepth> getSupportedBitDepths() {
    HashSet<ImageBitDepth> supported = new HashSet<>();
    supported.add(ImageBitDepth.BITONAL);
    supported.add(ImageBitDepth.GRAYSCALE);
    supported.add(ImageBitDepth.COLOR);
    return supported;
  }
}
