IIIF Image API Library
======================
[![Build Status](https://travis-ci.org/dbmdz/iiif-image-api.svg?branch=master)](https://travis-ci.org/dbmdz/iiif-image-api)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/4791195661d84028945d5b384ce5324f)](https://www.codacy.com/app/ralf-eichinger/iiif-image-api?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=dbmdz/iiif-image-api&amp;utm_campaign=Badge_Grade)
[![codecov](https://codecov.io/gh/dbmdz/iiif-image-api/branch/master/graph/badge.svg)](https://codecov.io/gh/dbmdz/iiif-image-api)
[![MIT License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![GitHub release](https://img.shields.io/github/release/dbmdz/iiif-image-api.svg?maxAge=2592000)](https://github.com/dbmdz/iiif-image-api/releases)

This Java library implements the IIIF Image API 2.0.0 (see <a href="http://iiif.io/api/image/2.0/">http://iiif.io/api/image/2.0/</a>):

"The IIIF Image API specifies a web service that returns an image in response to a standard HTTP or HTTPS request. The URI can specify the region, size, rotation, quality characteristics and format of the requested image. A URI can also be constructed to request basic technical information about the image to support client applications. This API was conceived of to facilitate systematic reuse of image resources in digital image repositories maintained by cultural heritage organizations. It could be adopted by any image repository or service, and can be used to retrieve static images in response to a properly constructed URI."

Features
--------
- IIIF Image API 2.0.0 conform
- Embeddable Spring components: Spring MVC Controller, Spring Services
- Access to images over project specific Resolver-plugin mechanism.
- Image processing engines:
    - Java Image I/O API (javax.imageio)<br/>
(see http://docs.oracle.com/javase/8/docs/api/javax/imageio/package-summary.html#package.description)
    - Independent JPEG Group library "libjpeg8"<br/>
(see http://ijg.org/)

Usage
-----

<h3>Embed IIIF-Controller into your Spring MVC webapplication</h3>

- Add Spring MVC-library as dependency to your pom.xml:

        <dependency>
          <groupId>de.digitalcollections</groupId>
          <artifactId>iiif-image-frontend-impl-springmvc</artifactId>
          <version>2.0.0-SNAPSHOT</version>
          <type>jar</type>
        </dependency>

- Import library's root configuration class into your Spring Web configuration. Example:

        ...
        @Configuration
        ...
        @Import(SpringConfigIIIF.class)
        ...
        public class SpringConfigWeb extends WebMvcConfigurerAdapter {
          ...
        }

- Add Listener "IIOProviderContextListener" (for supporting additional ImageIO image formats) in your WebappInitializer

        import com.twelvemonkeys.servlet.image.IIOProviderContextListener;
        ...

        public class WebappInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
          ...
          @Override
          public void onStartup(ServletContext servletContext) throws ServletException {
            super.onStartup(servletContext);
            servletContext.addListener(new IIOProviderContextListener());
          }
        }

- Implement project specific Resolvers for accessing images. Place them into package "org.mdz.iiifserver.backend.repository.resolvers" or a subpackage and implement interface org.mdz.iiifserver.backend.repository.resolvers.Resolver or use/extend provided implementations from org.mdz.iiifserver.backend.repository.resolvers (FileSystemResolver, HttpResolver, ResourceResolver, ...). They will be found automatically by ComponentScan.

- Start your Spring MVC webapp. You should see mappings for IIIF in your log:

        ...
        [2015-09-22 10:56:58,046 INFO ] [...] RequestMappingHandlerMapping (main    ) > Mapped "{[/iiif/image/{identifier}/info.json],methods=[GET]}" onto public org.json.simple.JSONObject org.mdz.iiifserver.frontend.webapp.controller.IIIFImageApiController.getInfo(java.lang.String,javax.servlet.http.HttpServletRequest) throws org.mdz.iiifserver.domain.errors.ResolvingException,org.mdz.iiifserver.domain.errors.UnsupportedFormat,org.mdz.iiifserver.domain.errors.UnsupportedOperation,java.io.IOException
        [2015-09-22 10:56:58,046 INFO ] [...] RequestMappingHandlerMapping (main    ) > Mapped "{[/iiif/image/{identifier}/{region}/{size}/{rotation}/{quality}.{format}]}" onto public org.springframework.http.ResponseEntity<byte[]> org.mdz.iiifserver.frontend.webapp.controller.IIIFImageApiController.getImageRepresentation(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,javax.servlet.http.HttpServletRequest) throws org.mdz.iiifserver.domain.errors.ResolvingException,org.mdz.iiifserver.domain.errors.UnsupportedFormat,org.mdz.iiifserver.domain.errors.UnsupportedOperation,java.io.IOException,org.mdz.iiifserver.domain.errors.InvalidParametersException,org.mdz.iiifserver.domain.errors.RateLimitExceeded,java.net.URISyntaxException
        [2015-09-22 10:56:58,046 INFO ] [...] RequestMappingHandlerMapping (main    ) > Mapped "{[/iiif/image/{identifier}],methods=[GET]}" onto public java.lang.String org.mdz.iiifserver.frontend.webapp.controller.IIIFImageApiController.getInfoRedirect(java.lang.String)
        [2015-09-22 10:56:58,046 INFO ] [...] RequestMappingHandlerMapping (main    ) > Mapped "{[/iiif/presentation/{identifier}/manifest],methods=[GET],produces=[application/json]}" onto public com.datazuul.iiif.presentation.api.model.Manifest org.mdz.iiifserver.frontend.webapp.controller.IIIFPresentationApiController.getManifest(java.lang.String)


FAQ
---
<b>Q</b>: The JSON output for IIIF Presentation API is not correct.<br/>
<b>A</b>: Be sure that Jackson object mapping is configured correctly. The SpringConfigIIIF overrides the method "configureMessageConverters(...)" and configures the MappingJackson2HttpMessageConverter's ObjectMapper properly. But if you override the method in your Spring MVC configuration class, the SpringConfigIIIF message converters configuration is ignored (the root beans rules...).<br/>
Solution: Add proper ObjectMapper configuration to your config. For IIIF this is needed:

        @Bean
        public ObjectMapper objectMapper() {
          ObjectMapper objectMapper = new ObjectMapper();
          // do not serialize null values/objects
          objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

          // IIIF Presentation API objects:
          objectMapper.addMixIn(AbstractIiifResource.class, AbstractIiifResourceMixIn.class);
          objectMapper.addMixIn(Image.class, AbstractIiifResourceMixIn.class);
          objectMapper.addMixIn(Manifest.class, ManifestMixIn.class);
          objectMapper.addMixIn(MetadataLocalizedValue.class, MetadataLocalizedValueMixIn.class);
          objectMapper.addMixIn(Resource.class, AbstractIiifResourceMixIn.class);
          objectMapper.addMixIn(Service.class, ServiceMixIn.class);

          return objectMapper;
        }
