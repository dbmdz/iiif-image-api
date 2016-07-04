package de.digitalcollections.iiif.image.frontend.impl.springmvc.controller;

import de.digitalcollections.iiif.image.business.api.service.IiifParameterParserService;
import de.digitalcollections.iiif.image.business.api.service.ImageService;
import de.digitalcollections.iiif.image.frontend.impl.springmvc.exception.InvalidParametersException;
import de.digitalcollections.iiif.image.frontend.impl.springmvc.exception.ResolvingException;
import de.digitalcollections.iiif.image.frontend.impl.springmvc.exception.ResourceNotFoundException;
import de.digitalcollections.iiif.image.frontend.impl.springmvc.exception.UnsupportedFormatException;
import de.digitalcollections.iiif.image.model.api.Image;
import de.digitalcollections.iiif.image.model.api.ImageInfo;
import de.digitalcollections.iiif.image.model.api.RegionParameters;
import de.digitalcollections.iiif.image.model.api.ResizeParameters;
import de.digitalcollections.iiif.image.model.api.RotationParameters;
import de.digitalcollections.iiif.image.model.api.enums.ImageBitDepth;
import de.digitalcollections.iiif.image.model.api.enums.ImageFormat;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@CrossOrigin
@RequestMapping("/iiif/image")
public class IIIFImageApiController {

  private static final Logger LOGGER = LoggerFactory.getLogger(IIIFImageApiController.class);

  @Autowired
  private IiifParameterParserService iiifParameterParserService;

  @Autowired
  private ImageService imageService;

  private final String IIIF_COMPLIANCE = "http://iiif.io/api/image/2/level2.json";
  private final String IIIF_CONTEXT = "http://iiif.io/api/image/2/context.json";

  private String getBasePath(HttpServletRequest request, String identifier) {
    String requestURI = request.getRequestURI();
    if (requestURI.isEmpty()) {
      requestURI = "/" + identifier + "/"; // For unit-tests
    }
    String idEndpoint = requestURI.substring(0, requestURI.lastIndexOf('/'));
    String baseUrl;
    if (request.getServerPort() != 80) {
      baseUrl = String.format("%s://%s:%d%s", request.getScheme(),
              request.getServerName(), request.getServerPort(),
              idEndpoint);
    } else {
      baseUrl = String.format("%s://%s%s", request.getScheme(),
              request.getServerName(), idEndpoint);
    }
    return baseUrl;
  }

  /**
   * see <a href="http://iiif.io/api/image/2.0/#image-request-uri-syntax">IIIF 2.0</a><br/>
   * The sequence of parameters in the URI is intended as a mnemonic for the order in which image manipulations are made
   * against the full image content. This is important to consider when implementing the service because applying the
   * same parameters in a different sequence will often result in a different image being delivered. The order is
   * critical so that the application calling the service reliably receives the output it expects.<br/>
   * The parameters should be interpreted as if the the sequence of image manipulations were:<br/>
   * <b>Region THEN Size THEN Rotation THEN Quality THEN Format</b><br/>
   * If the rotation parameter includes mirroring (“!”), the mirroring is applied before the rotation.
   *
   * @param identifier
   * @param region The region parameter defines the rectangular portion of the full image to be returned. Region can be
   * specified by pixel coordinates, percentage or by the value “full”, which specifies that the entire image should be
   * returned.
   * @param size The size parameter determines the dimensions to which the extracted region is to be scaled.
   * @param rotation The rotation parameter specifies mirroring and rotation. A leading exclamation mark (“!”) indicates
   * that the image should be mirrored by reflection on the vertical axis before any rotation is applied. The numerical
   * value represents the number of degrees of clockwise rotation, and may be any floating point number from 0 to 360.
   * @param quality The quality parameter determines whether the image is delivered in color, grayscale or black and
   * white.
   * @param format The format of the returned image is expressed as an extension at the end of the URI.
   * @param request
   * @return
   * @throws ResolvingException
   * @throws UnsupportedFormatException
   * @throws UnsupportedOperationException
   * @throws IOException on
   * @throws URISyntaxException
   * @throws de.digitalcollections.iiif.image.frontend.impl.springmvc.exception.InvalidParametersException
   */
  @CrossOrigin(origins = "*")
  @RequestMapping(value = "{identifier}/{region}/{size}/{rotation}/{quality}.{format}")
  public ResponseEntity<byte[]> getImageRepresentation(
          @PathVariable String identifier, @PathVariable String region,
          @PathVariable String size, @PathVariable String rotation,
          @PathVariable String quality, @PathVariable String format,
          HttpServletRequest request) throws ResolvingException,
          UnsupportedFormatException, UnsupportedOperationException, IOException,
          URISyntaxException, InvalidParametersException {
    final String requestURI = request.getRequestURI();
    LOGGER.info("getImageRepresentation for url {}", requestURI);

    HttpHeaders headers = new HttpHeaders();

    try {
      RegionParameters regionParameters = iiifParameterParserService.parseIiifRegion(region);
      ResizeParameters sizeParameters = iiifParameterParserService.parseIiifSize(size);
      RotationParameters rotationParameters = iiifParameterParserService.parseIiifRotation(rotation);
      ImageBitDepth bitDepthParameter = iiifParameterParserService.parseIiifQuality(quality);
      ImageFormat formatParameter = iiifParameterParserService.parseIiifFormat(format);

      Image image = imageService.processImage(identifier, regionParameters, sizeParameters,
              rotationParameters, bitDepthParameter, formatParameter);

      // header
      final ImageFormat imageFormat = image.getFormat();
      final String mimeType = imageFormat.getMimeType();
      headers.setContentType(MediaType.parseMediaType(mimeType));
      String path;
      if (request.getPathInfo() != null) {
        path = request.getPathInfo();
      } else {
        path = request.getServletPath();
      }
      String filename = path.replaceFirst("/iiif/image/", "").replace('/', '_').
              replace(',', '_');
      headers.set("Content-Disposition", "attachment; filename=" + filename);
      // content
      byte[] data = image.toByteArray();

      final ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(data, headers, HttpStatus.OK);
      return responseEntity;
    } catch (de.digitalcollections.iiif.image.model.api.exception.InvalidParametersException ex) {
      throw new InvalidParametersException(ex.getMessage());
    } catch (de.digitalcollections.iiif.image.model.api.exception.UnsupportedFormatException ex) {
      throw new UnsupportedFormatException(ex.getMessage());
    }
  }

