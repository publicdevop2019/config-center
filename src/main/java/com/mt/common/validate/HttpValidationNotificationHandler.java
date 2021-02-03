package com.mt.common.validate;

public class HttpValidationNotificationHandler implements ValidationNotificationHandler {
    public void handleError(String error) {
        throw new IllegalArgumentException(error);
    }
}
