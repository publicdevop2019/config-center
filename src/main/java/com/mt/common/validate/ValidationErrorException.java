package com.mt.common.validate;

public class ValidationErrorException extends RuntimeException {
    public ValidationErrorException(Throwable cause) {
        super("error during validation api call", cause);
    }
}
