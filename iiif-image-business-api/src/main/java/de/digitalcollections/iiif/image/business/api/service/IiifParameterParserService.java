package de.digitalcollections.iiif.image.business.api.service;

import de.digitalcollections.iiif.image.model.api.RegionParameters;
import de.digitalcollections.iiif.image.model.api.ResizeParameters;
import de.digitalcollections.iiif.image.model.api.RotationParameters;
import de.digitalcollections.iiif.image.model.api.enums.ImageBitDepth;
import de.digitalcollections.iiif.image.model.api.enums.ImageFormat;
import de.digitalcollections.iiif.image.model.api.exception.InvalidParametersException;
import de.digitalcollections.iiif.image.model.api.exception.UnsupportedFormatException;

public interface IiifParameterParserService {

  ImageFormat parseIiifFormat(String targetFormat) throws UnsupportedFormatException;

  ImageBitDepth parseIiifQuality(String targetQuality) throws InvalidParametersException;

  RegionParameters parseIiifRegion(String region) throws InvalidParametersException;

  /**
   * The rotation parameter specifies mirroring and rotation. A leading exclamation mark (“!”) indicates that the image
   * should be mirrored by reflection on the vertical axis before any rotation is applied. The numerical value
   * represents the number of degrees of clockwise rotation, and may be any floating point number from 0 to 360.
   *
   * @param rotation n: The degrees of clockwise rotation from 0 up to 360., !n: The image should be mirrored and then
   * rotated.
   * @return rotation params
   * @throws InvalidParametersException
   */
  RotationParameters parseIiifRotation(String rotation) throws InvalidParametersException;

  ResizeParameters parseIiifSize(String size) throws InvalidParametersException;

}
