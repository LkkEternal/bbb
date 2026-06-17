# 华为云IoTDA工具类使用说明

> 版本：v1.0  
> 更新日期：2026-06-17

---

## 1. 工具类位置

```
navel-orange-common/src/main/java/com/citrus/common/iotda/HuaweiIotdaClient.java
```

---

## 2. 配置说明

### 2.1 application-dev.yml 配置

```yaml
huawei:
  iotda:
    endpoint: bf593413cf.st1.iotda-app.cn-south-1.myhuaweicloud.com  # IoTDA接入地址
    project-id: 019e966a75dd7184ab7e7e69f2ad38a5                     # 项目ID
    access-key: ${IOTDA_ACCESS_KEY}                                   # AK（使用环境变量）
    secret-key: ${IOTDA_SECRET_KEY}                                   # SK（使用环境变量）
    device-id: 6a2e6c41c9429d337f50a67c_Equipment01                   # 默认设备ID
    device-secret: ce7924b9318baeec1225669dafcb4850                    # 设备密钥
```

### 2.2 获取配置信息

| 配置项 | 获取方式 |
|--------|----------|
| endpoint | IoTDA控制台 → 接入信息 → 应用侧HTTPS接入地址 |
| project-id | 华为云控制台 → 我的凭证 → 项目列表 → 对应区域的项目ID |
| access-key / secret-key | 华为云控制台 → 我的凭证 → 管理访问密钥 |

---

## 3. API方法说明

### 3.1 设备管理

```java
@Autowired
private HuaweiIotdaClient iotdaClient;

// 查询设备详情
HuaweiIotdaClient.DeviceDetail device = iotdaClient.getDevice("device_id");
// 返回: deviceId, productName, status, description等

// 查询设备列表
HuaweiIotdaClient.DeviceListResponse devices = iotdaClient.listDevices("product_id", 10, null);
// 参数: productId(可选), limit, marker(分页)

// 获取设备影子数据
HuaweiIotdaClient.DeviceShadowResponse shadow = iotdaClient.getDeviceShadow("device_id");
// 返回: 设备上报的属性数据

// 获取设备属性（简化方法）
Map<String, Object> properties = iotdaClient.getDeviceProperties("device_id", "service_id");
// 返回: {moisture=60, soil_temp=25.5, humidity=67.3}
```

### 3.2 设备消息

```java
// 下发设备消息
Map<String, Object> message = new HashMap<>();
message.put("content", "hello");
String messageId = iotdaClient.sendMessage("device_id", message);

// 查询设备消息列表
HuaweiIotdaClient.MessageListResponse messages = iotdaClient.listMessages("device_id", 10);
```

### 3.3 设备命令

```java
// 下发设备命令
Map<String, Object> paras = new HashMap<>();
paras.put("temperature", 25);
HuaweiIotdaClient.CommandResponse cmd = iotdaClient.sendCommand(
    "device_id", "service_id", "setTemperature", paras
);
```

### 3.4 产品管理

```java
// 查询产品列表
HuaweiIotdaClient.ProductListResponse products = iotdaClient.listProducts(10);

// 查询产品详情
HuaweiIotdaClient.ProductDetail product = iotdaClient.getProduct("product_id");
```

---

## 4. 完整示例

### 4.1 获取传感器数据

```java
@RestController
@RequestMapping("/api/sensor")
public class SensorController {

    @Autowired
    private HuaweiIotdaClient iotdaClient;

    private static final String DEVICE_ID = "6a2e6c41c9429d337f50a67c_Equipment01";
    private static final String SERVICE_ID = "SensorDate";

    @GetMapping("/data")
    public Result getSensorData() {
        Map<String, Object> properties = iotdaClient.getDeviceProperties(DEVICE_ID, SERVICE_ID);
        return Result.success(properties);
    }
}
```

**返回示例：**
```json
{
  "code": 200,
  "data": {
    "moisture": 60,
    "soil_temp": 25.5,
    "conductivity": 0,
    "ph": 7.6,
    "air_temp": 23.6,
    "humidity": 67.3
  }
}
```

### 4.2 获取设备状态

```java
@GetMapping("/status")
public Result getDeviceStatus() {
    DeviceDetail device = iotdaClient.getDevice(DEVICE_ID);
    
    Map<String, Object> result = new HashMap<>();
    result.put("deviceId", device.getDeviceId());
    result.put("status", device.getStatus());  // ONLINE / OFFLINE / INACTIVE
    result.put("productName", device.getProductName());
    
    return Result.success(result);
}
```

### 4.3 下发控制命令

```java
@PostMapping("/command")
public Result sendCommand(@RequestBody Map<String, Object> request) {
    String commandName = (String) request.get("command");
    Map<String, Object> paras = (Map<String, Object>) request.get("paras");
    
    CommandResponse response = iotdaClient.sendCommand(DEVICE_ID, SERVICE_ID, commandName, paras);
    return Result.success(response);
}
```

---

## 5. 设备数据字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| `air_temp` | number | 空气温度（℃） |
| `humidity` | number | 空气湿度（%） |
| `soil_temp` | number | 土壤温度（℃） |
| `moisture` | number | 土壤湿度（%） |
| `ph` | number | 酸碱度 |
| `conductivity` | number | 电导率 |

---

## 6. 认证机制

工具类使用 **IAM Token认证**：

1. 使用AK/SK调用IAM服务获取Token
2. Token有效期24小时，自动缓存和刷新
3. 后续请求携带 `X-Auth-Token` 请求头

---

## 7. 注意事项

1. **敏感信息**：AK/SK必须使用环境变量，禁止硬编码
2. **超时配置**：默认连接超时30秒，可根据网络情况调整
3. **错误处理**：工具类会抛出 `BizException`，需统一异常处理
4. **设备状态**：设备离线时无法获取实时数据，但可获取最后一次上报的数据（影子数据）

---

## 8. 测试类

```bash
# 运行测试
mvn test -Dtest=HuaweiIotdaClientTest -pl navel-orange-common
```

测试类位置：`navel-orange-common/src/test/java/com/citrus/common/iotda/HuaweiIotdaClientTest.java`
