package io.github.xesam.cloud.api.core;

public interface ApiSignature {
    String getSignature(String... contents);
}
