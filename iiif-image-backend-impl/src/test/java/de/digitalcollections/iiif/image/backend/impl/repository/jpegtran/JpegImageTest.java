package de.digitalcollections.iiif.image.backend.impl.repository.jpegtran;

import de.digitalcollections.iiif.image.backend.impl.repository.jpegtran.v2_0_0.JpegImage;
import de.digitalcollections.iiif.image.JniTest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import org.apache.commons.io.IOUtils;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;

@Category(JniTest.class)
public class JpegImageTest {

  private JpegImage image;

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Before
  public void setUp() throws IOException {
    InputStream imgStream = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("test.jpg");
    this.image = new JpegImage(IOUtils.toByteArray(imgStream));
  }

  @Test
  public void testFromURI() throws URISyntaxException, IOException {
    URL imgUrl = Thread.currentThread()
            .getContextClassLoader()
            .getResource("test.jpg");
    JpegImage img = new JpegImage(imgUrl.toURI());
    assertEquals(img.getWidth(), 480);
    assertEquals(img.getHeight(), 360);
  }

  @Test
  public void testRotate() {
    JpegImage rotatedImg90 = image.rotate(90);
    assertEquals(rotatedImg90.getWidth(), image.getHeight());

    JpegImage rotatedImg180 = image.rotate(180);
    assertEquals(rotatedImg180.getWidth(), image.getWidth());
    assertThat(rotatedImg180.toByteArray(), not(equalTo(image.toByteArray())));

    JpegImage rotatedImg270 = image.rotate(270);
    assertEquals(rotatedImg270.getWidth(), image.getHeight());
    assertThat(rotatedImg270.toByteArray(), not(equalTo(rotatedImg90.toByteArray())));

    exception.expect(IllegalArgumentException.class);
    image.rotate(-90);

    exception.expect(IllegalArgumentException.class);
    image.rotate(450);

    exception.expect(IllegalArgumentException.class);
    image.rotate(45);
  }

  @Test
  public void testFlipVertical() {
    JpegImage flippedImage = image.flipVertical();
    assertEquals(flippedImage.getHeight(), image.getHeight());
    assertThat(flippedImage.toByteArray(), not(equalTo(image.toByteArray())));
  }

  @Test
  public void testFlipHorizontal() {
    JpegImage flippedImageV = image.flipVertical();
    JpegImage flippedImageH = image.flipHorizontal();
    assertEquals(flippedImageH.getHeight(), image.getHeight());
    assertThat(flippedImageH.toByteArray(), not(equalTo(image.toByteArray())));
    assertThat(flippedImageH.toByteArray(), not(equalTo(flippedImageV.toByteArray())));
  }

  @Test
  public void testTranspose() throws Exception {
    JpegImage transposedImage = image.transpose();
    assertEquals(transposedImage.getHeight(), image.getWidth());
    assertThat(transposedImage.toByteArray(), not(equalTo(image.toByteArray())));
  }

  @Test
  public void testTransverse() throws Exception {
    JpegImage transposedImage = image.transpose();
    JpegImage transversedImageH = image.transverse();
    assertEquals(transversedImageH.getHeight(), image.getWidth());
    assertThat(transversedImageH.toByteArray(), not(equalTo(image.toByteArray())));
    assertThat(transversedImageH.toByteArray(), not(equalTo(transposedImage.toByteArray())));
  }

  @Test
  public void testDownScale() {
    JpegImage scaledImg = image.downScale(50, 50);
    Assert.assertEquals(scaledImg.getWidth(), 50);
    Assert.assertEquals(scaledImg.getHeight(), 50);

    exception.expect(IllegalArgumentException.class);
    image.downScale(800, 800);
  }

  @Test
  public void testCrop() throws Exception {
    JpegImage croppedImage = image.crop(0, 0, 50, 50);
    Assert.assertEquals(croppedImage.getWidth(), 50);
    Assert.assertEquals(croppedImage.getHeight(), 50);
  }

  @Test
  public void TestCropFullWidth() throws Exception {
    JpegImage croppedImage = image.crop(0, 0, 480, 240);
    Assert.assertEquals(croppedImage.getWidth(), 480);
    Assert.assertEquals(croppedImage.getHeight(), 240);
  }

  @Test
  public void testCropBadWidth() throws Exception {
    InputStream imgStream = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("test3.jpg");
    JpegImage image = new JpegImage(IOUtils.toByteArray(imgStream));
    JpegImage croppedImage = image.crop(0, 0, 1500, 2048);
    Assert.assertEquals(croppedImage.getWidth(), 1500);
    Assert.assertEquals(croppedImage.getHeight(), 2048);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadData() throws Exception {
    JpegImage image = new JpegImage(new byte[]{1, 2, 3, 4, 5});
    image.downScale(50, 50);
  }
}
