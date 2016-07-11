package de.digitalcollections.iiif.image.backend.impl.repository.jpegtran.v2_0_0;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * JNI Wrapper for libepeg and libjpeg analog to https://github.com/jbaiter/jpegtran-cffi for python.
 */
public class Transformation {

  static {
    try {
      LibraryLoader.loadLibrary("jpegtran-jni");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Downscale the image.
   *
   * @param bytesIn buffer with the source image
   * @param outData buffer containing the result image
   * @param width width of the scaled image
   * @param height height of scaled
   * @param quality quality of the scaled image
   * @return byte array length of result buffer
   *
   */
  public static native int downscale(byte[] bytesIn, ByteBuffer outData, int width, int height, int quality);

  /**
   * Crop a rectangular area from the image.
   *
   * @param bytesIn buffer with the source image
   * @param outData buffer containing the result image
   * @param x horizontal coordinate of upper-left corner
   * @param y vertical coordinate of upper-left corner
   * @param width width of area
   * @param height height of area
   * @return byte array length of result buffer
   *
   */
  public static native int crop(byte[] bytesIn, ByteBuffer outData, int x, int y, int width, int height);

  /**
   * Transpose the image.
   *
   * @param bytesIn buffer with the source image
   * @param outData buffer containing the result image
   * @return byte array length of result buffer
   *
   */
  public static native int transpose(byte[] bytesIn, ByteBuffer outData);

  /**
   * Transverse the image.
   *
   * @param bytesIn buffer with the source image
   * @param outData buffer containing the result image
   * @return byte array length of result buffer
   *
   */
  public static native int transverse(byte[] bytesIn, ByteBuffer outData);

  /**
   * Flip the image in horizontal or vertical direction.
   *
   * @param bytesIn buffer with the source image
   * @param outData buffer containing the result image
   * @param vertical Flipping direction (true = vertical, false = horizontal)
   * @return byte array length of result buffer
   *
   */
  public static native int flip(byte[] bytesIn, ByteBuffer outData, boolean vertical);

  /**
   * Rotate the image.
   *
   * @param bytesIn buffer with the source image
   * @param outData buffer containing the result image
   * @param angle: rotation angle, -90, 90, 180 or 270
   * @return byte array length of result buffer
   */
  public static native int rotate(byte[] bytesIn, ByteBuffer outData, int angle);

  /**
   * Get width of image.
   *
   * @param bytesIn buffer with the source image
   * @return width of image in pixels
   */
  public static native int getWidth(byte[] bytesIn);

  /**
   * Get height of image.
   *
   * @param bytesIn buffer with the source image
   * @return height of image in pixels
   */
  public static native int getHeight(byte[] bytesIn);
}
