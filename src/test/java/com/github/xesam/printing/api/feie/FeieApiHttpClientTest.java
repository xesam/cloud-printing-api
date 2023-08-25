package com.github.xesam.printing.api.feie;

import com.github.xesam.printing.api.core.*;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

public class FeieApiHttpClientTest {
    private static final ApiAuth TEST_API_AUTH = new ApiAuth("test_id", "test_secret");
    private static final ApiClock TEST_API_CLOCK = () -> 1000000000;

    private static ApiHttpEngine createTestHttpClient() {
        ApiHttpEngine client = mock(DefaultApiHttpEngine.class);
        when(client.doPost(anyString(), anyMap(), anyMap())).thenReturn(new ApiHttpResponse(""));
        return client;
    }

    private static FeieApiHttpClient createTestApiClient() {
        return createTestApiClient(mock(DefaultApiHttpEngine.class));
    }

    private static FeieApiHttpClient createTestApiClient(ApiHttpEngine httpClient) {
        FeieApiHttpClient client = new FeieApiHttpClient(TEST_API_AUTH, TEST_API_CLOCK, httpClient);
        return spy(client);
    }

    private static Map<String, Object> createRequestDataWithoutSigAndTime() {
        return new HashMap<String, Object>() {{
            put("user", "this_is_user");
            put("debug", "this_is_debug");
            put("apiname", "this_is_apiname");
            put("printerContent", "this_is_printerContent");
        }};
    }

    private static Map<String, Object> createRequestDataWithoutSig() {
        Map<String, Object> data = createRequestDataWithoutSigAndTime();
        data.put("stime", "1000000000");
        return data;
    }

    private static Map<String, Object> createRequestData() {
        Map<String, Object> data = createRequestDataWithoutSig();
        data.put("sig", "this_is_sig");
        return data;
    }

    @Test
    public void when_send_request_then_auto_add_header() {
        ApiHttpEngine testSimpleHttpClient = createTestHttpClient();
        FeieApiHttpClient testApiClient = createTestApiClient(testSimpleHttpClient);

        testApiClient.send("", createRequestData());

        verify(testSimpleHttpClient).doPost(eq(""), isA(Map.class), argThat(headers -> {
            assertThat(headers.get("Content-Type"), equalToIgnoringCase("application/x-www-form-urlencoded; charset=UTF-8"));
            return true;
        }));
    }

    private void checkSig(Map<String, Object> data) {
        assertThat(data.get("sig").toString(), equalTo("c92c63ca5be6d9d31c71a8cc7e6140d59f79a9af"));
    }

    @Test
    public void when_send_without_signature_then_auto_add_signature_with_time_in_request() {
        ApiHttpEngine testSimpleHttpClient = createTestHttpClient();
        FeieApiHttpClient testApiClient = createTestApiClient(testSimpleHttpClient);

        testApiClient.send("", createRequestDataWithoutSig());

        verify(testSimpleHttpClient).doPost(
                eq(""),
                argThat((Map<String, Object> data) -> {
                    checkSig(data);
                    return true;
                }),
                isA(Map.class));
    }

    @Test
    public void when_send_without_signature_and_time_then_auto_add_signature_with_cloud_clock_time() {
        ApiHttpEngine testSimpleHttpClient = createTestHttpClient();
        FeieApiHttpClient testApiClient = createTestApiClient(testSimpleHttpClient);

        testApiClient.send("", createRequestDataWithoutSigAndTime());

        verify(testSimpleHttpClient).doPost(
                eq(""),
                argThat((Map<String, Object> data) -> {
                    checkSig(data);
                    return true;
                }),
                isA(Map.class));
    }

    private static void checkAddPrinterData(Map<String, Object> data) {
        assertThat(data.get("apiname").toString(), equalTo("Open_printerAddlist"));
        assertThat(data.get("printerContent").toString(), equalTo("this_is_printerContent"));
    }

