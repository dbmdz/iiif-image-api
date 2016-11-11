package de.digitalcollections.iiif.image.model.impl.v2;

import de.digitalcollections.iiif.image.model.api.v2.ScaleParameters;

public class ScaleParametersImpl implements ScaleParameters {

  private final int targetWidth;
  private final int targetHeight;

  public ScaleParametersImpl(int targetWidth, int targetHeight) {
    this.targetWidth = targetWidth;
    this.targetHeight = targetHeight;
  }

  @Override
  public int getTargetWidth() {
    return targetWidth;
  }

  @Override
  public int getTargetHeight() {
    return targetHeight;
  }
}
