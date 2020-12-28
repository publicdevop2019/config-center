package com.mt.common.validation;

public class ValidationErrorException extends RuntimeException {
    public ValidationErrorException(Throwable cause) {
        super("error during validation api call", cause);
    }
}
