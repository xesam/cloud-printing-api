package com.github.xesam.printing.api.core;

public final class ApiAuth {
    private final String appId;
    private final String secret;

    public ApiAuth(String appId, String secret) {
        this.appId = appId;
        this.secret = secret;
    }

    public String getAppId() {
        return appId;
    }

    public String getSecret() {
        return secret;
    }
}
