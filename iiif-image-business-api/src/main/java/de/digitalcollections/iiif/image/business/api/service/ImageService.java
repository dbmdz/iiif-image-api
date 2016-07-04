package de.digitalcollections.iiif.image.business.api.service;

import de.digitalcollections.iiif.image.model.api.Image;
import de.digitalcollections.iiif.image.model.api.ImageInfo;
import de.digitalcollections.iiif.image.model.api.RegionParameters;
import de.digitalcollections.iiif.image.model.api.ResizeParameters;
import de.digitalcollections.iiif.image.model.api.RotationParameters;
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
