package de.digitalcollections.iiif.image.frontend.impl.springmvc.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.INTERNAL_SERVER_ERROR)
public class TransformationException extends Exception {
  public TransformationException(String message) {
    super(message);
  }
}
