package de.digitalcollections.iiif.image.business.api.service;

public interface ImageSecurityService {

  boolean isAccessAllowed(String identifier);

}
