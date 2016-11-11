package de.digitalcollections.iiif.image.backend.impl.repository.v2;

import de.digitalcollections.iiif.image.backend.impl.repository.jpegtran.v2.JpegTranImage;
import de.digitalcollections.iiif.image.model.api.enums.ImageBitDepth;
import de.digitalcollections.iiif.image.model.api.enums.ImageFormat;
import de.digitalcollections.iiif.image.model.api.exception.InvalidParametersException;
import de.digitalcollections.iiif.image.model.api.exception.ResolvingException;
import de.digitalcollections.iiif.image.model.api.exception.UnsupportedFormatException;
import de.digitalcollections.iiif.image.model.api.v2.Image;
import de.digitalcollections.iiif.image.model.api.v2.RegionParameters;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.stereotype.Repository;

@Repository(value = "ImageRepositoryJpegTranImpl-v2.0.0")
public class ImageRepositoryJpegTranImpl extends AbstractImageRepositoryImpl implements PriorityOrdered {

  private static final Logger LOGGER = LoggerFactory.getLogger(ImageRepositoryJpegTranImpl.class);

  @Override
  protected Image createImage(String identifier, RegionParameters regionParameters) throws InvalidParametersException, ResolvingException, UnsupportedFormatException, IOException {
    byte[] imageData = getImageData(identifier);
    if ((imageData[0] & 0xFF) != 0xFF || (imageData[1] & 0xFF) != 0xD8) {
      throw new UnsupportedFormatException("Not a JPEG file");
    }
    JpegTranImage jpegTranImage = new JpegTranImage(imageData);
    return jpegTranImage;
  }

  @Override
  public Set<ImageFormat> getSupportedInputFormats() {
    Set<ImageFormat> formats = new HashSet<>();
    formats.add(ImageFormat.JPEG);
    return formats;
  }

  @Override
  public Set<ImageFormat> getSupportedOutputFormats() {
    Set<ImageFormat> formats = new HashSet<>();
    formats.add(ImageFormat.JPEG);
    return formats;
  }

  @Override
  public Set<ImageBitDepth> getSupportedBitDepths() {
    return new HashSet<>();
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }
}
