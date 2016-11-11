package de.digitalcollections.iiif.image.backend.api.repository.v2;

import de.digitalcollections.iiif.image.model.api.v2.Image;
import de.digitalcollections.iiif.image.model.api.v2.ImageInfo;
import de.digitalcollections.iiif.image.model.api.v2.RegionParameters;
import de.digitalcollections.iiif.image.model.api.enums.ImageBitDepth;
import de.digitalcollections.iiif.image.model.api.enums.ImageFormat;
import de.digitalcollections.iiif.image.model.api.exception.InvalidParametersException;
import de.digitalcollections.iiif.image.model.api.exception.UnsupportedFormatException;
import java.util.Set;

public interface ImageRepository {

  public ImageInfo getImageInfo(String identifier) throws UnsupportedFormatException, UnsupportedOperationException;

  public Image getImage(String identifier, RegionParameters regionParameters)
          throws InvalidParametersException, UnsupportedOperationException, UnsupportedFormatException;

  public Set<ImageFormat> getSupportedInputFormats();

  public Set<ImageFormat> getSupportedOutputFormats();

  public Set<ImageBitDepth> getSupportedBitDepths();
}
