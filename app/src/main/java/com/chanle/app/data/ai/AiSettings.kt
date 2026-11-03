package com.chanle.app.data.ai

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

data class AiConfig(
    val endpointUrl: String = "",
    val modelName: String = "",
    val dailyLimit: Int = 20,
    val hasKey: Boolean = false,
    val usedToday: Int = 0,
    val promptTokens: Long = 0,
    val completionTokens: Long = 0,
) {
    val isConfigured: Boolean
        get() = endpointUrl.isNotBlank() && modelName.isNotBlank() && hasKey

    val remainingToday: Int get() = (dailyLimit - usedToday).coerceAtLeast(0)

    val canCall: Boolean get() = isConfigured && remainingToday > 0

    val totalTokens: Long get() = promptTokens + completionTokens
}

object CallBudget {
    fun canCall(used: Int, limit: Int): Boolean = limit > 0 && used < limit
    fun remaining(used: Int, limit: Int): Int = (limit - used).coerceAtLeast(0)
}

class AiSettings(context: Context) {

    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("ai_settings", Context.MODE_PRIVATE)
    private val keyStore = AiKeyStore(appContext)
    private val _config = MutableStateFlow(readConfig())
    val config: StateFlow<AiConfig> = _config.asStateFlow()

    private fun readConfig(): AiConfig {
        val today = LocalDate.now().toEpochDay()
        val used = if (prefs.getLong(KEY_USAGE_DAY, 0L) == today) {
            prefs.getInt(KEY_USAGE_COUNT, 0)
        } else {
            0
        }
        return AiConfig(
            endpointUrl = prefs.getString(KEY_URL, "").orEmpty(),
            modelName = prefs.getString(KEY_MODEL, "").orEmpty(),
            dailyLimit = prefs.getInt(KEY_LIMIT, 20),
            hasKey = keyStore.has(),
            usedToday = used,
            promptTokens = prefs.getLong(KEY_PROMPT_TOKENS, 0L),
            completionTokens = prefs.getLong(KEY_COMPLETION_TOKENS, 0L),
        )
    }

    fun refresh() {
        _config.value = readConfig()
    }

    fun setEndpoint(url: String) {
        prefs.edit().putString(KEY_URL, EndpointNormalizer.normalize(url)).apply()
        refresh()
    }

    fun setModel(model: String) {
        prefs.edit().putString(KEY_MODEL, model.trim()).apply()
        refresh()
    }

    fun saveKey(apiKey: String) {
        if (apiKey.isBlank()) keyStore.clear() else keyStore.save(apiKey)
        refresh()
    }

    fun clearKey() {
        keyStore.clear()
        refresh()
    }

    fun maskedKey(): String? = keyStore.masked()

    fun setDailyLimit(limit: Int) {
        prefs.edit().putInt(KEY_LIMIT, limit.coerceIn(1, 100)).apply()
        refresh()
    }

    fun recordCall() {
        val today = LocalDate.now().toEpochDay()
        val used = if (prefs.getLong(KEY_USAGE_DAY, 0L) == today) {
            prefs.getInt(KEY_USAGE_COUNT, 0)
        } else {
            0
        }
        prefs.edit()
            .putLong(KEY_USAGE_DAY, today)
            .putInt(KEY_USAGE_COUNT, used + 1)
            .apply()
        refresh()
    }

    fun recordUsage(promptTokens: Int, completionTokens: Int) {
        prefs.edit()
            .putLong(KEY_PROMPT_TOKENS, prefs.getLong(KEY_PROMPT_TOKENS, 0L) + promptTokens)
            .putLong(KEY_COMPLETION_TOKENS, prefs.getLong(KEY_COMPLETION_TOKENS, 0L) + completionTokens)
            .apply()
        refresh()
    }

    fun readKey(): String? = keyStore.read()

    private companion object {
        const val KEY_URL = "endpointUrl"
        const val KEY_MODEL = "modelName"
        const val KEY_LIMIT = "dailyLimit"
        const val KEY_USAGE_DAY = "usageDay"
        const val KEY_USAGE_COUNT = "usageCount"
        const val KEY_PROMPT_TOKENS = "promptTokens"
        const val KEY_COMPLETION_TOKENS = "completionTokens"
    }
}
