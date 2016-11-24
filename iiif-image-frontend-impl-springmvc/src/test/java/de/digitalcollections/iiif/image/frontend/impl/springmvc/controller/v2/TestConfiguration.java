package de.digitalcollections.iiif.image.frontend.impl.springmvc.controller.v2;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = {
  "de.digitalcollections.core.config",
  "de.digitalcollections.iiif.image.config",
  "de.digitalcollections.iiif.image.backend.impl.repository",})
@PropertySource(value = {
  "classpath:de/digitalcollections/iiif/image/config/SpringConfigBackend-${spring.profiles.active:PROD}.properties"
})
public class TestConfiguration extends WebMvcConfigurerAdapter {

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
  }
}
