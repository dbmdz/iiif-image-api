package de.digitalcollections.iiif.image.model.impl;

import de.digitalcollections.iiif.image.model.api.RotationParameters;

public class RotationParametersImpl implements RotationParameters {

  private int degrees;
  private boolean mirrorHorizontally;

  @Override
  public int getDegrees() {
    return degrees;
  }

  @Override
  public void setDegrees(int degrees) {
    this.degrees = degrees;
  }

  @Override
  public boolean isMirrorHorizontally() {
    return mirrorHorizontally;
  }

  @Override
  public void setMirrorHorizontally(boolean mirrorHorizontally) {
    this.mirrorHorizontally = mirrorHorizontally;
  }
}
