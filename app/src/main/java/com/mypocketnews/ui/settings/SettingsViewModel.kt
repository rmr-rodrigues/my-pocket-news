package com.mypocketnews.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mypocketnews.data.settings.LlmProviderConfig
import com.mypocketnews.data.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

val OpenRouterModels = listOf(
    ModelOption("deepseek/deepseek-v4-flash", "DeepSeek V4 Flash"),
    ModelOption("qwen/qwen3-35b-a3b", "Qwen 3.6 35B A3B"),
    ModelOption("qwen/qwen3-plus", "Qwen 3.6 Plus"),
    ModelOption("deepseek/deepseek-v3.2", "DeepSeek V3.2"),
    ModelOption("google/gemma-4-27b", "Gemma 4 27B")
)

data class ModelOption(val id: String, val label: String)

data class SettingsUiState(
    val apiKey: String = "",
    val modelId: String = OpenRouterModels.first().id,
    val saved: Boolean = false
)

class SettingsViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadConfig()
    }

    private fun loadConfig() {
        val config = settingsRepository.getConfig()
        _uiState.value = SettingsUiState(
            apiKey = config?.apiKey ?: "",
            modelId = config?.model ?: OpenRouterModels.first().id
        )
        if (config == null) {
            _uiState.value = _uiState.value.copy(modelId = OpenRouterModels.first().id)
        }
    }

    fun save(apiKey: String, modelId: String) {
        val config = LlmProviderConfig("openrouter", apiKey, modelId, "https://openrouter.ai/api/v1")
        viewModelScope.launch {
            settingsRepository.saveConfig(config)
            _uiState.update { it.copy(saved = true) }
        }
    }

    fun onApiKeyChange(apiKey: String) {
        _uiState.update { it.copy(apiKey = apiKey) }
    }

    fun onModelChange(modelId: String) {
        _uiState.update { it.copy(modelId = modelId) }
    }

    companion object {
        fun factory(settingsRepository: SettingsRepository) = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                SettingsViewModel(settingsRepository) as T
        }
    }
}
