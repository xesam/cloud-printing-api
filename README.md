# Cloud Printing Api

外卖云打印机开放平台接口的 Java 封装。

如果需要支持统一形式的调用，可以考虑采用聚合接口：[https://github.com/xesam/cloud-printing](https://github.com/xesam/cloud-printing)

## 当前支持的打印产商

1. 芯烨；
2. 飞鹅；
3. 商鹏。

*注意：这些厂商各自都有常规打印机与云打印机系列，只有**云打印机**型号才被支持*


## 使用

### 安装

#### 源码方式

1. 下载源码；

#### jar


#### Maven


### 添加依赖

默认实现依赖 httpclient5（发送Http请求） 和 jackson（序列化Map为JSON）。

以 maven 为例：

```xml
    <dependencies>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.14.0</version>
        </dependency>
        <dependency>
            <groupId>org.apache.httpcomponents.client5</groupId>
            <artifactId>httpclient5</artifactId>
            <version>5.2.1</version>
        </dependency>
    </dependencies>
```

### 创建 ApiAuth

```java
    ApiAuth apiAuth = new ApiAuth("this is your appid", "this is your secret");
```

这一步不是必须的，你也可以直接把签名参数放到接口的对应字段去，也就是略微麻烦一点而已。

### 创建 ApiHttpClient

使用 ApiAuth 的方式：
```java
    ApiHttpClient apiClient = new FeieApiHttpClient(apiAuth); // 飞鹅
    ApiHttpClient apiClient = new XpyunApiHttpClient(apiAuth); // 芯烨
    ApiHttpClient apiClient = new SpyunApiHttpClient(apiAuth); // 商鹏
```

不使用 ApiAuth 的方式：
```java
    ApiHttpClient apiClient = new FeieApiHttpClient(); // 飞鹅
    ApiHttpClient apiClient = new XpyunApiHttpClient(); // 芯烨
    ApiHttpClient apiClient = new SpyunApiHttpClient(); // 商鹏
```

### 调用对应的接口

以打印接口为例，注意不同平台使用的参数不同。

飞鹅，使用了 apiAuth：

```java

    Map<String, Object> data = new HashMap<String, Object>() {{
        put("backurl", "this_is_backurl");
        put("expired", 86400);
        put("sn", "this_is_sn");
        put("content", "this_is_content");
        put("times", 1);
    }};

    ApiResponse res = testApiClient.printMsgOrder(data);
    if(res.isSuccess()){
        System.out.println(res.getValue());
    }else{
        res.getException().printStackTrace();    
    }

```

**提示：如果 testApiClient 在创建的时候没有使用 apiAuth，那么就需要在 data 中显式传入签名信息**

飞鹅，没有使用 apiAuth：

```java

    Map<String, Object> data = new HashMap<String, Object>() {{
        put("backurl", "this_is_backurl");
        put("expired", 86400);
        put("sn", "this_is_sn");
        put("content", "this_is_content");
        put("times", 1);
        put("stime", "this is your stime"); //显式传入 stime
        put("user", "this is your user"); //显式传入 user
        put("sig", "this is your sig");   //显式传入 sig
    }};

    ApiResponse res = testApiClient.printMsgOrder(data);
    if(res.isSuccess()){
        System.out.println(res.getValue());
    }else{
        res.getException().printStackTrace();    
    }

```


芯烨：

```java

    Map<String, Object> data = new HashMap<String, Object>() {{
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

    ApiResponse res = testApiClient.printMsgOrder(data);
    if(res.isSuccess()){
        System.out.println(res.getValue());
    }else{
        res.getException().printStackTrace();    
    }

```

商鹏：

```java

    Map<String, Object> mapData = new HashMap<String, Object>() {{
        put("sn", "this_is_sn");
        put("content", "this_is_content");
        put("times", 1);
    }};

    ApiResponse res = testApiClient.printMsgOrder(data);
    if(res.isSuccess()){
        System.out.println(res.getValue());
    }else{
        res.getException().printStackTrace();    
    }

```

具体接口参见：[ApiHttpClient.java](src/main/java/io/github/xesam/cloud/api/core/ApiHttpClient.java)

## 厂商接口差异

服务端打印回调 差异：

| 平台差异   | 飞鹅 | 商鹏云 | 芯烨云 |
| -------- | ---- | ------ | ------ |
| 服务器回调 | 支持 | 不支持 | 支持 |

常规接口 差异：

| 接口   | 飞鹅 | 商鹏云 | 芯烨云 |
| -------- | ---- | ------ | ------ |
| 打印小票 | 支持 | 支持 | 支持 |
| 打印标签 | 支持 | 不支持 | 支持 |
| 打印超时 | 支持接口配置，最长24小时 | 固定48小时 | 支持接口配置，最长24小时 |
| 添加设备 | 支持   | 支持  |  支持     |
| 删除设备 | 支持   | 支持  |  支持     |
| 修改设备 | 支持   | 支持  |  支持     |
| 查询订单 | 支持   | 支持  |  支持     |
| 清空订单 | 支持   | 支持  |  支持     |
| 修改声音 | 不支持 | 支持  |  支持     |
| 配置切刀 | 不支持 | 支持  |  不支持   |


### 自定义实现

如果需要替换 httpclient5 或者 jackson，直接通过构造方法替换 ApiHttpEngine 即可。

```java
    ApiHttpEngine customEngine = new CustomApiHttpEngine();
    ApiHttpClient apiClient = new FeieApiHttpClient(apiAuth, customEngine); // 飞鹅
    ApiHttpClient apiClient = new XpyunApiHttpClient(apiAuth, customEngine); // 芯烨
    ApiHttpClient apiClient = new SpyunApiHttpClient(apiAuth, customEngine); // 商鹏

```

## ChangeLog

### 0.0.1
1. Api 请求封装；
