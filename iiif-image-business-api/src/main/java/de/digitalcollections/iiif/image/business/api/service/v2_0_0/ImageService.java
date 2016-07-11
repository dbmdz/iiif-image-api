package de.digitalcollections.iiif.image.business.api.service.v2_0_0;

import de.digitalcollections.iiif.image.model.api.v2_0_0.Image;
import de.digitalcollections.iiif.image.model.api.v2_0_0.ImageInfo;
import de.digitalcollections.iiif.image.model.api.v2_0_0.RegionParameters;
import de.digitalcollections.iiif.image.model.api.v2_0_0.ResizeParameters;
import de.digitalcollections.iiif.image.model.api.v2_0_0.RotationParameters;
import de.digitalcollections.iiif.image.model.api.enums.ImageBitDepth;
import de.digitalcollections.iiif.image.model.api.enums.ImageFormat;
import de.digitalcollections.iiif.image.model.api.exception.InvalidParametersException;
import de.digitalcollections.iiif.image.model.api.exception.UnsupportedFormatException;

/**
 * Service providing image processing functionality.
 */
public interface ImageService {

  ImageInfo getImageInfo(String identifier) throws UnsupportedFormatException, UnsupportedOperationException;

  Image processImage(String identifier, RegionParameters regionParameters, ResizeParameters sizeParameters, RotationParameters rotationParameters, ImageBitDepth bitDepthParameter, ImageFormat formatParameter)
          throws InvalidParametersException, UnsupportedOperationException, UnsupportedFormatException;

}