    @Test
    public void when_add_printer_then_use_add_api_and_params() {
        FeieApiHttpClient testApiClient = createTestApiClient();
        Map<String, Object> addPrintersData = new HashMap<String, Object>() {{
            put("printerContent", "this_is_printerContent");
        }};

        testApiClient.addPrinter(addPrintersData);

        verify(testApiClient).send(eq(FeieApiHttpClient.BASE_URL), argThat(data -> {
            checkAddPrinterData(data);
            return true;
        }));
    }

    private static void checkDeletePrinterData(Map<String, Object> data) {
        assertThat(data.get("apiname").toString(), equalTo("Open_printerDelList"));
        assertThat(data.get("snlist").toString(), equalTo("this_is_printerDelList"));
    }

    @Test
    public void when_delete_printer_then_use_delete_api_and_params() {
        FeieApiHttpClient testApiClient = createTestApiClient();
        Map<String, Object> mapData = new HashMap<String, Object>() {{
            put("snlist", "this_is_printerDelList");
        }};

        testApiClient.deletePrinter(mapData);

        verify(testApiClient).send(eq(FeieApiHttpClient.BASE_URL), argThat(data -> {
            checkDeletePrinterData(data);
            return true;
        }));
    }

    private static void checkUpdatePrinterData(Map<String, Object> data) {
        assertThat(data.get("apiname").toString(), equalTo("Open_printerEdit"));
        assertThat(data.get("sn").toString(), equalTo("this_is_sn"));
        assertThat(data.get("name").toString(), equalTo("this_is_name"));
        assertThat(data.get("phonenum").toString(), equalTo("this_is_phonenum"));
    }

    @Test
    public void when_update_printer_then_use_update_api_and_params() {
        FeieApiHttpClient testApiClient = createTestApiClient();
        Map<String, Object> mapData = new HashMap<String, Object>() {{
            put("sn", "this_is_sn");
            put("name", "this_is_name");
            put("phonenum", "this_is_phonenum");
        }};

        testApiClient.updatePrinter(mapData);

        verify(testApiClient).send(eq(FeieApiHttpClient.BASE_URL), argThat(data -> {
            checkUpdatePrinterData(data);
            return true;
        }));
    }

    @Test
    public void when_update_printer_setting_then_throw_error() {
        FeieApiHttpClient testApiClient = createTestApiClient();
        Map<String, Object> mapData = new HashMap<String, Object>() {{
        }};

        ApiHttpResponse response = testApiClient.updatePrinterSetting(mapData);
        assertThat(response.isSuccess(), equalTo(false));
        assertThat(response.getException(), Matchers.isA(ApiUnsupportedException.class));
    }

    private static void checkQueryPrinterData(Map<String, Object> data) {
        assertThat(data.get("apiname").toString(), equalTo("Open_queryPrinterStatus"));
        assertThat(data.get("sn").toString(), equalTo("this_is_sn"));
    }

    @Test
    public void when_query_printer_then_use_query_api_and_params() {
        FeieApiHttpClient testApiClient = createTestApiClient();
        Map<String, Object> mapData = new HashMap<String, Object>() {{
            put("sn", "this_is_sn");
        }};

        testApiClient.queryPrinter(mapData);

        verify(testApiClient).send(eq(FeieApiHttpClient.BASE_URL), argThat(data -> {
            checkQueryPrinterData(data);
            return true;
        }));
    }

    private static void checkPrintMsgOrderData(Map<String, Object> data) {
        assertThat(data.get("apiname").toString(), equalTo("Open_printMsg"));
        assertThat(data.get("sn").toString(), equalTo("this_is_sn"));
        assertThat(data.get("content").toString(), equalTo("this_is_content"));
        assertThat(data.get("backurl").toString(), equalTo("this_is_backurl"));
        assertThat(data.get("expired"), equalTo(86400));
        assertThat(data.get("times"), equalTo(1));
    }

