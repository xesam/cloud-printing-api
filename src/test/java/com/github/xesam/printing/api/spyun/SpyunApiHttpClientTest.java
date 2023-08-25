package com.github.xesam.printing.api.spyun;

import com.github.xesam.printing.api.core.*;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class SpyunApiHttpClientTest {
    @Test
    public void canary() {
        Assert.assertTrue(true);
    }

    private static final ApiAuth TEST_API_AUTH = new ApiAuth("test_id", "test_secret");
    private static final ApiClock TEST_API_CLOCK = () -> 1000000000;

    private static ApiHttpEngine createTestHttpClient() {
        ApiHttpEngine client = mock(DefaultApiHttpEngine.class);
        when(client.doPost(anyString(), anyMap(), anyMap())).thenReturn(new ApiHttpResponse(""));
        return client;
    }

    private static SpyunApiHttpClient createTestApiClient(ApiHttpEngine httpClient) {
        SpyunApiHttpClient client = new SpyunApiHttpClient(TEST_API_AUTH, TEST_API_CLOCK, httpClient);
        return spy(client);
    }

    private static SpyunApiHttpClient createTestApiClient() {
        ApiHttpEngine testHttpClient = createTestHttpClient();
        return createTestApiClient(testHttpClient);
    }

    private static Map<String, Object> createRequestDataWithoutSigAndTime() {
        return new HashMap<String, Object>() {{
            put("appid", "this_is_appid");
        }};
    }

    private static Map<String, Object> createRequestDataWithoutSig() {
        Map<String, Object> data = createRequestDataWithoutSigAndTime();
        data.put("timestamp", "1000000000");
        return data;
    }

    private static Map<String, Object> createRequestData() {
        Map<String, Object> data = createRequestDataWithoutSig();
        data.put("sign", "this_is_sig");
        return data;
    }

    private void checkHeader(Map<String, String> headers) {
        assertThat(headers.get("Content-Type"), equalToIgnoringCase("application/x-www-form-urlencoded; charset=UTF-8"));
    }

    @Test
    public void when_send_request_then_auto_add_header() {
        ApiHttpEngine testHttpClient = createTestHttpClient();
        SpyunApiHttpClient testApiClient = createTestApiClient(testHttpClient);

        testApiClient.send("", "post", createRequestData());

        verify(testHttpClient).doPost(eq(""), isA(Map.class), argThat(headers -> {
            checkHeader(headers);
            return true;
        }));
    }

    private void checkSig(Map<String, Object> data) {
        assertThat(data.get("sign").toString(), equalTo("05BCCF94FAAFBD70150A1E697D563725"));
    }

    @Test
    public void when_send_without_signature_then_auto_add_signature_with_time_in_request() {
        ApiHttpEngine testHttpClient = createTestHttpClient();
        SpyunApiHttpClient testApiClient = createTestApiClient(testHttpClient);

        testApiClient.send("", "post", createRequestDataWithoutSig());

        verify(testHttpClient).doPost(eq(""), argThat((Map<String, Object> data) -> {
            checkSig(data);
            return true;
        }), isA(Map.class));
    }

    @Test
    public void when_send_without_signature_and_time_then_auto_add_signature_with_cloud_clock_time() {
        ApiHttpEngine testHttpClient = createTestHttpClient();
        SpyunApiHttpClient testApiClient = createTestApiClient(testHttpClient);

        testApiClient.send("", "post", createRequestDataWithoutSigAndTime());

        verify(testHttpClient).doPost(eq(""), argThat((Map<String, Object> data) -> {
            checkSig(data);
            return true;
        }), isA(Map.class));
    }

    private static String getFullUrl(String path) {
        return SpyunApiHttpClient.BASE_URL + path;
    }

    private static void checkAddPrinterData(Map<String, Object> data) {
        assertThat(data.get("sn").toString(), equalTo("this_id_sn"));
        assertThat(data.get("pkey").toString(), equalTo("this_is_pkey"));
        assertThat(data.get("name").toString(), equalTo("this_is_name"));
    }

    @Test
    public void when_add_printer_then_use_add_api_and_params() {
        ApiHttpEngine testHttpClient = createTestHttpClient();
        SpyunApiHttpClient testApiClient = createTestApiClient(testHttpClient);
        Map<String, Object> addPrintersData = new HashMap<String, Object>() {{
            put("sn", "this_id_sn");
            put("pkey", "this_is_pkey");
            put("name", "this_is_name");
        }};

        testApiClient.addPrinter(addPrintersData);

        verify(testApiClient).send(
                eq(getFullUrl("add")),
                eq("post"),
                argThat((Map<String, Object> data) -> {
                    checkAddPrinterData(data);
                    return true;
                })
        );
    }


    private static void checkDeletePrinterData(Map<String, Object> data) {
        assertThat(data.get("sn").toString(), equalTo("this_is_sn"));
    }

    @Test
    public void when_delete_printer_then_use_delete_api_and_params() {
        ApiHttpEngine testHttpClient = createTestHttpClient();
        SpyunApiHttpClient testApiClient = createTestApiClient(testHttpClient);
        Map<String, Object> mapData = new HashMap<String, Object>() {{
            put("sn", "this_is_sn");
        }};

        testApiClient.deletePrinter(mapData);

        verify(testApiClient).send(
                eq(getFullUrl("delete")),
                eq("delete"),
                argThat(data -> {
                    checkDeletePrinterData(data);
                    return true;
                }));
    }


    private static void checkUpdatePrinterData(Map<String, Object> data) {
        assertThat(data.get("sn").toString(), equalTo("this_is_sn"));
        assertThat(data.get("name").toString(), equalTo("this_is_name"));
    }

    @Test
    public void when_update_printer_then_use_update_api_and_params() {
        ApiHttpEngine testHttpClient = createTestHttpClient();
        SpyunApiHttpClient testApiClient = createTestApiClient(testHttpClient);
        Map<String, Object> mapData = new HashMap<String, Object>() {{
            put("sn", "this_is_sn");
            put("name", "this_is_name");
        }};

        testApiClient.updatePrinter(mapData);

        verify(testApiClient).send(
                eq(getFullUrl("update")),
                eq("patch"),
                argThat(data -> {
                    checkUpdatePrinterData(data);
                    return true;
                }));
    }

    private static void checkUpdatePrinterSettingData(Map<String, Object> data) {
        assertThat(data.get("sn").toString(), equalTo("this_is_sn"));
        assertThat(data.get("auto_cut"), equalTo(1));
        assertThat(data.get("voice").toString(), equalTo("N"));
    }

    @Test
    public void when_update_printer_setting_then_use_update_setting_api_and_params() {
        ApiHttpEngine testHttpClient = createTestHttpClient();
        SpyunApiHttpClient testApiClient = createTestApiClient(testHttpClient);
        Map<String, Object> mapData = new HashMap<String, Object>() {{
            put("sn", "this_is_sn");
            put("auto_cut", 1);
            put("voice", "N");
        }};

        testApiClient.updatePrinterSetting(mapData);

        verify(testApiClient).send(
                eq(getFullUrl("setting")),
                eq("patch"),
                argThat(data -> {
                    checkUpdatePrinterSettingData(data);
                    return true;
                }));
    }

    private static void checkQueryPrinterData(Map<String, Object> data) {
        assertThat(data.get("sn").toString(), equalTo("this_is_sn"));
    }

    @Test
    public void when_query_printer_then_use_query_api_and_params() {
        ApiHttpEngine testHttpClient = createTestHttpClient();
        SpyunApiHttpClient testApiClient = createTestApiClient(testHttpClient);
        Map<String, Object> mapData = new HashMap<String, Object>() {{
            put("sn", "this_is_sn");
        }};

        testApiClient.queryPrinter(mapData);

        verify(testApiClient).send(
                eq(getFullUrl("info")),
                eq("get"),
                argThat(data -> {
                    checkQueryPrinterData(data);
                    return true;
                }));
    }

    private static void checkPrintMsgOrderData(Map<String, Object> data) {
        assertThat(data.get("sn").toString(), equalTo("this_is_sn"));
        assertThat(data.get("content").toString(), equalTo("this_is_content"));
        assertThat(data.get("times"), equalTo(1));
    }

    @Test
    public void when_print_msg_order_then_use_print_msg_api_and_params() {
        ApiHttpEngine testHttpClient = createTestHttpClient();
        SpyunApiHttpClient testApiClient = createTestApiClient(testHttpClient);
        Map<String, Object> mapData = new HashMap<String, Object>() {{
            put("sn", "this_is_sn");
            put("content", "this_is_content");
            put("times", 1);
        }};

        testApiClient.printMsgOrder(mapData);

        verify(testApiClient).send(
                eq(getFullUrl("print")),
                eq("post"),
                argThat(data -> {
                    checkPrintMsgOrderData(data);
                    return true;
                }));
    }

    @Test
    public void when_print_label_order_then_use_print_label_api_and_params() {
        ApiHttpEngine testHttpClient = createTestHttpClient();
        SpyunApiHttpClient testApiClient = createTestApiClient(testHttpClient);
        Map<String, Object> mapData = new HashMap<String, Object>() {{
            put("sn", "this_is_sn");
            put("content", "this_is_content");
            put("times", 1);
        }};

        ApiHttpResponse response = testApiClient.printLabelOrder(mapData);
        assertThat(response.isSuccess(), equalTo(false));
        assertThat(response.getException(), Matchers.isA(ApiUnsupportedException.class));
    }

    private static void checkQueryOrderData(Map<String, Object> data) {
        assertThat(data.get("id").toString(), equalTo("this_is_orderid"));
    }

    @Test
    public void when_query_order_then_use_query_api_and_params() {
        SpyunApiHttpClient testApiClient = createTestApiClient();
        Map<String, Object> mapData = new HashMap<String, Object>() {{
            put("id", "this_is_orderid");
        }};

        testApiClient.queryOrder(mapData);

        verify(testApiClient).send(
                eq(getFullUrl("order/status")),
                eq("get"),
                argThat(data -> {
                    checkQueryOrderData(data);
                    return true;
                }));
    }

    private static void checkClearPrinterOrderData(Map<String, Object> data) {
        assertThat(data.get("sn").toString(), equalTo("this_is_sn"));
    }

    @Test
    public void when_clear_order_then_use_clear_api_and_params() {
        SpyunApiHttpClient testApiClient = createTestApiClient();
        Map<String, Object> mapData = new HashMap<String, Object>() {{
            put("sn", "this_is_sn");
        }};

        testApiClient.clearPrinterOrders(mapData);

        verify(testApiClient).send(
                eq(getFullUrl("cleansqs")),
                eq("delete"),
                argThat(data -> {
                    checkClearPrinterOrderData(data);
                    return true;
                }));
    }

    private static void checkQueryPrinterOrderStatsData(Map<String, Object> data) {
        assertThat(data.get("sn").toString(), equalTo("this_is_sn"));
        assertThat(data.get("date").toString(), equalTo("2016-09-20"));
    }

    @Test
    public void when_query_printer_order_stats_then_use_query_printer_order_stats_api_and_params() {
        SpyunApiHttpClient testApiClient = createTestApiClient();
        Map<String, Object> mapData = new HashMap<String, Object>() {{
            put("sn", "this_is_sn");
            put("date", "2016-09-20");
        }};

        testApiClient.queryPrinterOrderStats(mapData);

        verify(testApiClient).send(
                eq(getFullUrl("order/number")),
                eq("get"),
                argThat(data -> {
                    checkQueryPrinterOrderStatsData(data);
                    return true;
                }));
    }
}
