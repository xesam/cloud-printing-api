package com.github.xesam.printing.api.feie;

import com.github.xesam.printing.api.core.*;

import java.util.HashMap;
import java.util.Map;

public final class FeieApiHttpClient implements ApiHttpClient {

    public static final String BASE_URL = "https://api.feieyun.cn/Api/Open/";

    private ApiHttpEngine apiHttpEngine;
    private ApiAuth apiAuth;
    private ApiClock apiClock;
    private final ApiSignature signature = new ApiSha1Signature();

    public FeieApiHttpClient() {
        this(new DefaultApiHttpEngine());
    }

    public FeieApiHttpClient(ApiHttpEngine apiHttpEngine) {
        this(null, null, apiHttpEngine);
    }

    public FeieApiHttpClient(ApiAuth apiAuth) {
        this(apiAuth, new DefaultApiClock(), new DefaultApiHttpEngine());
    }

    public FeieApiHttpClient(ApiAuth apiAuth, ApiHttpEngine apiHttpEngine) {
        this(apiAuth, new DefaultApiClock(), apiHttpEngine);
    }

    public FeieApiHttpClient(ApiAuth apiAuth, ApiClock apiClock, ApiHttpEngine apiHttpEngine) {
        this.apiAuth = apiAuth;
        this.apiClock = apiClock;
        this.apiHttpEngine = apiHttpEngine;
    }

    private Map<String, String> createRequestHeaders() {
        Map<String, String> defaultHeaders = new HashMap<>();
        defaultHeaders.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        return defaultHeaders;
    }

    public ApiHttpResponse send(String url, Map<String, Object> data) {
        if (!data.containsKey("stime")) {
            data.put("stime", apiClock.getEpochSecond());
        }
        if (!data.containsKey("sig")) {
            data.put("user", apiAuth.getAppId());
            data.put("sig", signature.getSignature(apiAuth.getAppId(), apiAuth.getSecret(), data.get("stime").toString()));
        }
        Map<String, String> defaultHeaders = this.createRequestHeaders();
        return this.apiHttpEngine.doPost(url, data, defaultHeaders);
    }

    public ApiHttpResponse addPrinter(Map<String, Object> data) {
        data.put("apiname", "Open_printerAddlist");
        return this.send(BASE_URL, data);
    }

    public ApiHttpResponse deletePrinter(Map<String, Object> data) {
        data.put("apiname", "Open_printerDelList");
        return this.send(BASE_URL, data);
    }

    public ApiHttpResponse updatePrinter(Map<String, Object> data) {
        data.put("apiname", "Open_printerEdit");
        return this.send(BASE_URL, data);
    }

    public ApiHttpResponse updatePrinterSetting(Map<String, Object> data) {
        return new ApiHttpResponse(new ApiUnsupportedException());
    }

    public ApiHttpResponse queryPrinter(Map<String, Object> data) {
        data.put("apiname", "Open_queryPrinterStatus");
        return this.send(BASE_URL, data);
    }

    public ApiHttpResponse printMsgOrder(Map<String, Object> data) {
        data.put("apiname", "Open_printMsg");
        return this.send(BASE_URL, data);
    }

    public ApiHttpResponse printLabelOrder(Map<String, Object> data) {
        data.put("apiname", "Open_printLabelMsg");
        return this.send(BASE_URL, data);
    }

    public ApiHttpResponse queryOrder(Map<String, Object> data) {
        data.put("apiname", "Open_queryOrderState");
        return this.send(BASE_URL, data);
    }

    public ApiHttpResponse clearPrinterOrders(Map<String, Object> data) {
        data.put("apiname", "Open_delPrinterSqs");
        return this.send(BASE_URL, data);
    }

    public ApiHttpResponse queryPrinterOrderStats(Map<String, Object> data) {
        data.put("apiname", "Open_queryOrderInfoByDate");
        return this.send(BASE_URL, data);
    }
}