    @Test
    public void when_print_msg_order_then_use_print_msg_api_and_params() {
        FeieApiHttpClient testApiClient = createTestApiClient();
        Map<String, Object> mapData = new HashMap<String, Object>() {{
            put("backurl", "this_is_backurl");
            put("expired", 86400);
            put("sn", "this_is_sn");
            put("content", "this_is_content");
            put("times", 1);
        }};

        testApiClient.printMsgOrder(mapData);

        verify(testApiClient).send(eq(FeieApiHttpClient.BASE_URL), argThat(data -> {
            checkPrintMsgOrderData(data);
            return true;
        }));
    }

    private static void checkPrintLabelOrderData(Map<String, Object> data) {
        assertThat(data.get("apiname").toString(), equalTo("Open_printLabelMsg"));
        assertThat(data.get("sn").toString(), equalTo("this_is_sn"));
        assertThat(data.get("content").toString(), equalTo("this_is_content"));
        assertThat(data.get("backurl").toString(), equalTo("this_is_backurl"));
        assertThat(data.get("expired"), equalTo(86400));
        assertThat(data.get("times"), equalTo(1));
    }

    @Test
    public void when_print_label_order_then_use_print_label_api_and_params() {
        FeieApiHttpClient testApiClient = createTestApiClient();
        Map<String, Object> mapData = new HashMap<String, Object>() {{
            put("backurl", "this_is_backurl");
            put("expired", 86400);
            put("sn", "this_is_sn");
            put("content", "this_is_content");
            put("times", 1);
        }};

        testApiClient.printLabelOrder(mapData);

        verify(testApiClient).send(eq(FeieApiHttpClient.BASE_URL), argThat(data -> {
            checkPrintLabelOrderData(data);
            return true;
        }));
    }

    private static void checkQueryOrderData(Map<String, Object> data) {
        assertThat(data.get("apiname").toString(), equalTo("Open_queryOrderState"));
        assertThat(data.get("orderid").toString(), equalTo("this_is_orderid"));
    }

    @Test
    public void when_query_order_then_use_query_api_and_params() {
        FeieApiHttpClient testApiClient = createTestApiClient();
        Map<String, Object> mapData = new HashMap<String, Object>() {{
            put("orderid", "this_is_orderid");
        }};

        testApiClient.queryOrder(mapData);

        verify(testApiClient).send(eq(FeieApiHttpClient.BASE_URL), argThat(data -> {
            checkQueryOrderData(data);
            return true;
        }));
    }

    private static void checkClearPrinterOrderData(Map<String, Object> data) {
        assertThat(data.get("apiname").toString(), equalTo("Open_delPrinterSqs"));
        assertThat(data.get("sn").toString(), equalTo("this_is_sn"));
    }

    @Test
    public void when_clear_order_then_use_clear_api_and_params() {
        FeieApiHttpClient testApiClient = createTestApiClient();
        Map<String, Object> mapData = new HashMap<String, Object>() {{
            put("sn", "this_is_sn");
        }};

        testApiClient.clearPrinterOrders(mapData);

        verify(testApiClient).send(eq(FeieApiHttpClient.BASE_URL), argThat(data -> {
            checkClearPrinterOrderData(data);
            return true;
        }));
    }

    private static void checkQueryPrinterOrderStatsData(Map<String, Object> data) {
        assertThat(data.get("apiname").toString(), equalTo("Open_queryOrderInfoByDate"));
        assertThat(data.get("sn").toString(), equalTo("this_is_sn"));
        assertThat(data.get("date").toString(), equalTo("2016-09-20"));
    }

    @Test
    public void when_query_printer_order_stats_then_use_query_printer_order_stats_api_and_params() {
        FeieApiHttpClient testApiClient = createTestApiClient();
        Map<String, Object> mapData = new HashMap<String, Object>() {{
            put("sn", "this_is_sn");
            put("date", "2016-09-20");
        }};

        testApiClient.queryPrinterOrderStats(mapData);

        verify(testApiClient).send(eq(FeieApiHttpClient.BASE_URL), argThat(data -> {
            checkQueryPrinterOrderStatsData(data);
            return true;
        }));
    }
}
