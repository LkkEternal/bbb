# 天气API测试文档 — 和风天气

> **文档版本**：v1.1
> **更新日期**：2026-06-10
> **API服务商**：和风天气开发控制台
> **项目名称**：test-weather
> **凭证ID**：C8PRU8MUKE
> **API KEY**：77b19b0da0c24164a60e7f437d7b7cc5 
> **测试状态**：✅ 已成功测试通过

---

## 一、基础信息

### 1.1 API Host

| 环境 | Host |
|------|------|
| 测试环境 | `https://nk6k5qtwn8.re.qweatherapi.com/` |

### 1.2 认证方式

- 请求头中携带 API KEY（推荐方式）
- 或在请求参数中传递 `key` 参数

**请求头方式：**
```
X-QW-Api-Key: 77b19b0da0c24164a60e7f437d7b7cc5
```

**参数方式：**
```
?key=77b19b0da0c24164a60e7f437d7b7cc5
```

### 1.3 通用返回状态码

| 状态码 | 含义 |
|--------|------|
| 200 | 请求成功 |
| 400 | 请求参数错误 |
| 401 | 认证失败（API KEY错误或未提供） |
| 402 | 超过访问次数或余额不足 |
| 403 | 无访问权限 |
| 404 | 查询的数据或地区不存在 |
| 500 | 服务器内部错误 |

---

## 二、测试API列表

### 2.1 获取实时天气

**接口说明**

获取指定城市的实时天气数据，包括温度、湿度、天气状况、风向、风速等。

**请求**

| 项目 | 说明 |
|------|------|
| 请求方法 | GET |
| 请求路径 | `/v7/weather/now` |
| 数据格式 | JSON |

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| location | string | 是 | 城市ID/经纬度（如：101010100 北京，114.05,22.55 深圳） |
| lang | string | 否 | 语言，默认 `zh` |
| unit | string | 否 | 单位，`m` 公制（默认）/ `i` 英制 |

**请求示例**

```
GET https://nk6k5qtwn8.re.qweatherapi.com/v7/weather/now?location=101010100&key=77b19b0da0c24164a60e7f437d7b7cc5
```

**请求头示例**

```
GET /v7/weather/now?location=101010100 HTTP/1.1
Host: nk6k5qtwn8.re.qweatherapi.com
X-QW-Api-Key: 77b19b0da0c24164a60e7f437d7b7cc5
```

**响应示例（实际测试结果）**

```json
{
    "code": "200",
    "updateTime": "2026-06-10T10:19+08:00",
    "fxLink": "https://www.qweather.com/weather/beijing-101010100.html",
    "now": {
        "obsTime": "2026-06-10T10:08+08:00",
        "temp": "23",
        "feelsLike": "23",
        "icon": "104",
        "text": "阴",
        "wind360": "90",
        "windDir": "东风",
        "windScale": "1",
        "windSpeed": "5",
        "humidity": "55",
        "precip": "0.0",
        "pressure": "1009",
        "vis": "28",
        "cloud": "10",
        "dew": "14"
    },
    "refer": {
        "sources": [
            "QWeather"
        ],
        "license": [
            "QWeather Developers License"
        ]
    }
}
```

**响应字段说明**

| 字段名 | 类型 | 说明 |
|--------|------|------|
| code | string | 状态码 |
| updateTime | string | API最近更新时间 |
| now.obsTime | string | 数据观测时间 |
| now.temp | string | 温度（摄氏度） |
| now.feelsLike | string | 体感温度 |
| now.icon | string | 天气状况图标代码 |
| now.text | string | 天气状况文字描述 |
| now.wind360 | string | 风向360角度 |
| now.windDir | string | 风向 |
| now.windScale | string | 风力等级 |
| now.windSpeed | string | 风速（km/h） |
| now.humidity | string | 相对湿度（%） |
| now.precip | string | 降水量（mm） |
| now.pressure | string | 大气压强（hPa） |
| now.vis | string | 能见度（km） |

