package de.digitalcollections.iiif.image.frontend.impl.springmvc.controller;

import de.digitalcollections.iiif.image.backend.impl.cache.BufferedImageCache;
import de.digitalcollections.iiif.image.backend.impl.cache.PersistenceException;
import de.digitalcollections.iiif.image.config.SpringConfigBackendImage;
import java.awt.image.BufferedImage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = {
  "de.digitalcollections.core.config",
  "de.digitalcollections.iiif.image.config",
  "de.digitalcollections.iiif.image.backend.impl.repository",
  "de.digitalcollections.iiif.image.backend.impl.resolver"
}, excludeFilters = {
  @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = SpringConfigBackendImage.class)
})
@PropertySource(value = {
  "classpath:de/digitalcollections/iiif/image/config/SpringConfigBackend-${spring.profiles.active:PROD}.properties"
})
public class TestConfiguration extends WebMvcConfigurerAdapter {

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
  }

  @Bean
  BufferedImageCache getImageCache() {
    return new StubbedBufferedImageCache();
  }

  private class StubbedBufferedImageCache extends BufferedImageCache {

    @Override
    public void put(String key, BufferedImage bi) throws PersistenceException {
    }

    @Override
    public boolean isCached(String identifier) {
      return super.isCached(identifier);
    }

    @Override
    public void setCacheDir(String cacheDir) {
      return;
    }
  }
}
