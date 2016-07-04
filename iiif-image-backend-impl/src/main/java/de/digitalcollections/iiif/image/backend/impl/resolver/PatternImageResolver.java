package de.digitalcollections.iiif.image.backend.impl.resolver;

import de.digitalcollections.iiif.image.backend.api.resolver.ImageResolver;
import de.digitalcollections.iiif.image.model.api.exception.ResolvingException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class PatternImageResolver implements ImageResolver, InitializingBean {

  private static final Logger LOGGER = LoggerFactory.getLogger(PatternImageResolver.class);

  @Value("${image.resolvingPatternFile}")
  private String PATTERNFILE;

  private final Map<Pattern, String> patterns;

  public PatternImageResolver(Map<Pattern, String> patterns) {
    this.patterns = patterns;
  }

  public PatternImageResolver() {
    this.patterns = new LinkedHashMap<>();
  }

  public void addPattern(String pattern, String replacement) {
    this.patterns.put(Pattern.compile(pattern), replacement);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if (PATTERNFILE == null) {
      return;
    }
    Resource patRes = new ClassPathResource(PATTERNFILE);
    if (patRes.exists()) {
      Properties props = new Properties();
      try {
        props.load(patRes.getInputStream());
      } catch (IOException e) {
        LOGGER.error("Could not read pattern file.", e);
        return;
      }
      for (Map.Entry<Object, Object> entry : props.entrySet()) {
        String pattern = (String) entry.getKey();
        String replacement = (String) entry.getValue();
        this.addPattern(pattern, replacement);
      }
    }
  }

  public Map<Pattern, String> getPatterns() {
    return patterns;
  }

  @Override
  public URI getURI(String identifier) throws ResolvingException {
    URI uri = null;
    for (Map.Entry<Pattern, String> entry : this.patterns.entrySet()) {
      String sourcePath = entry.getKey().matcher(identifier).replaceAll(entry.getValue());
      if (sourcePath.equals(identifier)) {
        continue;
      }
      try {
        uri = new URI(sourcePath);
        if (!uri.isAbsolute()) {
          uri = new URI("file:" + sourcePath);
        }
        break;
      } catch (URISyntaxException e) {
        LOGGER.error("Invalid URI", e);
      }
    }
    if (uri == null) {
      throw new ResolvingException("No pattern matched.");
    }
    return uri;
  }

  @Override
  public boolean isResolvable(String identifier) {
    for (Pattern p : this.patterns.keySet()) {
      if (p.matcher(identifier).matches()) {
        return true;
      }
    }
    return false;
  }

}
