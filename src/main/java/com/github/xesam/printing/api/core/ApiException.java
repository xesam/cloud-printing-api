package com.github.xesam.printing.api.core;

public class ApiException extends Exception {

    public ApiException() {
        super();
    }

    public ApiException(String message) {
        super(message);
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public int getErrorCode() {
        return -1;
    }

}
