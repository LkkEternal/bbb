# 天气API测试文档 — 和风天气

> **文档版本**：v1.2
>
> **更新日期**：2026-06-10
>
> **API服务商**：和风天气开发控制台
>
> **项目名称**：test-weather
>
> **凭证ID**：C8PRU8MUKE
>
> **API KEY**：77b19b0da0c24164a60e7f437d7b7cc5 
>
> **测试状态**：✅ 已成功测试通过（实时天气 + 24h预报 + 72h预报）

> ⚠️ **安全提示**：文档中的API Key仅用于测试环境。生产环境请使用环境变量管理密钥，禁止硬编码在代码中。

---

## 环境变量配置建议

| 变量名 | 说明 | 示例值 |
| --- | --- | --- |
| `QWEATHER_API_KEY` | 和风天气API Key | `your_api_key_here` |
| `QWEATHER_API_HOST` | 和风天气API Host | `https://nk6k5qtwn8.re.qweatherapi.com/` |

**Spring Boot配置示例：**

```yaml
weather:
  qweather:
    api-key: ${QWEATHER_API_KEY}
    api-host: ${QWEATHER_API_HOST}
```

---

## 一、基础信息

### 1.1 API Host

| 环境 | Host |
| --- | --- |
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
| --- | --- |
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
| --- | --- |
| 请求方法 | GET |
| 请求路径 | `/v7/weather/now` |
| 数据格式 | JSON |

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
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
| --- | --- | --- |
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
| now.cloud | string | 云量（%） |
| now.dew | string | 露点温度（℃） |

---

### 2.2 获取24小时天气预报

**接口说明**

获取指定城市未来24小时的逐小时天气预报数据，包括温度、湿度、天气状况、风向、风速、降水概率等。

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | `/v7/weather/24h` |
| 数据格式 | JSON |

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| location | string | 是 | 城市ID/经纬度（如：101010100 北京） |
| lang | string | 否 | 语言，默认 `zh` |
| unit | string | 否 | 单位，`m` 公制（默认）/ `i` 英制 |
| key | string | 是 | API KEY |

**请求示例**

```
GET https://nk6k5qtwn8.re.qweatherapi.com/v7/weather/24h?location=101010100&key=77b19b0da0c24164a60e7f437d7b7cc5
```

**响应示例（实际测试结果）**

