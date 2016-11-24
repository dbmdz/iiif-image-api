package de.digitalcollections.iiif.image.config;

import java.io.IOException;
import javax.cache.CacheManager;
import javax.cache.Caching;
import org.ehcache.jsr107.EhcacheCachingProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * Backend configuration.
 */
@Configuration
@ComponentScan(basePackages = {
  "de.digitalcollections.iiif.image.backend.impl.repository",
  "de.digitalcollections.core.config"
})
@PropertySource(value = {
  "classpath:de/digitalcollections/iiif/image/config/SpringConfigBackend-${spring.profiles.active:PROD}.properties"
})
@EnableCaching
public class SpringConfigBackendImage {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpringConfigBackendImage.class);

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
  }

  @Bean
  public JCacheCacheManager jCacheManager(CacheManager cacheManager) {
    return new JCacheCacheManager(cacheManager);
  }

  @Bean
  public CacheManager cacheManager(ResourceLoader resourceLoader) throws IOException {
    EhcacheCachingProvider provider = (EhcacheCachingProvider) Caching.getCachingProvider();
    Resource configLocation = resourceLoader.getResource("classpath:ehcache.xml");
    return  provider.getCacheManager(
        configLocation.getURI(),
        provider.getDefaultClassLoader());
  }
}
