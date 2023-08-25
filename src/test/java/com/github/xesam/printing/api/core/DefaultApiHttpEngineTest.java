package com.github.xesam.printing.api.core;

import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

public class DefaultApiHttpEngineTest {
    private static final String TEST_URL = "https://test1.test2.test3/test4";

    private static CloseableHttpClient createMockHttpClientWithHttpCodeIsNot200() {
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

    private static CloseableHttpClient createMockHttpClientWithHttpCodeIs200() {
        CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
        try {
            CloseableHttpResponse response = mock(CloseableHttpResponse.class);
            when(response.getCode()).thenReturn(200);
            when(response.getEntity()).thenReturn(new StringEntity(""));
            when(httpClient.execute(any(HttpUriRequestBase.class))).thenReturn(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return httpClient;
    }

    private static Map<String, Object> createRequestData() {
        return new HashMap<String, Object>() {{
            put("user", "this_is_user");
            put("stime", "this_is_stime");
            put("sig", "this_is_sig");
            put("debug", "this_is_debug");
            put("apiname", "this_is_apiname");
            put("printerContent", "this_is_printerContent");
        }};
    }

    private static Map<String, String> createRequestHeaders() {
        return new HashMap<String, String>() {{
            put("test_header_key1", "test_header_value1");
            put("test_header_key2", "test_header_value2");
        }};
    }

    @Test
    public void when_response_code_is_not_200_then_return_fail_res() {
        DefaultApiHttpEngine requestClient = new DefaultApiHttpEngine(createMockHttpClientWithHttpCodeIsNot200());
        ApiHttpResponse res = requestClient.doPost(TEST_URL, createRequestData(), createRequestHeaders());

        Assert.assertFalse(res.isSuccess());
        Assert.assertTrue(res.getException() instanceof ApiHttpException);
        Assert.assertEquals(((ApiHttpException) res.getException()).getErrorCode(), 201);
    }

    @Test
    public void when_response_code_is_200_then_return_success_res() {
        DefaultApiHttpEngine requestClient = new DefaultApiHttpEngine(createMockHttpClientWithHttpCodeIs200());
        ApiHttpResponse res = requestClient.doPost(TEST_URL, createRequestData(), createRequestHeaders());

        Assert.assertTrue(res.isSuccess());
    }

    @Test
    public void when_send_with_custom_headers_then_add_headers_to_request() throws IOException {
        CloseableHttpClient client = createMockHttpClientWithHttpCodeIs200();
        DefaultApiHttpEngine requestClient = new DefaultApiHttpEngine(client);

        requestClient.doPost(TEST_URL, createRequestData(), createRequestHeaders());

        verify(client).execute(argThat(request -> {
            try {
                assertThat(request.getHeader("test_header_key1").getValue(), equalToIgnoringCase(createRequestHeaders().get("test_header_key1")));
                assertThat(request.getHeader("test_header_key2").getValue(), equalToIgnoringCase(createRequestHeaders().get("test_header_key2")));
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }));
    }

    @Test
    public void when_post_without_header_then_send_request_with_form_urlencoded_body() throws IOException {
        CloseableHttpClient client = createMockHttpClientWithHttpCodeIs200();
        DefaultApiHttpEngine requestClient = new DefaultApiHttpEngine(client);

        requestClient.doPost(TEST_URL, createRequestData(), new HashMap<>());

        verify(client).execute(argThat(request -> {
            try {
                assertThat(request.getUri().toString(), equalTo(TEST_URL));
                assertThat(request.getMethod(), equalToIgnoringCase("post"));
                assertThat(request.getEntity().getClass(), is(UrlEncodedFormEntity.class));
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }));
    }

    @Test
    public void when_post_with_json_header_then_send_request_with_json_body() throws IOException {
        CloseableHttpClient client = createMockHttpClientWithHttpCodeIs200();
        DefaultApiHttpEngine requestClient = new DefaultApiHttpEngine(client);

        requestClient.doPost(TEST_URL, createRequestData(), new HashMap<String, String>() {{
            put("Content-Type", "application/json;charset=UTF-8");
        }});

        verify(client).execute(argThat(request -> {
            try {
                assertThat(request.getUri().toString(), equalTo(TEST_URL));
                assertThat(request.getMethod(), equalToIgnoringCase("post"));
                assertThat(request.getEntity().getClass(), is(StringEntity.class));
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }));
    }

    @Test
    public void when_do_delete_then_the_method_of_request_is_delete() throws IOException {
        CloseableHttpClient client = createMockHttpClientWithHttpCodeIs200();
        DefaultApiHttpEngine requestClient = new DefaultApiHttpEngine(client);

        requestClient.doDelete(TEST_URL, createRequestData(), new HashMap<>());

        verify(client).execute(argThat(request -> {
            try {
                assertThat(request.getMethod(), equalToIgnoringCase("delete"));
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }));
    }

    @Test
    public void when_do_delete_then_add_params_to_url() throws IOException {
        CloseableHttpClient client = createMockHttpClientWithHttpCodeIs200();
        DefaultApiHttpEngine requestClient = new DefaultApiHttpEngine(client);

        requestClient.doDelete(TEST_URL, createRequestData(), new HashMap<>());

        verify(client).execute(argThat(request -> {
            try {
                assertThat(request.getPath(), containsString("stime=this_is_stime"));
                assertThat(request.getPath(), containsString("debug=this_is_debug"));
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }));
    }

    @Test
    public void when_do_patch_then_the_method_of_request_is_patch() throws IOException {
        CloseableHttpClient client = createMockHttpClientWithHttpCodeIs200();
        DefaultApiHttpEngine requestClient = new DefaultApiHttpEngine(client);

        requestClient.doPatch(TEST_URL, createRequestData(), new HashMap<>());

        verify(client).execute(argThat(request -> {
            try {
                assertThat(request.getMethod(), equalToIgnoringCase("patch"));
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }));
    }

    @Test
    public void when_do_patch_then_add_params_to_body() throws IOException {
        CloseableHttpClient client = createMockHttpClientWithHttpCodeIs200();
        DefaultApiHttpEngine requestClient = new DefaultApiHttpEngine(client);

        requestClient.doPatch(TEST_URL, createRequestData(), new HashMap<>());

        verify(client).execute(argThat(request -> {
            try {
                assertThat(request.getEntity().getClass(), is(UrlEncodedFormEntity.class));
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }));
    }


    @Test
    public void when_do_get_then_the_method_of_request_is_get() throws IOException {
        CloseableHttpClient client = createMockHttpClientWithHttpCodeIs200();
        DefaultApiHttpEngine requestClient = new DefaultApiHttpEngine(client);

        requestClient.doGet(TEST_URL, createRequestData(), new HashMap<>());

        verify(client).execute(argThat(request -> {
            try {
                assertThat(request.getMethod(), equalToIgnoringCase("get"));
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }));
    }

    @Test
    public void when_do_get_then_add_params_to_url() throws IOException {
        CloseableHttpClient client = createMockHttpClientWithHttpCodeIs200();
        DefaultApiHttpEngine requestClient = new DefaultApiHttpEngine(client);

        requestClient.doGet(TEST_URL, createRequestData(), new HashMap<>());

        verify(client).execute(argThat(request -> {
            try {
                assertThat(request.getPath(), containsString("stime=this_is_stime"));
                assertThat(request.getPath(), containsString("debug=this_is_debug"));
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }));
    }
}
