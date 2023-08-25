package com.github.xesam.printing.api.core;

import java.util.Map;

public interface ApiHttpClient {

    /**
     * 新增设备
     */
    ApiHttpResponse addPrinter(Map<String, Object> data);

    /**
     * 删除设备
     */
    ApiHttpResponse deletePrinter(Map<String, Object> data);

    /**
     * 更新设备信息，设备不支持的属性会直接忽略
     */
    ApiHttpResponse updatePrinter(Map<String, Object> data);

    /**
     * 更新设备配置，设备不支持的配置会直接忽略
     */
    ApiHttpResponse updatePrinterSetting(Map<String, Object> data);

    /**
     * 查询设备信息
     */
    ApiHttpResponse queryPrinter(Map<String, Object> data);

    /**
     * 打印小票订单，如果设备不支持，会直接返回失败
     */
    ApiHttpResponse printMsgOrder(Map<String, Object> data);

    /**
     * 打印标签订单，如果设备不支持，会直接返回失败
     */
    ApiHttpResponse printLabelOrder(Map<String, Object> data);

    /**
     * 查询指定订单的打印情况
     */
    ApiHttpResponse queryOrder(Map<String, Object> data);

    /**
     * 清空指定设备的打印队列
     */
    ApiHttpResponse clearPrinterOrders(Map<String, Object> data);

    /**
     * 查询打印机的订单信息
     */
    ApiHttpResponse queryPrinterOrderStats(Map<String, Object> data);
}
