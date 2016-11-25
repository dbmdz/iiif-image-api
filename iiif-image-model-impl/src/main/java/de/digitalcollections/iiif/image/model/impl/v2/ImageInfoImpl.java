package de.digitalcollections.iiif.image.model.impl.v2;

import de.digitalcollections.iiif.image.model.api.v2.ImageInfo;

public class ImageInfoImpl implements ImageInfo {

  private int height;
  private int width;

  @Override
  public int getWidth() {
    return width;
  }

  @Override
  public int getHeight() {
    return height;
  }

  @Override
  public void setHeight(int height) {
    this.height = height;
  }

  @Override
  public void setWidth(int width) {
    this.width = width;
  }

}