  /**
   * Specification see: http://iiif.io/api/image/2.0/#image-information
   * <p>
   * Example response:</p>
   *
   * <pre>
   * {
   *   "@context": "http://library.stanford.edu/iiif/image-api/1.1/context.json",
   *   "@id": "http://iiif.example.com/prefix/1E34750D-38DB-4825-A38A-B60A345E591C",
   *   "width": 6000, "height": 4000
   * }
   * </pre>
   *
   * @param identifier - The identifier to obtain information for
   * @param request servlet request
   * @return String - The information in JSON notation
   * @throws de.digitalcollections.iiif.image.frontend.impl.springmvc.exception.ResolvingException
   * @throws de.digitalcollections.iiif.image.frontend.impl.springmvc.exception.UnsupportedFormatException
   * @throws java.io.IOException
   */
  @SuppressWarnings("unchecked")
  @CrossOrigin(origins = "*")
  @RequestMapping(value = "{identifier}/info.json", method = RequestMethod.GET)
  public ResponseEntity<String> getInfo(@PathVariable String identifier,
          HttpServletRequest request) throws ResolvingException,
          UnsupportedFormatException, UnsupportedOperationException, IOException {
    try {
      identifier = URLDecoder.decode(identifier, "UTF-8");
      String baseUrl = getBasePath(request, identifier);
      ImageInfo img = imageService.getImageInfo(identifier);
      if (img == null) {
        throw new ResourceNotFoundException();
      }
      JSONObject info = new JSONObject();
      JSONArray profiles = new JSONArray();
      profiles.add(IIIF_COMPLIANCE);
      info.put("@context", IIIF_CONTEXT); // The context document that describes the semantics of the terms used in the document. This must be the URI: http://iiif.io/api/image/2/context.json for version 2.0 of the IIIF Image API.
      info.put("@id", baseUrl); // The Base URI of the image as defined in URI Syntax, including scheme, server, prefix and identifier without a trailing slash.
      info.put("width", img.getWidth()); // The width in pixels of the full image content, given as an integer.
      info.put("height", img.getHeight()); // The height in pixels of the full image content, given as an integer.
      info.put("profile", profiles); // An array of profiles, indicated by either a URI or an object describing the features supported. The first entry in the array must be a compliance level URI, as defined below.
      info.put("protocol", "http://iiif.io/api/image");

      HttpHeaders headers = new HttpHeaders();
      String contentType = request.getHeader("Accept");
      if (contentType != null && contentType.equals("application/ld+json")) {
        headers.set("Content-Type", contentType);
      } else {
        headers.set("Content-Type", "application/json");
        headers.set("Link", "<http://iiif.io/api/image/2/context.json>; "
                + "rel=\"http://www.w3.org/ns/json-ld#context\"; "
                + "type=\"application/ld+json\"");
      }
      String json = info.toJSONString();
      return new ResponseEntity<>(json, headers, HttpStatus.OK);
    } catch (de.digitalcollections.iiif.image.model.api.exception.UnsupportedFormatException ex) {
      throw new UnsupportedFormatException(ex.getMessage());
    }
  }

  @CrossOrigin(origins = "*")
  @RequestMapping(value = "{identifier}", method = RequestMethod.GET)
  public String getInfoRedirect(@PathVariable String identifier
  ) {
    return "redirect:/iiif/image/" + identifier + "/info.json";
  }
}
