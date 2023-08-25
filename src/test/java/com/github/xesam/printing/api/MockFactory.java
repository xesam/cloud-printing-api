package com.github.xesam.printing.api;

import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.mockito.stubbing.Answer;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class MockFactory {
    public static CloseableHttpClient createMockHttpClientWithHttpCodeIsNot200() {
        CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
        try {
            CloseableHttpResponse response = mock(CloseableHttpResponse.class);
            when(response.getCode()).thenReturn(201);
            when(httpClient.execute(any(HttpUriRequestBase.class))).thenReturn(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return httpClient;
    }

    public static CloseableHttpClient createMockHttpClientWithHttpCodeIs200() {
        CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
        try {
            when(mockHttpClient.execute(any(HttpUriRequestBase.class))).thenAnswer((Answer<CloseableHttpResponse>) invocationOnMock -> {
                HttpUriRequestBase httpUriRequestBase = invocationOnMock.getArgument(0);
                CloseableHttpResponse theAnswerResponse = mock(CloseableHttpResponse.class);
                when(theAnswerResponse.getCode()).thenReturn(200);
                when(theAnswerResponse.getHeaders()).thenReturn(httpUriRequestBase.getHeaders());
                when(theAnswerResponse.getEntity()).thenReturn(httpUriRequestBase.getEntity());
                return theAnswerResponse;
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return mockHttpClient;
    }
}
