package de.digitalcollections.iiif.image.backend.api.resolver;

import de.digitalcollections.iiif.image.model.api.exception.ResolvingException;
import java.net.URI;

public interface ImageResolver {

    public URI getURI(String identifier) throws ResolvingException;

    public boolean isResolvable(String identifier);
}
