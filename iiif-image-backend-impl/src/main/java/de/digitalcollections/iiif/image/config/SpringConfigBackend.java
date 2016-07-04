package de.digitalcollections.iiif.image.config;

import de.digitalcollections.iiif.image.backend.impl.cache.BufferedImageCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.util.StringUtils;

/**
 * Backend configuration.
 */
@Configuration
@ComponentScan(basePackages = {
  "de.digitalcollections.iiif.image.backend.impl.repository",
  "de.digitalcollections.iiif.image.backend.impl.resolver"
})
@PropertySource(value = {
  "classpath:de/digitalcollections/iiif/image/config/SpringConfigBackend-${spring.profiles.active:PROD}.properties"
})
public class SpringConfigBackend {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpringConfigBackend.class);

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
  }

  private String bufferedImageCacheDirectory;

  @Value("${bufferedImageCacheDirectory}")
  public void setBufferedImageCacheDirectory(String path) {
    this.bufferedImageCacheDirectory = path.replaceFirst("~", System.getProperty("user.home"));
  }

  @Bean
  public BufferedImageCache getBufferedImageCache() {
    BufferedImageCache bufferedImageCache = new BufferedImageCache();
    if (StringUtils.isEmpty(bufferedImageCacheDirectory)) {
      // TODO make use of cache configurable
//        bufferedImageCache.s
    }
    bufferedImageCache.setCacheDir(bufferedImageCacheDirectory);
    return bufferedImageCache;
  }
}
