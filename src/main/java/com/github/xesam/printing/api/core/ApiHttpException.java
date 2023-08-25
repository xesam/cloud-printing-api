package com.github.xesam.printing.api.core;

public final class ApiHttpException extends ApiException {
    private final int errorCode;

    public ApiHttpException(int httpCode) {
        super("httpCode is " + httpCode);
        this.errorCode = httpCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
