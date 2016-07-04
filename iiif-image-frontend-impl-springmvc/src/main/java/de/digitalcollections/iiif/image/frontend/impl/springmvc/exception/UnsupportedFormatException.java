package de.digitalcollections.iiif.image.frontend.impl.springmvc.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.UNSUPPORTED_MEDIA_TYPE)
public class UnsupportedFormatException extends Exception {
    public UnsupportedFormatException(String message) {
        super(message);
    }

    public UnsupportedFormatException() {
        super();
    }
}
