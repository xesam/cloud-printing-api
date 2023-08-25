package com.github.xesam.printing.api.spyun;

import com.github.xesam.printing.api.core.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class SpyunApiHttpClient implements ApiHttpClient {
    public static final String BASE_URL = "https://open.spyun.net/v1/printer/";

    private ApiHttpEngine apiHttpEngine;
    private ApiAuth apiAuth;
    private final ApiClock apiClock;
    private final ApiSignature signature = new ApiMd5Signature();

    public SpyunApiHttpClient() {
        this(new DefaultApiHttpEngine());
    }

    public SpyunApiHttpClient(ApiHttpEngine apiHttpEngine) {
        this(null, null, apiHttpEngine);
    }

    public SpyunApiHttpClient(ApiAuth apiAuth) {
        this(apiAuth, new DefaultApiClock(), new DefaultApiHttpEngine());
    }

    public SpyunApiHttpClient(ApiAuth apiAuth, ApiHttpEngine apiHttpEngine) {
        this(apiAuth, new DefaultApiClock(), apiHttpEngine);
    }

    public SpyunApiHttpClient(ApiAuth apiAuth, ApiClock apiClock, ApiHttpEngine apiHttpEngine) {
        this.apiAuth = apiAuth;
        this.apiClock = apiClock;
        this.apiHttpEngine = apiHttpEngine;
    }

    private String getFullUrl(String path) {
        return SpyunApiHttpClient.BASE_URL + path;
    }

    private Map<String, String> createRequestHeaders() {
        Map<String, String> defaultHeaders = new HashMap<>();
        defaultHeaders.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        return defaultHeaders;
    }

    private Map<String, Object> createRequestData(Map<String, Object> data) {
        if (!data.containsKey("timestamp")) {
            data.put("timestamp", apiClock.getEpochSecond());
        }
        data.entrySet().removeIf(ele -> Objects.isNull(ele.getValue()) || ele.getValue().toString().trim().isEmpty());

        if (!data.containsKey("sign")) {
            data.put("appid", apiAuth.getAppId());
            String paramStr = data.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(ele -> ele.getKey() + "=" + ele.getValue())
                    .collect(Collectors.joining("&"));
            data.put("sign", signature.getSignature(paramStr + "&appsecret=" + this.apiAuth.getSecret()));
        }
        return data;
    }

    public ApiHttpResponse send(String url, String method, Map<String, Object> data) {
        Map<String, Object> fulfilledData = this.createRequestData(data);
        Map<String, String> defaultHeaders = this.createRequestHeaders();
        switch (method.toUpperCase()) {
            case "POST":
                return this.apiHttpEngine.doPost(url, fulfilledData, defaultHeaders);
            case "DELETE":
                return this.apiHttpEngine.doDelete(url, fulfilledData, defaultHeaders);
            case "PATCH":
                return this.apiHttpEngine.doPatch(url, fulfilledData, defaultHeaders);
            default:
                return this.apiHttpEngine.doGet(url, fulfilledData, defaultHeaders);
        }
    }

    public ApiHttpResponse addPrinter(Map<String, Object> data) {
        return this.send(this.getFullUrl("add"), "post", data);
    }

    public ApiHttpResponse deletePrinter(Map<String, Object> data) {
        return this.send(this.getFullUrl("delete"), "delete", data);
    }

    public ApiHttpResponse updatePrinter(Map<String, Object> data) {
        return this.send(this.getFullUrl("update"), "patch", data);
    }

    public ApiHttpResponse updatePrinterSetting(Map<String, Object> data) {
        return this.send(this.getFullUrl("setting"), "patch", data);
    }

    public ApiHttpResponse queryPrinter(Map<String, Object> data) {
        return this.send(this.getFullUrl("info"), "get", data);
    }

    public ApiHttpResponse printMsgOrder(Map<String, Object> data) {
        return this.send(this.getFullUrl("print"), "post", data);
    }

    public ApiHttpResponse printLabelOrder(Map<String, Object> data) {
        return new ApiHttpResponse(new ApiUnsupportedException());
    }

    public ApiHttpResponse queryOrder(Map<String, Object> data) {
        return this.send(this.getFullUrl("order/status"), "get", data);
    }

    public ApiHttpResponse clearPrinterOrders(Map<String, Object> data) {
        return this.send(this.getFullUrl("cleansqs"), "delete", data);
    }

    public ApiHttpResponse queryPrinterOrderStats(Map<String, Object> data) {
        return this.send(this.getFullUrl("order/number"), "get", data);
    }
}
