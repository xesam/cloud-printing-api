package io.github.xesam.cloud.api.core;

public final class ApiUnsupportedException extends ApiException {
    public ApiUnsupportedException() {
        super();
    }

    @Override
    public int getErrorCode() {
        return -2;
    }
}
