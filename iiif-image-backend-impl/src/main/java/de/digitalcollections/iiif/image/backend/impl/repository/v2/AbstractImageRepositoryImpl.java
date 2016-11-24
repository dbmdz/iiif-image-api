package de.digitalcollections.iiif.image.backend.impl.repository.v2;

import de.digitalcollections.core.business.api.ResourceService;
import de.digitalcollections.core.model.api.MimeType;
import de.digitalcollections.core.model.api.resource.Resource;
import de.digitalcollections.core.model.api.resource.enums.ResourcePersistenceType;
import de.digitalcollections.core.model.api.resource.exceptions.ResourceIOException;
import de.digitalcollections.iiif.image.backend.api.repository.v2.ImageRepository;
import de.digitalcollections.iiif.image.model.api.enums.ImageFormat;
import de.digitalcollections.iiif.image.model.api.exception.InvalidParametersException;
import de.digitalcollections.iiif.image.model.api.exception.ResolvingException;
import de.digitalcollections.iiif.image.model.api.exception.UnsupportedFormatException;
import de.digitalcollections.iiif.image.model.api.v2.Image;
import de.digitalcollections.iiif.image.model.api.v2.ImageInfo;
import de.digitalcollections.iiif.image.model.api.v2.RegionParameters;
import de.digitalcollections.iiif.image.model.impl.v2.ImageInfoImpl;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Iterator;
import javax.cache.annotation.CacheResult;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractImageRepositoryImpl implements ImageRepository {
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractImageRepositoryImpl.class);

  private boolean forceJpeg;

  @Autowired
  private ResourceService resourceService;

  private byte[] convertToJpeg(byte[] data) throws IOException {
    if ((data[0] & 0xFF) != 0xFF || (data[1] & 0xFF) != 0xD8) {
      BufferedImage img = ImageIO.read(new ByteArrayInputStream(data));
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      ImageIO.write(img, "JPEG", os);
      return os.toByteArray();
    } else {
      return data;
    }
  }

  protected abstract Image createImage(String identifier, RegionParameters region) throws InvalidParametersException, ResolvingException, UnsupportedFormatException, IOException;

  @Override
  public Image getImage(String identifier, RegionParameters regionParameters) throws InvalidParametersException, UnsupportedOperationException, UnsupportedFormatException {
    Image image;
    try {
      image = createImage(identifier, regionParameters);
    } catch (ResolvingException re) {
      LOGGER.warn("Could not find resolver for {}", identifier, re);
      return null;
    } catch (IOException ioe) {
      LOGGER.error("Could not read image for {}", identifier, ioe);
      return null;
    } catch (IllegalArgumentException arge) {
      throw new InvalidParametersException("Illegal offsets.");
    }
    return image;
  }

  ByteBuffer getImageData(String identifier) throws ResolvingException {
    Resource resource = getImageResource(identifier);
    URI imageUri = resource.getUri();
    LOGGER.info("URI for {} is {}", identifier, imageUri.toString());
    return getImageData(imageUri);
  }

  private Resource getImageResource(String identifier) throws ResolvingException {
    Resource resource;
    try {
      resource = resourceService.get(identifier, ResourcePersistenceType.REFERENCED, MimeType.MIME_IMAGE);
    } catch (ResourceIOException ex) {
      LOGGER.warn("Error getting manifest for identifier " + identifier, ex);
      throw new ResolvingException("No manifest for identifier " + identifier);
    }
    return resource;
  }

  @CacheResult(cacheName = "imageSources")
  private ByteBuffer getImageData(URI imageUri) throws ResolvingException {
    String location = imageUri.toString();
    LOGGER.debug("Trying to get image data from: " + location);

    try {
      byte[] imageData;
      String scheme = imageUri.getScheme();

      InputStream inputStream = resourceService.getInputStream(imageUri);
      imageData = IOUtils.toByteArray(inputStream);

      if (imageData == null || imageData.length == 0) {
        throw new ResolvingException("No image data at location " + location);
      }

      if (forceJpeg) {
        try {
          imageData = convertToJpeg(imageData);
        } catch (IOException e) {
          LOGGER.error("JPEG conversion failed", e);
          throw new ResolvingException("Error converting image from location " + location + "to JPEG.");
        }
      }

      return ByteBuffer.wrap(imageData);
    } catch (IOException ex) {
      LOGGER.warn("Error getting image data from location " + location);
      throw new ResolvingException("No image data for location " + location);
    }
  }

  @Override
  @CacheResult(cacheName="imageInfos")
  public ImageInfo getImageInfo(String identifier) throws UnsupportedFormatException, UnsupportedOperationException {
    ImageInfo imageInfo = null;
    try {
      Resource imageResource = getImageResource(identifier);

      try (ImageInputStream in = ImageIO.createImageInputStream(resourceService.getInputStream(imageResource))) {
        final Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
        if (readers.hasNext()) {
          ImageReader reader = readers.next();
          reader.setInput(in);
          imageInfo = new ImageInfoImpl();
          final String formatName = reader.getFormatName();
          imageInfo.setFormat(ImageFormat.getByExtension(formatName));
          imageInfo.setHeight(reader.getHeight(0));
          imageInfo.setWidth(reader.getWidth(0));
          reader.dispose();
        }
      }
    } catch (IOException | ResolvingException ex) {
      throw new RuntimeException("Could not get image info for image with identifier " + identifier, ex);
    }
    return imageInfo;
  }

  public boolean isForceJpeg() {
    return forceJpeg;
  }

  public void setForceJpeg(boolean forceJpeg) {
    this.forceJpeg = forceJpeg;
  }
}
