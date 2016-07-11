package de.digitalcollections.iiif.image.backend.impl.repository.v2_0_0;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.digitalcollections.iiif.image.backend.api.repository.v2_0_0.ImageRepository;
import de.digitalcollections.iiif.image.backend.api.resolver.ImageResolver;
import de.digitalcollections.iiif.image.model.api.exception.InvalidParametersException;
import de.digitalcollections.iiif.image.model.api.exception.ResolvingException;
import de.digitalcollections.iiif.image.model.api.exception.UnsupportedFormatException;
import de.digitalcollections.iiif.image.model.api.v2_0_0.Image;
import de.digitalcollections.iiif.image.model.api.v2_0_0.ImageInfo;
import de.digitalcollections.iiif.image.model.api.v2_0_0.RegionParameters;
import de.digitalcollections.iiif.image.model.impl.v2_0_0.ImageInfoImpl;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import javax.imageio.ImageIO;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

public abstract class AbstractImageRepositoryImpl implements ImageRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractImageRepositoryImpl.class);
  @Autowired
  private ApplicationContext applicationContext;
  private boolean forceJpeg;

  private final Cache<String, byte[]> httpCache;
  private final Executor httpExecutor;

  @Autowired(required = true)
  List<ImageResolver> resolvers;

  public AbstractImageRepositoryImpl() {
    httpExecutor = Executor.newInstance();
    httpCache = CacheBuilder.newBuilder().maximumSize(32).build();
  }

  protected byte[] convertToJpeg(byte[] data) throws IOException {
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

  private String getCacheKey(String identifier) {
    return String.format("iiif.imagedata.%s", identifier);
  }

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

  protected byte[] getImageData(String identifier) throws ResolvingException {

    byte[] imageData = null;

    LOGGER.debug("Try to get image data for: " + identifier);

    LOGGER.debug("START getImageData() for " + identifier);
    ImageResolver resolver = getImageResolver(identifier);
    URI imageUri = resolver.getURI(identifier);
    LOGGER.info("URI for {} is {}", identifier, imageUri.toString());
    try {
      if (imageUri.getScheme().equals("file")) {
        imageData = IOUtils.toByteArray(imageUri);
      } else if (imageUri.getScheme().startsWith("http")) {
        String cacheKey = getCacheKey(identifier);
        imageData = httpCache.getIfPresent(cacheKey);
        if (imageData == null) {
          LOGGER.debug("HTTP Cache miss!");
          imageData = httpExecutor.execute(Request.Get(imageUri)).returnContent().asBytes();
          httpCache.put(cacheKey, imageData);
        } else {
          LOGGER.info("HTTP Cache hit!");
        }
      } else if (imageUri.getScheme().equals("classpath")) {
        Resource resource = applicationContext.getResource(imageUri.toString());
        InputStream is = resource.getInputStream();
        imageData = IOUtils.toByteArray(is);
      }
    } catch (IOException e) {
      LOGGER.warn("Could not read from {}", imageUri.toString());
      throw new ResolvingException(e);
    }
    LOGGER.debug("DONE getImageData() for " + identifier);

    if (imageData == null || imageData.length == 0) {
      throw new ResolvingException("No image data for identifier: " + identifier);
    }

    if (forceJpeg) {
      try {
        imageData = convertToJpeg(imageData);
      } catch (IOException e) {
        LOGGER.error("JPEG conversion failed", e);
        throw new ResolvingException("Error converting " + identifier + "to JPEG.");
      }
    }

    return imageData;
  }

  @Override
  public ImageInfo getImageInfo(String identifier) throws UnsupportedFormatException, UnsupportedOperationException {
    try {
      // FIXME do not get whole image just for image infos... use imageio reader:
      // see getImageDimension in DzpIiifPresentationRepositoryImpl
      Image image = getImage(identifier, null);
      if (image != null) {
        ImageInfo imageInfo = new ImageInfoImpl();
        imageInfo.setFormat(image.getFormat());
        imageInfo.setHeight(image.getHeight());
        imageInfo.setWidth(image.getWidth());
        return imageInfo;
      }
    } catch (InvalidParametersException ipe) {
      // as region == null params can not be invalid
    }
    return null;
  }

  private ImageResolver getImageResolver(String identifier) throws ResolvingException {
    for (ImageResolver resolver : resolvers) {
      if (resolver.isResolvable(identifier)) {
        String msg = identifier + " resolved with this resolver: " + resolver.getClass().
                getSimpleName();
        LOGGER.debug(msg);
        return resolver;
      }
    }
    String msg = "No resolver found for identifier '" + identifier + "'";
    throw new ResolvingException(msg);
  }

  public boolean isForceJpeg() {
    return forceJpeg;
  }

  public void setForceJpeg(boolean forceJpeg) {
    this.forceJpeg = forceJpeg;
  }
}
