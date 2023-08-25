package com.github.xesam.printing.api.xpyun;

import com.github.xesam.printing.api.core.*;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class XpyunApiHttpClientTest {

    private static final ApiAuth TEST_API_AUTH = new ApiAuth("test_id", "test_secret");
    private static final ApiClock TEST_API_CLOCK = () -> 1000000000;

    private static ApiHttpEngine createTestHttpClient() {
        ApiHttpEngine client = mock(DefaultApiHttpEngine.class);
        when(client.doPost(anyString(), anyMap(), anyMap())).thenReturn(new ApiHttpResponse(""));
        return client;
    }

    private static XpyunApiHttpClient createTestApiClient(ApiHttpEngine httpClient) {
        XpyunApiHttpClient client = new XpyunApiHttpClient(TEST_API_AUTH, TEST_API_CLOCK, httpClient);
        return spy(client);
    }

    private static XpyunApiHttpClient createTestApiClient() {
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
        assertThat(headers.get("Content-Type"), equalToIgnoringCase("application/json;charset=UTF-8"));
    }

    @Test
    public void when_send_request_then_auto_add_header() {
        ApiHttpEngine testHttpClient = createTestHttpClient();
        XpyunApiHttpClient testApiClient = createTestApiClient(testHttpClient);

        testApiClient.send("", createRequestData());

        verify(testHttpClient).doPost(eq(""), isA(Map.class), argThat(headers -> {
            checkHeader(headers);
            return true;
        }));
    }

    private void checkSig(Map<String, Object> data) {
        assertThat(data.get("sign").toString(), equalTo("c92c63ca5be6d9d31c71a8cc7e6140d59f79a9af"));
    }

    @Test
    public void when_send_without_signature_then_auto_add_signature_with_time_in_request() {
        ApiHttpEngine testHttpClient = createTestHttpClient();
        XpyunApiHttpClient testApiClient = createTestApiClient(testHttpClient);

        testApiClient.send("", createRequestDataWithoutSig());

        verify(testHttpClient).doPost(eq(""), argThat((Map<String, Object> data) -> {
            checkSig(data);
            return true;
        }), isA(Map.class));
    }

    @Test
    public void when_send_without_signature_and_time_then_auto_add_signature_with_cloud_clock_time() {
        ApiHttpEngine testHttpClient = createTestHttpClient();
        XpyunApiHttpClient testApiClient = createTestApiClient(testHttpClient);

        testApiClient.send("", createRequestDataWithoutSigAndTime());

        verify(testHttpClient).doPost(eq(""), argThat((Map<String, Object> data) -> {
            checkSig(data);
            return true;
        }), isA(Map.class));
    }

    private static String getFullUrl(String path) {
        return XpyunApiHttpClient.BASE_URL + path;
    }

    private static void checkAddPrinterData(Map<String, Object> data) {
        assertThat(data.get("items").toString(), equalTo("this_id_sn"));
    }

    @Test
    public void when_add_printer_then_use_add_api_and_params() {
        ApiHttpEngine testHttpClient = createTestHttpClient();
        XpyunApiHttpClient testApiClient = createTestApiClient(testHttpClient);
        Map<String, Object> addPrintersData = new HashMap<String, Object>() {{
            put("items", "this_id_sn");
        }};

        testApiClient.addPrinter(addPrintersData);

        verify(testApiClient).send(
                eq(getFullUrl("addPrinters")),
                argThat((Map<String, Object> data) -> {
                    checkAddPrinterData(data);
                    return true;
                })
        );
    }

    private static void checkDeletePrinterData(Map<String, Object> data) {
        assertThat(data.get("snlist").toString(), equalTo("this_is_printerDelList"));
    }

    @Test
    public void when_delete_printer_then_use_delete_api_and_params() {
        XpyunApiHttpClient testApiClient = createTestApiClient();
        Map<String, Object> mapData = new HashMap<String, Object>() {{
            put("snlist", "this_is_printerDelList");
        }};

        testApiClient.deletePrinter(mapData);

        verify(testApiClient).send(eq(getFullUrl("delPrinters")), argThat(data -> {
            checkDeletePrinterData(data);
            return true;
        }));
    }

    private static void checkUpdatePrinterData(Map<String, Object> data) {
        assertThat(data.get("sn").toString(), equalTo("this_is_sn"));
        assertThat(data.get("name").toString(), equalTo("this_is_name"));
        assertThat(data.get("cardno").toString(), equalTo("this_is_phonenum"));
    }

    @Test
    public void when_update_printer_then_use_update_api_and_params() {
        XpyunApiHttpClient testApiClient = createTestApiClient();
        Map<String, Object> mapData = new HashMap<String, Object>() {{
            put("sn", "this_is_sn");
            put("name", "this_is_name");
            put("cardno", "this_is_phonenum");
        }};

        testApiClient.updatePrinter(mapData);

        verify(testApiClient).send(eq(getFullUrl("updPrinter")), argThat(data -> {
            checkUpdatePrinterData(data);
            return true;
        }));
    }

    private static void checkUpdatePrinterSettingData(Map<String, Object> data) {
        assertThat(data.get("sn").toString(), equalTo("this_is_sn"));
        assertThat(data.get("voiceType"), equalTo(1));
        assertThat(data.get("volumeLevel"), equalTo(2));
    }

    @Test
    public void when_update_printer_setting_then_use_update_setting_api_and_params() {
        ApiHttpEngine testHttpClient = createTestHttpClient();
        XpyunApiHttpClient testApiClient = createTestApiClient(testHttpClient);
        Map<String, Object> mapData = new HashMap<String, Object>() {{
            put("sn", "this_is_sn");
            put("voiceType", 1);
            put("volumeLevel", 2);
        }};

        testApiClient.updatePrinterSetting(mapData);

        verify(testApiClient).send(
                eq(getFullUrl("setVoiceType")),
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
        XpyunApiHttpClient testApiClient = createTestApiClient();
        Map<String, Object> mapData = new HashMap<String, Object>() {{
            put("sn", "this_is_sn");
        }};

        testApiClient.queryPrinter(mapData);

        verify(testApiClient).send(eq(getFullUrl("queryPrinterStatus")), argThat(data -> {
            checkQueryPrinterData(data);
            return true;
        }));
    }

    private static void checkPrintMsgOrderData(Map<String, Object> data) {
        assertThat(data.get("sn").toString(), equalTo("this_is_sn"));
        assertThat(data.get("content").toString(), equalTo("this_is_content"));
        assertThat(data.get("copies"), equalTo(1));
        assertThat(data.get("backurlFlag"), equalTo(1));
        assertThat(data.get("cutter"), equalTo(1));
        assertThat(data.get("voice"), equalTo(1));
        assertThat(data.get("mode"), equalTo(1));
        assertThat(data.get("expiresIn"), equalTo(86400));
        assertThat(data.get("payType"), equalTo(41));
        assertThat(data.get("payMode"), equalTo(59));
        assertThat(data.get("money"), equalTo(22));
    }

    @Test
    public void when_print_msg_order_then_use_print_msg_api_and_params() {
        XpyunApiHttpClient testApiClient = createTestApiClient();
        Map<String, Object> mapData = new HashMap<String, Object>() {{
            put("sn", "this_is_sn");
            put("content", "this_is_content");
            put("copies", 1);
            put("backurlFlag", 1);
            put("cutter", 1);
            put("voice", 1);
            put("mode", 1);
            put("expiresIn", 86400);
            put("payType", 41);
            put("payMode", 59);
            put("money", 22);
        }};

        testApiClient.printMsgOrder(mapData);

        verify(testApiClient).send(eq(getFullUrl("print")), argThat(data -> {
            checkPrintMsgOrderData(data);
            return true;
        }));
    }

    private static void checkPrintLabelOrderData(Map<String, Object> data) {
        assertThat(data.get("sn").toString(), equalTo("this_is_sn"));
        assertThat(data.get("content").toString(), equalTo("this_is_content"));
        assertThat(data.get("copies"), equalTo(1));
        assertThat(data.get("backurlFlag"), equalTo(1));
        assertThat(data.get("cutter"), equalTo(1));
        assertThat(data.get("voice"), equalTo(1));
        assertThat(data.get("mode"), equalTo(1));
        assertThat(data.get("expiresIn"), equalTo(86400));
        assertThat(data.get("payType"), equalTo(41));
        assertThat(data.get("payMode"), equalTo(59));
        assertThat(data.get("money"), equalTo(22));
    }

    @Test
    public void when_print_label_order_then_use_print_label_api_and_params() {
        XpyunApiHttpClient testApiClient = createTestApiClient();
        Map<String, Object> mapData = new HashMap<String, Object>() {{
            put("sn", "this_is_sn");
            put("content", "this_is_content");
            put("copies", 1);
            put("backurlFlag", 1);
            put("cutter", 1);
            put("voice", 1);
            put("mode", 1);
            put("expiresIn", 86400);
            put("payType", 41);
            put("payMode", 59);
            put("money", 22);
        }};

        testApiClient.printLabelOrder(mapData);

        verify(testApiClient).send(eq(getFullUrl("printLabel")), argThat(data -> {
            checkPrintLabelOrderData(data);
            return true;
        }));
    }

    private static void checkQueryOrderData(Map<String, Object> data) {
        assertThat(data.get("orderId").toString(), equalTo("this_is_orderid"));
    }

    @Test
    public void when_query_order_then_use_query_api_and_params() {
        XpyunApiHttpClient testApiClient = createTestApiClient();
        Map<String, Object> mapData = new HashMap<String, Object>() {{
            put("orderId", "this_is_orderid");
        }};

        testApiClient.queryOrder(mapData);

        verify(testApiClient).send(eq(getFullUrl("queryOrderState")), argThat(data -> {
            checkQueryOrderData(data);
            return true;
        }));
    }

    private static void checkClearPrinterOrderData(Map<String, Object> data) {
        assertThat(data.get("sn").toString(), equalTo("this_is_sn"));
    }

    @Test
    public void when_clear_order_then_use_clear_api_and_params() {
        XpyunApiHttpClient testApiClient = createTestApiClient();
        Map<String, Object> mapData = new HashMap<String, Object>() {{
            put("sn", "this_is_sn");
        }};

        testApiClient.clearPrinterOrders(mapData);

        verify(testApiClient).send(eq(getFullUrl("delPrinterQueue")), argThat(data -> {
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
        XpyunApiHttpClient testApiClient = createTestApiClient();
        Map<String, Object> mapData = new HashMap<String, Object>() {{
            put("sn", "this_is_sn");
            put("date", "2016-09-20");
        }};

        testApiClient.queryPrinterOrderStats(mapData);

        verify(testApiClient).send(eq(getFullUrl("queryOrderStatis")), argThat(data -> {
            checkQueryPrinterOrderStatsData(data);
            return true;
        }));
    }


}
