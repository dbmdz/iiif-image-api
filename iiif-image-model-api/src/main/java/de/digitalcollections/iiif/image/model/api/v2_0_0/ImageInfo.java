package de.digitalcollections.iiif.image.model.api.v2_0_0;

import de.digitalcollections.iiif.image.model.api.enums.ImageBitDepth;
import de.digitalcollections.iiif.image.model.api.enums.ImageFormat;

public interface ImageInfo {

  ImageBitDepth getBitDepth();

  void setBitDepth(ImageBitDepth bitDepth);

  ImageFormat getFormat();

  void setFormat(ImageFormat format);

  int getHeight();

  void setHeight(int height);

  int getWidth();

  void setWidth(int width);

}
