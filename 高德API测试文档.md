# 高德地图API测试文档 — Web端

> **文档版本**：v1.2
>
> **更新日期**：2026-06-10
>
> **API服务商**：高德地图开放平台
>
> **应用名称**：gaode
>
> **创建时间**：2026/6/10
>
> **服务类型**：Web端
>
> **测试状态**：✅ 已成功测试通过

> ⚠️ **安全提示**：文档中的API Key仅用于测试环境。生产环境请使用环境变量管理密钥，禁止硬编码在代码中。

---

## 环境变量配置建议

| 变量名 | 说明 | 示例值 |
| --- | --- | --- |
| `AMAP_API_KEY` | 高德地图API Key | `your_api_key_here` |
| `AMAP_API_HOST` | 高德地图API Host | `https://restapi.amap.com` |

**Spring Boot配置示例：**

```yaml
map:
  amap:
    api-key: ${AMAP_API_KEY}
    api-host: ${AMAP_API_HOST}
```

---

## 一、基础信息

### 1.1 API凭证

| 项目 | 值 |
| --- | --- |
| **API Key** | `acb49eaba3bed860392346e6f34b0bfb` |
| **服务类型** | Web端 |
| **协议** | HTTPS |

### 1.2 API Host

| 环境 | Host |
| --- | --- |
| 正式环境 | `https://restapi.amap.com` |

### 1.3 认证方式

**参数方式（GET请求）：**

```
?key=acb49eaba3bed860392346e6f34b0bfb
```


### 1.4 通用返回状态码

| 状态码 | 含义 |
| --- | --- |
| 0 | 请求成功 |
| 1 | 服务器内部错误 |
| 10001 | 无效的用户key |
| 10002 | 权限不足 |
| 10003 | 配额超限 |
| 10004 | 服务不支持 |
| 10005 | 参数错误 |
| 10006 | 解析错误 |
| 10007 | 网络错误 |
| 10008 | 超时 |
| 10009 | 未知错误 |

---

## 二、测试API列表

### 2.1 地理编码（地址转坐标）

**接口说明**

将中文地址转换为经纬度坐标。

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | `/v3/geocode/geo` |
| 数据格式 | JSON |

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| key | string | 是 | API Key |
| address | string | 是 | 中文地址（如：北京市朝阳区望京SOHO） |
| city | string | 否 | 城市名（用于限定搜索范围） |

**请求示例**

```
GET https://restapi.amap.com/v3/geocode/geo?address=北京市朝阳区望京SOHO&city=北京&key=acb49eaba3bed860392346e6f34b0bfb
```

**响应示例（实际测试结果）**

```json
{
  "status": "1",
  "info": "OK",
  "infocode": "10000",
  "count": "1",
  "geocodes": [
    {
      "formatted_address": "北京市朝阳区望京",
      "country": "中国",
      "province": "北京市",
      "citycode": "010",
      "city": "北京市",
      "district": "朝阳区",
      "township": [],
      "neighborhood": {
        "name": [],
        "type": []
      },
      "building": {
        "name": [],
        "type": []
      },
      "adcode": "110105",
      "street": [],
      "number": [],
      "location": "116.470293,39.996171",
      "level": "住宅区"
    }
  ]
}
```

**响应字段说明**

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| status | string | 1=成功，0=失败 |
| info | string | 返回信息 |
| geocodes[0].formatted_address | string | 格式化地址 |
| geocodes[0].province | string | 省份 |
| geocodes[0].city | string | 城市 |
| geocodes[0].district | string | 区县 |
| geocodes[0].adcode | string | 行政区划代码 |
| geocodes[0].location | string | 经纬度（格式：经度,纬度） |
