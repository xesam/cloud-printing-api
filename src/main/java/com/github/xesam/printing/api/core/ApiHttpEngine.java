package com.github.xesam.printing.api.core;

import java.util.Map;

public interface ApiHttpEngine {
    ApiHttpResponse doGet(String url, Map<String, Object> params, Map<String, String> headers);

    ApiHttpResponse doPost(String url, Map<String, Object> body, Map<String, String> headers);

    ApiHttpResponse doDelete(String url, Map<String, Object> params, Map<String, String> headers);

    ApiHttpResponse doPatch(String url, Map<String, Object> body, Map<String, String> headers);
}
