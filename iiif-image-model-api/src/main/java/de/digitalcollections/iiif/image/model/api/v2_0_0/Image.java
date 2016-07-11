package de.digitalcollections.iiif.image.model.api.v2_0_0;

import de.digitalcollections.iiif.image.model.api.enums.ImageBitDepth;
import de.digitalcollections.iiif.image.model.api.enums.ImageFormat;
import de.digitalcollections.iiif.image.model.api.exception.InvalidParametersException;
import java.io.IOException;

public interface Image {

  public abstract Image convert(ImageFormat format) throws UnsupportedOperationException;

  public abstract Image crop(RegionParameters params) throws UnsupportedOperationException, InvalidParametersException;

  public Image flipHorizontally();

  abstract ImageFormat getFormat();

  public abstract int getHeight();

  public abstract int getWidth();

  public abstract Image rotate(int arcDegree) throws UnsupportedOperationException, InvalidParametersException;

  public abstract Image scale(ResizeParameters params) throws UnsupportedOperationException, InvalidParametersException;

  public abstract byte[] toByteArray() throws IOException;

  public abstract Image toDepth(ImageBitDepth depth) throws UnsupportedOperationException;

}
