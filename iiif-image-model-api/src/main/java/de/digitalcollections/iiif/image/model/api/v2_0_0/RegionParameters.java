package de.digitalcollections.iiif.image.model.api.v2_0_0;

/**
 * Container for type safe image region parameters.
 */
public interface RegionParameters {

  float getHeight();

  void setHeight(float height);

  float getHorizontalOffset();

  void setHorizontalOffset(float horizontalOffset);

  float getVerticalOffset();

  void setVerticalOffset(float verticalOffset);

  float getWidth();

  void setWidth(float width);

  boolean isAbsolute();

  void setAbsolute(boolean absolute);

}
