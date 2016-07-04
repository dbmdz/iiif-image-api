package de.digitalcollections.iiif.image.backend.impl.repository.jpegtran;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * JNI Wrapper for libepeg and libjpeg analog to
 * https://github.com/jbaiter/jpegtran-cffi for python.
 *
 * @author Christian Kaufhold
 * @version 1.0
 */
public class Transformation {
  static {
    try {
      LibraryLoader.loadLibrary("jpegtran-jni");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static int MAX_FILE_SIZE = 20000000;

  // JNI //////////////////////////////////////////////////////////////////////
  /**
   * Downscale the image.
   *
   * @param bytesIn: buffer with the source image
   * @param width: width of the scaled image
   * @param height: height of scaled
   * @param quality: quality of the scaled image
   * @return: scaled image
   *
   */
  public static native int downscale(byte[] bytesIn, ByteBuffer outData, int width, int height, int quality);

  /**
   * Crop a rectangular area from the image.
   *
   * @param bytesIn: buffer with the source image
   * @param x: horizontal coordinate of upper-left corner
   * @param y: vertical coordinate of upper-left corner
   * @param width: width of area
   * @param height: height of area
   * @return: cropped image
   *
   */
  public static native int crop(byte[] bytesIn, ByteBuffer outData, int x, int y, int width, int height);

  /**
   * Transpose the image.
   *
   * @param bytesIn: buffer with the source image
   * @return: transposed image
   *
   */
  public static native int transpose(byte[] bytesIn, ByteBuffer outData);

  /**
   * Transverse the image.
   *
   * @param bytesIn: buffer with the source image
   * @return: transversed image
   *
   */
  public static native int transverse(byte[] bytesIn, ByteBuffer outData);

  /**
   * Flip the image in horizontal or vertical direction.
   *
   * @param bytesIn: buffer with the source image
   * @param direction: Flipping direction
   * @return: flipped image
   *
   */
  public static native int flip(byte[] bytesIn, ByteBuffer outData, boolean vertical);

  /**
   * Rotate the image.
   *
   * @param angle: rotation angle, -90, 90, 180 or 270
   * @return: rotated image
   */
  public static native int rotate(byte[] bytesIn, ByteBuffer outData, int angle);

  public static native int getWidth(byte[] bytesIn);

  public static native int getHeight(byte[] bytesIn);
}
