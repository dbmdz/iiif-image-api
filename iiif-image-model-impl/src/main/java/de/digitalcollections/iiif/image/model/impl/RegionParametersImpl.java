package de.digitalcollections.iiif.image.model.impl;

import de.digitalcollections.iiif.image.model.api.RegionParameters;
import de.digitalcollections.iiif.image.model.api.exception.InvalidParametersException;

public class RegionParametersImpl implements RegionParameters {

  private boolean absolute;
  private float height;
  private float horizontalOffset;
  private float verticalOffset;
  private float width;

  public RegionParametersImpl() {
  }

  public RegionParametersImpl(boolean absolute, float horizontalOffset, float verticalOffset, float width, float height) {
    this.absolute = absolute;
    this.horizontalOffset = horizontalOffset;
    this.verticalOffset = verticalOffset;
    this.width = width;
    this.height = height;
  }

  /**
   * Region is given by percentage scales. Region params are calculated relative to original image size.
   *
   * @param regionParameters
   * @param origWidth
   * @param origHeight
   * @throws de.digitalcollections.iiif.image.model.api.exception.InvalidParametersException
   */
  public RegionParametersImpl(RegionParametersImpl regionParameters, int origWidth, int origHeight) throws InvalidParametersException {
    assert regionParameters != null;
    assert !regionParameters.isAbsolute();

    if (regionParameters.getHorizontalOffset() > origWidth || regionParameters.getVerticalOffset() > origHeight) {
      throw new InvalidParametersException("Either vertical or horizontal offset are outside of the image.");
    }
    this.horizontalOffset = regionParameters.getHorizontalOffset() * (float) origWidth;
    this.verticalOffset = regionParameters.getVerticalOffset() * (float) origHeight;
    this.width = width * (float) origWidth;
    this.height = height * (float) origHeight;
    if (width > (origWidth - horizontalOffset)) {
      this.width = origWidth - horizontalOffset;
    }
    if (height > (origHeight - verticalOffset)) {
      this.height = origHeight - verticalOffset;
    }
  }

  @Override
  public float getHeight() {
    return height;
  }

  @Override
  public float getHorizontalOffset() {
    return horizontalOffset;
  }

  @Override
  public float getVerticalOffset() {
    return verticalOffset;
  }

  @Override
  public float getWidth() {
    return width;
  }

  @Override
  public boolean isAbsolute() {
    return absolute;
  }

  @Override
  public void setAbsolute(boolean absolute) {
    this.absolute = absolute;
  }

  @Override
  public void setHeight(float height) {
    this.height = height;
  }

  @Override
  public void setHorizontalOffset(float horizontalOffset) {
    this.horizontalOffset = horizontalOffset;
  }

  @Override
  public void setVerticalOffset(float verticalOffset) {
    this.verticalOffset = verticalOffset;
  }

  @Override
  public void setWidth(float width) {
    this.width = width;
  }

}
