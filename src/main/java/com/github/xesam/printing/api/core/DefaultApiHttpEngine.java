package com.github.xesam.printing.api.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.DefaultConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.hc.core5.util.TimeValue;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public final class DefaultApiHttpEngine implements ApiHttpEngine {
    private static final class SimpleConnectionKeepAliveStrategy extends DefaultConnectionKeepAliveStrategy {
        private static final TimeValue TIMEOUT = TimeValue.ofSeconds(15);

        @Override
        public TimeValue getKeepAliveDuration(final HttpResponse response, final HttpContext context) {
            TimeValue timeValue = super.getKeepAliveDuration(response, context);
            return timeValue.min(TIMEOUT);
        }
    }

    private static CloseableHttpClient createDefault() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(5, TimeUnit.SECONDS)
                .setConnectTimeout(5, TimeUnit.SECONDS)
                .setResponseTimeout(5, TimeUnit.SECONDS)
                .build();
        return HttpClients.custom()
                .setKeepAliveStrategy(new SimpleConnectionKeepAliveStrategy())
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    private CloseableHttpClient httpClient;

    public DefaultApiHttpEngine() {
        this(createDefault());
    }

    public DefaultApiHttpEngine(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    private ApiHttpResponse sendRequest(HttpUriRequestBase request, Map<String, String> headers) {
        if (headers != null) {
            headers.forEach(request::addHeader);
        }
        try (CloseableHttpResponse httpResponse = this.httpClient.execute(request)) {
            if (httpResponse.getCode() != HttpStatus.SC_OK) {
                return new ApiHttpResponse(new ApiHttpException(httpResponse.getCode()));
            }
            String responseString = EntityUtils.toString(httpResponse.getEntity());
            return new ApiHttpResponse(responseString);
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiHttpResponse(e);
        }
    }

    private List<NameValuePair> buildParameterPairs(Map<String, Object> data) {
        return data.entrySet().stream()
                .map(ele -> new BasicNameValuePair(ele.getKey(), String.valueOf(ele.getValue())))
                .collect(Collectors.toList());
    }

    private ApiHttpResponse sendUrlParamsRequest(HttpUriRequestBase request, Map<String, Object> params, Map<String, String> headers) {
        List<NameValuePair> pairs = buildParameterPairs(params);
        URI uri;
        try {
            uri = new URIBuilder(request.getUri())
                    .addParameters(pairs)
                    .build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return new ApiHttpResponse(e);
        }
        request.setUri(uri);
        return sendRequest(request, headers);
    }

    private ApiHttpResponse sendFormRequest(HttpUriRequestBase request, Map<String, Object> formData, Map<String, String> headers) {
        List<NameValuePair> pairs = buildParameterPairs(formData);
        request.setEntity(new UrlEncodedFormEntity(pairs, StandardCharsets.UTF_8));
        return sendRequest(request, headers);
    }

    @Override
    public ApiHttpResponse doGet(String url, Map<String, Object> params, Map<String, String> headers) {
        HttpGet request = new HttpGet(url);
        return sendUrlParamsRequest(request, params, headers);
    }

    private ApiHttpResponse doPostJson(String url, Map<String, Object> body, Map<String, String> headers) {
        HttpPost request = new HttpPost(url);
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonDataString = "";
        try {
            jsonDataString = objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        request.setEntity(new StringEntity(jsonDataString, StandardCharsets.UTF_8));
        return sendRequest(request, headers);
    }

    private ApiHttpResponse doPostForm(String url, Map<String, Object> body, Map<String, String> headers) {
        HttpPost request = new HttpPost(url);
        return sendFormRequest(request, body, headers);
    }

    private boolean useJson(Map<String, String> headers) {
        String contentType = headers.get("Content-Type");
        if (contentType == null) {
            return false;
        }
        String application = contentType.split(";")[0];
        return application.trim().equalsIgnoreCase("application/json");
    }

    @Override
    public ApiHttpResponse doPost(String url, Map<String, Object> body, Map<String, String> headers) {
        if (this.useJson(headers)) {
            return this.doPostJson(url, body, headers);
        } else {
            return this.doPostForm(url, body, headers);
        }
    }

    @Override
    public ApiHttpResponse doDelete(String url, Map<String, Object> params, Map<String, String> headers) {
        HttpDelete request = new HttpDelete(url);
        return sendUrlParamsRequest(request, params, headers);
    }

    @Override
    public ApiHttpResponse doPatch(String url, Map<String, Object> body, Map<String, String> headers) {
        HttpPatch request = new HttpPatch(url);
        return sendFormRequest(request, body, headers);
    }
}