```json
{
    "code": "200",
    "updateTime": "2026-06-10T18:04+08:00",
    "fxLink": "https://www.qweather.com/weather/beijing-101010100.html",
    "hourly": [
        {
            "fxTime": "2026-06-10T19:00+08:00",
            "temp": "22",
            "icon": "101",
            "text": "多云",
            "wind360": "227",
            "windDir": "西南风",
            "windScale": "1-3",
            "windSpeed": "7",
            "humidity": "66",
            "pop": "0",
            "precip": "0.0",
            "pressure": "1006",
            "cloud": "47",
            "dew": "14"
        }
    ],
    ...//24小时内的数据
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
| --- | --- | --- |
| code | string | 状态码 |
| updateTime | string | API最近更新时间 |
| hourly[].fxTime | string | 预报时间（ISO 8601格式） |
| hourly[].temp | string | 温度（摄氏度） |
| hourly[].icon | string | 天气状况图标代码 |
| hourly[].text | string | 天气状况文字描述 |
| hourly[].wind360 | string | 风向360角度 |
| hourly[].windDir | string | 风向 |
| hourly[].windScale | string | 风力等级 |
| hourly[].windSpeed | string | 风速（km/h） |
| hourly[].humidity | string | 相对湿度（%） |
| hourly[].pop | string | 降水概率（%） |
| hourly[].precip | string | 降水量（mm） |
| hourly[].pressure | string | 大气压强（hPa） |
| hourly[].cloud | string | 云量（%） |
| hourly[].dew | string | 露点温度（℃） |

---

### 2.3 获取3天天气预报

**接口说明**

获取指定城市未来3天的每日天气预报数据，包括最高/最低温度、天气状况、风向、湿度、降水量等。

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | `/v7/weather/3d` |
| 数据格式 | JSON |

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| location | string | 是 | 城市ID/经纬度（如：101010100 北京，114.05,22.55 深圳） |
| lang | string | 否 | 语言，默认 `zh` |
| unit | string | 否 | 单位，`m` 公制（默认）/ `i` 英制 |
| key | string | 是 | API KEY |

**请求示例**

```
GET https://nk6k5qtwn8.re.qweatherapi.com/v7/weather/3d?location=101010100&key=77b19b0da0c24164a60e7f437d7b7cc5
```

**响应示例（实际测试结果）**

```json
{
    "code": "200",
    "updateTime": "2026-06-11T10:05+08:00",
    "fxLink": "https://www.qweather.com/weather/beijing-101010100.html",
    "daily": [
        {
            "fxDate": "2026-06-11",
            "sunrise": "04:46",
            "sunset": "19:43",
            "moonrise": "01:29",
            "moonset": "15:32",
            "moonPhase": "残月",
            "moonPhaseIcon": "807",
            "tempMax": "31",
            "tempMin": "19",
            "iconDay": "100",
            "textDay": "晴",
            "iconNight": "151",
            "textNight": "多云",
            "wind360Day": "180",
            "windDirDay": "南风",
            "windScaleDay": "1-3",
            "windSpeedDay": "3",
            "wind360Night": "225",
            "windDirNight": "西南风",
            "windScaleNight": "1-3",
            "windSpeedNight": "3",
            "humidity": "38",
            "precip": "0.0",
            "pressure": "1002",
            "vis": "25",
            "cloud": "0",
            "uvIndex": "11"
        },
        {
            "fxDate": "2026-06-12",
            "sunrise": "04:46",
            "sunset": "19:44",
            "moonrise": "01:57",
            "moonset": "16:49",
            "moonPhase": "残月",
            "moonPhaseIcon": "807",
            "tempMax": "32",
            "tempMin": "20",
            "iconDay": "302",
            "textDay": "雷阵雨",
            "iconNight": "302",
            "textNight": "雷阵雨",
            "wind360Day": "180",
            "windDirDay": "南风",
            "windScaleDay": "1-3",
            "windSpeedDay": "3",
            "wind360Night": "45",
            "windDirNight": "东北风",
            "windScaleNight": "1-3",
            "windSpeedNight": "3",
            "humidity": "50",
            "precip": "0.0",
            "pressure": "996",
            "vis": "24",
            "cloud": "70",
            "uvIndex": "7"
        },
        {
            "fxDate": "2026-06-13",
            "sunrise": "04:46",
            "sunset": "19:44",
            "moonrise": "02:33",
            "moonset": "18:06",
            "moonPhase": "残月",
            "moonPhaseIcon": "807",
            "tempMax": "28",
            "tempMin": "19",
            "iconDay": "302",
            "textDay": "雷阵雨",
            "iconNight": "151",
            "textNight": "多云",
            "wind360Day": "90",
            "windDirDay": "东风",
            "windScaleDay": "1-3",
            "windSpeedDay": "3",
            "wind360Night": "0",
            "windDirNight": "北风",
            "windScaleNight": "1-3",
            "windSpeedNight": "3",
            "humidity": "77",
            "precip": "13.6",
            "pressure": "997",
            "vis": "24",
            "cloud": "80",
            "uvIndex": "6"
        }
    ],
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
| --- | --- | --- |
| code | string | 状态码 |
| updateTime | string | API最近更新时间 |
| daily[].fxDate | string | 预报日期 |
| daily[].sunrise | string | 日出时间 |
| daily[].sunset | string | 日落时间 |
| daily[].moonrise | string | 月出时间 |
| daily[].moonset | string | 月落时间 |
| daily[].moonPhase | string | 月相名称 |
| daily[].moonPhaseIcon | string | 月相图标代码 |
| daily[].tempMax | string | 最高温度（℃） |
| daily[].tempMin | string | 最低温度（℃） |
| daily[].iconDay | string | 白天天气图标代码 |
| daily[].textDay | string | 白天天气状况文字描述 |
| daily[].iconNight | string | 夜间天气图标代码 |
| daily[].textNight | string | 夜间天气状况文字描述 |
| daily[].wind360Day | string | 白天风向360角度 |
| daily[].windDirDay | string | 白天风向 |
| daily[].windScaleDay | string | 白天风力等级 |
| daily[].windSpeedDay | string | 白天风速（km/h） |
| daily[].wind360Night | string | 夜间风向360角度 |
| daily[].windDirNight | string | 夜间风向 |
| daily[].windScaleNight | string | 夜间风力等级 |
| daily[].windSpeedNight | string | 夜间风速（km/h） |
| daily[].humidity | string | 相对湿度（%） |
| daily[].precip | string | 降水量（mm） |
| daily[].pressure | string | 大气压强（hPa） |
| daily[].vis | string | 能见度（km） |
| daily[].cloud | string | 云量（%） |
| daily[].uvIndex | string | 紫外线指数 |

---
