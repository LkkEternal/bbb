package com.chanle.app.data.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.addJsonObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

class AiClient(private val settings: AiSettings) {

    suspend fun testConnection(): Result<String> = runCall { config, key ->
        post(
            config = config,
            apiKey = key,
            system = "你是连通性测试助手。只回复两个字：可用",
            user = "测试一下",
            maxTokens = 32,
        )
    }

    suspend fun generateRecipe(dishName: String): Result<String> {
        val name = dishName.trim()
        if (name.isBlank()) return Result.failure(IllegalStateException("请输入菜名"))
        if (name.length > 30) return Result.failure(IllegalStateException("菜名最多 30 个字"))
        return runCall { config, key ->
            post(
                config = config,
                apiKey = key,
                system = RecipePrompt.system(),
                user = RecipePrompt.user(name),
                maxTokens = 2600,
            )
        }
    }

    suspend fun recognizeIngredientsFromImage(context: Context, imageUri: Uri): Result<List<String>> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val config = settings.config.value
                if (config.endpointUrl.isBlank() || config.modelName.isBlank()) {
                    error("还没有填写接口地址和模型名")
                }
                if (!CallBudget.canCall(config.usedToday, config.dailyLimit)) {
                    error("今日调用次数已用完，明天再试或调高上限")
                }
                val key = settings.readKey() ?: error("还没有填写 API Key")

                val base64Image = loadImageAsBase64(context, imageUri)
                val response = postWithImage(
                    config = config,
                    apiKey = key,
                    system = ImageRecognitionPrompt.system(),
                    user = ImageRecognitionPrompt.user(),
                    imageBase64 = base64Image,
                    maxTokens = 800,
                )
                settings.recordCall()

