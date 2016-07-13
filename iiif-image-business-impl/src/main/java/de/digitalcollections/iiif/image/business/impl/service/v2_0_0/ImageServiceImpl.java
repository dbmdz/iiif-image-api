package de.digitalcollections.iiif.image.business.impl.service.v2_0_0;

import de.digitalcollections.iiif.image.backend.api.repository.v2_0_0.ImageRepository;
import de.digitalcollections.iiif.image.business.api.service.ImageSecurityService;
import de.digitalcollections.iiif.image.business.api.service.v2_0_0.ImageService;
import de.digitalcollections.iiif.image.model.api.enums.ImageBitDepth;
import de.digitalcollections.iiif.image.model.api.enums.ImageFormat;
import de.digitalcollections.iiif.image.model.api.exception.InvalidParametersException;
import de.digitalcollections.iiif.image.model.api.exception.ResourceNotFoundException;
import de.digitalcollections.iiif.image.model.api.exception.UnsupportedFormatException;
import de.digitalcollections.iiif.image.model.api.v2_0_0.Image;
import de.digitalcollections.iiif.image.model.api.v2_0_0.ImageInfo;
import de.digitalcollections.iiif.image.model.api.v2_0_0.RegionParameters;
import de.digitalcollections.iiif.image.model.api.v2_0_0.ResizeParameters;
import de.digitalcollections.iiif.image.model.api.v2_0_0.RotationParameters;
import de.digitalcollections.iiif.image.model.impl.v2_0_0.ResizeParametersImpl;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service(value = "ImageServiceImpl-v2.0.0")
public class ImageServiceImpl implements ImageService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ImageServiceImpl.class);

  @Autowired
  private List<ImageRepository> imageRepositories;

  @Autowired(required = false)
  private ImageSecurityService imageSecurityService;

  @Override
  public ImageInfo getImageInfo(String identifier) throws UnsupportedFormatException, UnsupportedOperationException {
    if (imageSecurityService != null && !imageSecurityService.isAccessAllowed(identifier)) {
      throw new ResourceNotFoundException(); // TODO maybe throw an explicitely access disallowed exception
    }
    // FIXME: This is really ugly, but unfortunately there's no way to tell from the identifier what
    // format we're dealing with...
    for (ImageRepository repo : this.imageRepositories) {
      try {
        ImageInfo info = repo.getImageInfo(identifier);
        LOGGER.debug("Using " + repo.getClass().getName());
        return info;
      } catch (Throwable repoNotWorking) {
      }
    }
    throw new UnsupportedFormatException();
  }

  private Image getImage(String identifier, RegionParameters regionParameters, ImageFormat outputFormat, ImageBitDepth bitDepthParameter)
          throws UnsupportedFormatException, InvalidParametersException, UnsupportedOperationException {
    for (ImageRepository repo : this.imageRepositories) {
      if (!repo.getSupportedOutputFormats().contains(outputFormat)
              || (bitDepthParameter != null && !repo.getSupportedBitDepths().
              contains(bitDepthParameter))) {
        continue;
      }
      try {
        Image image = repo.getImage(identifier, regionParameters);
        LOGGER.debug("Using " + repo.getClass().getName());
        return image;
      } catch (InvalidParametersException e) {
        throw e;
      } catch (Throwable repoNotWorking) {
      }
    }
    throw new UnsupportedFormatException();
  }

  @Override
  public Image processImage(String identifier, RegionParameters regionParameters, ResizeParameters sizeParameters, RotationParameters rotationParameters, ImageBitDepth bitDepthParameter, ImageFormat formatParameter)
          throws InvalidParametersException, UnsupportedOperationException, UnsupportedFormatException {
    if (imageSecurityService != null && !imageSecurityService.isAccessAllowed(identifier)) {
      LOGGER.info("Access to image '{}' is not allowed!", identifier);
      throw new ResourceNotFoundException(); // TODO maybe throw an explicitely access disallowed exception
    }
    Image image = getImage(identifier, regionParameters, formatParameter, bitDepthParameter);
    if (image == null) {
      throw new ResourceNotFoundException();
    }
    image = transformImage(image, regionParameters, sizeParameters, rotationParameters, bitDepthParameter, formatParameter);
    return image;
  }

  private Image transformImage(Image image, RegionParameters regionParameters, ResizeParameters sizeParameters, RotationParameters rotationParameters, ImageBitDepth bitDepthParameter, ImageFormat formatParameter)
          throws InvalidParametersException, UnsupportedOperationException, UnsupportedFormatException {

    // now do processing:
    if (regionParameters != null && (image.getWidth() != regionParameters.getWidth() || image.
            getHeight() != regionParameters.getHeight())) {
      image = image.crop(regionParameters);
    }

    if (sizeParameters != null) {
      sizeParameters = new ResizeParametersImpl(sizeParameters, image.getWidth(), image.getHeight());
    }

    if (sizeParameters != null && sizeParameters.getMaxHeight() != -1 && sizeParameters.
            getMaxWidth() != -1) {
      image = image.scale(sizeParameters);
    }
    if (rotationParameters != null) {
      if (rotationParameters.isMirrorHorizontally()) {
        image = image.flipHorizontally();
      }
      if (rotationParameters.getDegrees() > 0) {
        image = image.rotate(rotationParameters.getDegrees());
      }
    }
    if (bitDepthParameter != null) {
      image = image.toDepth(bitDepthParameter);
    }
    if (!(formatParameter == image.getFormat())) {
      image = image.convert(formatParameter);
    }
    return image;
  }

}
