package de.digitalcollections.iiif.image.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Services context.
 */
@Configuration
@ComponentScan(basePackages = {
  "de.digitalcollections.iiif.image.business.impl.service"
})
public class SpringConfigBusiness {
}
