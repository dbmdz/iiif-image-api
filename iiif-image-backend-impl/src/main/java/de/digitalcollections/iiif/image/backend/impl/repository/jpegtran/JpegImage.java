package de.digitalcollections.iiif.image.backend.impl.repository.jpegtran;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;

public class JpegImage {

  private byte[] imgData;

  /**
   * Read JPEG image from URI.
   *
   * @param filePath
   * @throws IOException
   */
  public JpegImage(URI filePath) throws IOException {
    this.imgData = IOUtils.toByteArray(filePath.toURL().openStream());
  }

  /**
   * Read JPEG image from byte array.
   *
   * @param data
   */
  public JpegImage(byte[] data) {
    if ((data[0] & 0xFF) != 0xFF || (data[1] & 0xFF) != 0xD8) {
        throw new IllegalArgumentException("Not a JPEG file");
    }
    this.imgData = data;
  }

  public int getWidth() {
    return Transformation.getWidth(this.imgData);
  }

  public int getHeight() {
    return Transformation.getHeight(this.imgData);
  }

  /**
   * Rotate image
   * @param angle Degree to rotate. Must be 90, 180 or 270.
   * @return A new JpegImage instance with the rotated image data.
   */
  public JpegImage rotate(int angle) {
    if (angle % 90 != 0 || angle < 0 || angle > 270) {
      throw new IllegalArgumentException("Degree must be 90, 180 or 270");
    }
    ByteBuffer outBuf = getByteBuffer();
    int length = Transformation.rotate(imgData, outBuf, angle);
    byte[] newData = new byte[length];
    outBuf.get(newData);
    return new JpegImage(newData);
  }

  /**
   * Flip the image in horizontal direction.
   * @return A new JpegImage instance with the flipped image data.
   */
  public JpegImage flipHorizontal() {
    ByteBuffer outBuf = getByteBuffer();
    int length = Transformation.flip(imgData, outBuf, false);
    byte[] newData = new byte[length];
    outBuf.get(newData);
    return new JpegImage(newData);
  }

  /**
   * Flip the image in vertical direction.
   * @return A new JpegImage instance with the flipped image data.
   */
  public JpegImage flipVertical() {
    ByteBuffer outBuf = getByteBuffer();
    int length = Transformation.flip(imgData, outBuf, true);
    byte[] newData = new byte[length];
    outBuf.get(newData);
    return new JpegImage(newData);
  }

  /**
   * Tranpose the image.
   * @return A new JpegImage instance with the transposed image data.
   */
  public JpegImage transpose() {
    ByteBuffer outBuf = getByteBuffer();
    int length = Transformation.transpose(imgData, outBuf);
    byte[] newData = new byte[length];
    outBuf.get(newData);
    return new JpegImage(newData);
  }

  /**
   * Transverse transpose the image.
   * @return A new JpegImage instance with the transverse transposed image data.
   */
  public JpegImage transverse() {
    ByteBuffer outBuf = getByteBuffer();
    int length = Transformation.transverse(imgData, outBuf);
    byte[] newData = new byte[length];
    outBuf.get(newData);
    return new JpegImage(newData);
  }

  /**
   * Downscale the image.
   * @param width Desired width in pixels, must be smaller than original width
   * @param height Desired height in pixels, must be smaller than original height
   * @return A new JpegImage instance with the downscaled image data.
   */
  public JpegImage downScale(int width, int height) {
    return downScale(width, height, 75);
  }

  public JpegImage downScale(int width, int height, int quality) {
    ByteBuffer outBuf = getByteBuffer();
    if (width > getWidth() || height > getHeight()) {
      throw new IllegalArgumentException("Target dimensions must be smaller than original dimensions.");
    }
    if (width <= 0 || height <= 0) {
        throw new IllegalArgumentException("Width and height must be greater than 0");
    }
    int length = Transformation.downscale(imgData, outBuf, width, height, quality);
    byte[] newData = new byte[length];
    outBuf.get(newData);
    return new JpegImage(newData);
  }

  public JpegImage crop(int x, int y, int width, int height) {
    if (width > (this.getWidth() - x) || height > (this.getHeight() - y)) {
        throw new IllegalArgumentException("Width or height exceed the boundaries of the cropped image, check the vertical/horizontal offset!");
    }
    if (x < 0 || y < 0) {
        throw new IllegalArgumentException("Vertical and horizontal offsets cannot be negative.");
    }
    if (width <= 0 || height <= 0) {
        throw new IllegalArgumentException("Width and height must be greater than 0");

    }
    ByteBuffer outBuf = getByteBuffer();
    int length = Transformation.crop(imgData, outBuf, x, y, width, height);
    byte[] newData = new byte[length];
    outBuf.get(newData);
    return new JpegImage(newData);
  }

  public byte[] toByteArray() {
    return imgData;
  }

  public void write(URI filePath) throws IOException {
    File outFile = new File(filePath.toURL().getFile());
    if (!outFile.exists()) {
      outFile.createNewFile();
    }
    IOUtils.write(imgData, new FileOutputStream(outFile));
  }

  private ByteBuffer getByteBuffer() {
      return ByteBuffer.allocateDirect((int) (imgData.length*5));
  }
}
