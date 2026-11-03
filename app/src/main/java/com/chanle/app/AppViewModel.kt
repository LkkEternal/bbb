package com.chanle.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chanle.app.data.Dish
import com.chanle.app.data.RecipeRepository
import com.chanle.app.data.ai.AiConfig
import com.chanle.app.data.ai.AiClient
import com.chanle.app.data.ai.AiSettings
import com.chanle.app.data.ai.RecipeGenerator
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

sealed interface GenerationState {
    data object Idle : GenerationState
    data object Loading : GenerationState
    data class Success(val dish: Dish) : GenerationState
    data class Error(val message: String) : GenerationState
}

sealed interface TestState {
    data object Idle : TestState
    data object Testing : TestState
    data class Success(val message: String) : TestState
    data class Error(val message: String) : TestState
}

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as ChanLeApplication).container
    private val repository: RecipeRepository = container.repository
    private val aiSettings: AiSettings = container.aiSettings
    private val aiClient: AiClient = container.aiClient
    private val generator: RecipeGenerator = container.recipeGenerator

    private val _curated = MutableStateFlow<List<Dish>>(emptyList())
    val curated: StateFlow<List<Dish>> = _curated.asStateFlow()

    private val _mine = MutableStateFlow<List<Dish>>(emptyList())
    val mine: StateFlow<List<Dish>> = _mine.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _loadError = MutableStateFlow<String?>(null)
    val loadError: StateFlow<String?> = _loadError.asStateFlow()

    val favorites: StateFlow<Set<String>> = repository.favorites
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    val aiConfig: StateFlow<AiConfig> = aiSettings.config

    private val _pendingDish = MutableStateFlow<Dish?>(null)
    val pendingDish: StateFlow<Dish?> = _pendingDish.asStateFlow()

    private val _generation = MutableStateFlow<GenerationState>(GenerationState.Idle)
    val generation: StateFlow<GenerationState> = _generation.asStateFlow()

    private val _testState = MutableStateFlow<TestState>(TestState.Idle)
    val testState: StateFlow<TestState> = _testState.asStateFlow()

    private var generationJob: Job? = null

    init {
        viewModelScope.launch {
            _loading.value = true
            runCatching { repository.loadCurated() }
                .onSuccess { _curated.value = it }
                .onFailure { _loadError.value = "菜谱数据加载失败：${it.message ?: "未知错误"}" }
            _mine.value = runCatching { repository.loadMine() }.getOrDefault(emptyList())
            _loading.value = false
        }
    }

    fun allDishes(): List<Dish> = _curated.value + _mine.value

    fun findDish(id: String?): Dish? {
        if (id == null) return null
        return _curated.value.firstOrNull { it.id == id } ?: _mine.value.firstOrNull { it.id == id }
    }

    fun search(query: String): List<Dish> = repository.search(allDishes(), query)

    fun matchLocal(input: String): Dish? = repository.matchLocal(_curated.value, input)

    fun findMineByName(name: String): Dish? {
        val target = repository.normalize(name)
        return _mine.value.firstOrNull { repository.normalize(it.name) == target }
    }

    fun dishesOf(category: String): List<Dish> = _curated.value.filter { it.category == category }

    fun toggleFavorite(id: String) {
        viewModelScope.launch { repository.toggleFavorite(id) }
    }

    fun generate(dishName: String) {
        if (generationJob?.isActive == true) return
        val config = aiSettings.config.value
        if (!config.isConfigured) {
            _generation.value = GenerationState.Error("还没配置 AI。请填写接口地址、模型名和 API Key 后再试。")
            return
        }
        if (!config.canCall) {
            _generation.value = GenerationState.Error("今日调用次数已用完，明天再试或调高每日上限。")
            return
        }
        _pendingDish.value = null
        _generation.value = GenerationState.Loading
        generationJob = viewModelScope.launch {
            generator.generate(dishName)
                .onSuccess { dish ->
                    _pendingDish.value = dish
                    _generation.value = GenerationState.Success(dish)
                }
                .onFailure { error ->
                    _generation.value = GenerationState.Error(humanize(error))
                }
        }
    }

    fun cancelGeneration() {
        generationJob?.cancel()
        generationJob = null
        _generation.value = GenerationState.Idle
    }

    fun consumeGenerationSuccess() {
        if (_generation.value is GenerationState.Success) {
            _generation.value = GenerationState.Idle
        }
    }

    fun clearGenerationError() {
        if (_generation.value is GenerationState.Error) {
            _generation.value = GenerationState.Idle
        }
    }

    fun saveAiDish(dish: Dish, onSaved: (String) -> Unit = {}) {
        viewModelScope.launch {
            val target = repository.normalize(dish.name)
            val next = _mine.value.filterNot { repository.normalize(it.name) == target } + dish
            repository.saveMine(next)
            _mine.value = next
            onSaved(dish.id)
        }
    }

    fun maskedKey(): String? = aiSettings.maskedKey()

    fun deleteMyDish(id: String) {
        viewModelScope.launch {
            val next = _mine.value.filterNot { it.id == id }
            repository.saveMine(next)
            _mine.value = next
            if (favorites.value.contains(id)) repository.toggleFavorite(id)
        }
    }

    fun saveAiConfig(
        endpoint: String,
        model: String,
        apiKey: String?,
        dailyLimit: Int,
    ): String? {
        if (endpoint.isBlank()) return "请填写接口地址"
        if (model.isBlank()) return "请填写模型名称"
        aiSettings.setEndpoint(endpoint)
        aiSettings.setModel(model)
        if (!apiKey.isNullOrBlank()) aiSettings.saveKey(apiKey)
        aiSettings.setDailyLimit(dailyLimit)
        return null
    }

    fun clearAiKey() {
        aiSettings.clearKey()
        _testState.value = TestState.Idle
    }

    fun saveAndTest(
        endpoint: String,
        model: String,
        apiKey: String?,
        dailyLimit: Int,
    ) {
        val error = saveAiConfig(endpoint, model, apiKey, dailyLimit)
        if (error != null) {
            _testState.value = TestState.Error(error)
            return
        }
        _testState.value = TestState.Testing
        viewModelScope.launch {
            aiClient.testConnection()
                .onSuccess { reply ->
                    _testState.value = TestState.Success("连接成功：${reply.trim().take(40)}")
                }
                .onFailure { error ->
                    _testState.value = TestState.Error(humanize(error))
                }
        }
    }

    fun testConnection() {
        if (_testState.value == TestState.Testing) return
        _testState.value = TestState.Testing
        viewModelScope.launch {
            aiClient.testConnection()
                .onSuccess { reply ->
                    _testState.value = TestState.Success("连接成功：${reply.trim().take(40)}")
                }
                .onFailure { error ->
                    _testState.value = TestState.Error(humanize(error))
                }
        }
    }

    fun clearTestState() {
        _testState.value = TestState.Idle
    }

    private fun humanize(error: Throwable): String {
        val message = error.message.orEmpty()
        return when {
            error is SocketTimeoutException || message.contains("timeout", ignoreCase = true) ->
                "生成超时了。请检查网络后重试，或换一个模型。"
            error is UnknownHostException ->
                "找不到接口服务器。请检查网络和接口地址。"
            error is ConnectException ->
                "连接不上接口。请检查网络，或确认地址是否正确。"
            message.isBlank() -> "生成失败，请重试。"
            else -> message
        }
    }
}
