package com.github.xesam.printing.api.core;

public class ApiHttpResponse {
    private final boolean _success;
    private String value;
    private Exception exception;

    public ApiHttpResponse(String body) {
        this._success = true;
        this.value = body;
    }

    public ApiHttpResponse(Exception e) {
        this._success = false;
        this.exception = e;
    }

    public boolean isSuccess() {
        return this._success;
    }

    public Exception getException() {
        return exception;
    }

    public String getValue() {
        return this.value;
    }
}