                val ingredients = parseIngredientsFromResponse(response)
                ingredients
            }
        }
    }

    private fun loadImageAsBase64(context: Context, imageUri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(imageUri)
            ?: error("无法读取图片")
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        val scaledBitmap = scaleBitmap(bitmap, 1024)
        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        val imageBytes = outputStream.toByteArray()
        return Base64.encodeToString(imageBytes, Base64.NO_WRAP)
    }

    private fun scaleBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxSize && height <= maxSize) return bitmap

        val ratio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int
        if (width > height) {
            newWidth = maxSize
            newHeight = (maxSize / ratio).toInt()
        } else {
            newHeight = maxSize
            newWidth = (maxSize * ratio).toInt()
        }
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun parseIngredientsFromResponse(response: String): List<String> {
        return runCatching {
            val obj = Json.parseToJsonElement(response).jsonObject
            val ingredientsArray = obj["ingredients"]?.jsonArray
            if (ingredientsArray != null) {
                ingredientsArray.mapNotNull { it.jsonPrimitive.contentOrNull }
                    .filter { it.isNotBlank() }
            } else {
                val content = obj["content"]?.jsonPrimitive?.contentOrNull
                    ?: response
                extractIngredientsFromText(content)
            }
        }.getOrElse {
            extractIngredientsFromText(response)
        }
    }

    private fun extractIngredientsFromText(text: String): List<String> {
        val cleanText = text.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        return runCatching {
            val obj = Json.parseToJsonElement(cleanText).jsonObject
            obj["ingredients"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull }
                ?.filter { it.isNotBlank() }
                ?: emptyList()
        }.getOrElse {
            cleanText.split("[，,、\n]".toRegex())
                .map { it.trim() }
                .filter { it.isNotBlank() && it.length <= 20 }
        }
    }

    private suspend fun runCall(block: (AiConfig, String) -> String): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val config = settings.config.value
                if (config.endpointUrl.isBlank() || config.modelName.isBlank()) {
                    error("还没有填写接口地址和模型名")
                }
                if (!CallBudget.canCall(config.usedToday, config.dailyLimit)) {
                    error("今日调用次数已用完，明天再试或调高上限")
                }
                val key = settings.readKey() ?: error("还没有填写 API Key")
                val text = block(config, key)
                settings.recordCall()
                text
            }
        }

    private fun post(
        config: AiConfig,
        apiKey: String,
        system: String,
        user: String,
        maxTokens: Int,
    ): String {
        if (!config.endpointUrl.startsWith("https://", ignoreCase = true)) {
            error("接口地址必须使用 HTTPS")
        }
        val body = buildJsonObject {
            put("model", config.modelName)
            put("temperature", 0.2)
            put("max_tokens", maxTokens)
            put("stream", false)
            putJsonArray("messages") {
                addJsonObject {
                    put("role", "system")
                    put("content", system)
                }
                addJsonObject {
                    put("role", "user")
                    put("content", user)
                }
            }
        }.toString()

        val raw = postRaw(config.endpointUrl, apiKey, body, config.modelName)
        val root = runCatching { Json.parseToJsonElement(raw).jsonObject }
            .getOrElse { error("服务端返回的不是合法 JSON") }
        val message = root["choices"]
            ?.jsonArray
            ?.firstOrNull()
            ?.jsonObject
            ?.get("message")
            ?.jsonObject

        val content = message?.get("content")?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }
            ?: message?.get("reasoning_content")?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }
            ?: error("模型没有返回内容")

        root["usage"]?.jsonObject?.let { usage ->
            settings.recordUsage(
                promptTokens = usage["prompt_tokens"]?.jsonPrimitive?.intOrNull ?: 0,
                completionTokens = usage["completion_tokens"]?.jsonPrimitive?.intOrNull ?: 0,
            )
        }
        return content
    }

    private fun postRaw(
        url: String,
        apiKey: String,
        body: String,
        model: String,
        redirectsLeft: Int = 1,
    ): String {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 20_000
            readTimeout = 90_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Authorization", "Bearer ${apiKey.trim()}")
            setRequestProperty("User-Agent", "chanle-app")
        }
        try {
            connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            val code = connection.responseCode

            if (code in 300..399 && redirectsLeft > 0) {
                val location = connection.getHeaderField("Location")
                if (location != null) return postRaw(location, apiKey, body, model, redirectsLeft - 1)
            }

            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) error(describe(code, url, model, text))
            return text
        } finally {
            connection.disconnect()
        }
    }

    private fun describe(code: Int, url: String, model: String, body: String): String {
        val detail = runCatching {
            val obj = Json.parseToJsonElement(body).jsonObject
            obj["error"]?.jsonObject?.get("message")?.jsonPrimitive?.contentOrNull
                ?: obj["message"]?.jsonPrimitive?.contentOrNull
        }.getOrNull()?.takeIf { it.isNotBlank() }

        val reason = when (code) {
            400 -> "请求被拒绝，多是模型名不对"
            401, 403 -> "Key 无效或没有这个模型的权限"
            404 -> "地址不对或模型不存在"
            422 -> "请求参数不被接受"
            429 -> "调用太频繁或额度不足"
            in 500..599 -> "服务端繁忙"
            else -> "调用失败"
        }
        val target = url.substringAfter("://").substringBefore('?')
        return buildString {
            append(reason).append("（HTTP ").append(code).append("）")
            append("\n地址：").append(target)
            append("\n模型：").append(model)
            if (detail != null) append("\n服务器：").append(detail.take(200))
        }
    }

    private fun postWithImage(
        config: AiConfig,
        apiKey: String,
        system: String,
        user: String,
        imageBase64: String,
        maxTokens: Int,
    ): String {
        val body = buildJsonObject {
            put("model", config.modelName)
            put("temperature", 0.2)
            put("max_tokens", maxTokens)
            put("stream", false)
            putJsonArray("messages") {
                addJsonObject {
                    put("role", "system")
                    put("content", system)
                }
                addJsonObject {
                    put("role", "user")
                    putJsonArray("content") {
                        addJsonObject {
                            put("type", "text")
                            put("text", user)
                        }
                        addJsonObject {
                            put("type", "image_url")
                            val imageUrlJson = buildJsonObject {
                                put("url", "data:image/jpeg;base64,${imageBase64}")
                            }
                            put("image_url", imageUrlJson)
                        }
                    }
                }
            }
        }.toString()

        val raw = postRaw(config.endpointUrl, apiKey, body, config.modelName)
        val root = runCatching { Json.parseToJsonElement(raw).jsonObject }
            .getOrElse { error("服务端返回的不是合法 JSON") }
        val message = root["choices"]
            ?.jsonArray
            ?.firstOrNull()
            ?.jsonObject
            ?.get("message")
            ?.jsonObject

        val content = message?.get("content")?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }
            ?: message?.get("reasoning_content")?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }
            ?: error("模型没有返回内容")

        root["usage"]?.jsonObject?.let { usage ->
            settings.recordUsage(
                promptTokens = usage["prompt_tokens"]?.jsonPrimitive?.intOrNull ?: 0,
                completionTokens = usage["completion_tokens"]?.jsonPrimitive?.intOrNull ?: 0,
            )
        }
        return content
    }
}