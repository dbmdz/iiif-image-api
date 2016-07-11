package de.digitalcollections.iiif.image.model.impl.v2_0_0;

import de.digitalcollections.iiif.image.model.api.v2_0_0.ImageInfo;
import de.digitalcollections.iiif.image.model.api.enums.ImageBitDepth;
import de.digitalcollections.iiif.image.model.api.enums.ImageFormat;

public class ImageInfoImpl implements ImageInfo {

  private ImageBitDepth bitDepth;
  private int height;
  private ImageFormat format;
  private int width;

  @Override
  public ImageBitDepth getBitDepth() {
    return bitDepth;
  }

  @Override
  public ImageFormat getFormat() {
    return format;
  }

  @Override
  public int getHeight() {
    return height;
  }

  @Override
  public int getWidth() {
    return width;
  }

  @Override
  public void setBitDepth(ImageBitDepth bitDepth) {
    this.bitDepth = bitDepth;
  }

  @Override
  public void setFormat(ImageFormat format) {
    this.format = format;
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
