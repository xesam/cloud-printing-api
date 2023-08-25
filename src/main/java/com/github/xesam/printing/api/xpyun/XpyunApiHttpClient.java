package com.github.xesam.printing.api.xpyun;

import com.github.xesam.printing.api.core.*;

import java.util.HashMap;
import java.util.Map;

public class XpyunApiHttpClient implements ApiHttpClient {
    public static final String BASE_URL = "https://open.xpyun.net/api/openapi/xprinter/";

    private ApiHttpEngine apiHttpEngine;
    private ApiAuth apiAuth;
    private final ApiClock apiClock;
    private final ApiSignature signature = new ApiSha1Signature();

    public XpyunApiHttpClient() {
        this(new DefaultApiHttpEngine());
    }

    public XpyunApiHttpClient(ApiHttpEngine apiHttpEngine) {
        this(null, null, apiHttpEngine);
    }

    public XpyunApiHttpClient(ApiAuth apiAuth) {
        this(apiAuth, new DefaultApiClock(), new DefaultApiHttpEngine());
    }

    public XpyunApiHttpClient(ApiAuth apiAuth, ApiHttpEngine apiHttpEngine) {
        this(apiAuth, new DefaultApiClock(), apiHttpEngine);
    }

    public XpyunApiHttpClient(ApiAuth apiAuth, ApiClock apiClock, ApiHttpEngine apiHttpEngine) {
        this.apiAuth = apiAuth;
        this.apiClock = apiClock;
        this.apiHttpEngine = apiHttpEngine;
    }

    private Map<String, String> createRequestHeaders() {
        Map<String, String> defaultHeaders = new HashMap<>();
        defaultHeaders.put("Content-Type", "application/json;charset=UTF-8");
        return defaultHeaders;
    }

    private String getFullUrl(String path) {
        return BASE_URL + path;
    }

    public ApiHttpResponse send(String url, Map<String, Object> data) {
        if (!data.containsKey("timestamp")) {
            data.put("timestamp", apiClock.getEpochSecond());
        }
        if (!data.containsKey("sign")) {
            data.put("user", apiAuth.getAppId());
            data.put("sign", signature.getSignature(apiAuth.getAppId(), apiAuth.getSecret(), data.get("timestamp").toString()));
        }
        Map<String, String> defaultHeaders = this.createRequestHeaders();
        return this.apiHttpEngine.doPost(url, data, defaultHeaders);
    }

    public ApiHttpResponse addPrinter(Map<String, Object> data) {
        return this.send(getFullUrl("addPrinters"), data);
    }


    public ApiHttpResponse deletePrinter(Map<String, Object> data) {
        return this.send(getFullUrl("delPrinters"), data);
    }

    public ApiHttpResponse updatePrinter(Map<String, Object> data) {
        return this.send(getFullUrl("updPrinter"), data);
    }

    public ApiHttpResponse updatePrinterSetting(Map<String, Object> data) {
        return this.send(getFullUrl("setVoiceType"), data);
    }

    public ApiHttpResponse queryPrinter(Map<String, Object> data) {
        return this.send(getFullUrl("queryPrinterStatus"), data);
    }

    public ApiHttpResponse printMsgOrder(Map<String, Object> data) {
        return this.send(getFullUrl("print"), data);
    }

    public ApiHttpResponse printLabelOrder(Map<String, Object> data) {
        return this.send(getFullUrl("printLabel"), data);
    }

    public ApiHttpResponse queryOrder(Map<String, Object> data) {
        return this.send(getFullUrl("queryOrderState"), data);
    }

    public ApiHttpResponse clearPrinterOrders(Map<String, Object> data) {
        return this.send(getFullUrl("delPrinterQueue"), data);
    }

    public ApiHttpResponse queryPrinterOrderStats(Map<String, Object> data) {
        return this.send(getFullUrl("queryOrderStatis"), data);
    }

}
