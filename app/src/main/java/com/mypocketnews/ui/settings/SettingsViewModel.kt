package com.mypocketnews.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.mypocketnews.data.settings.LlmProviderConfig
import com.mypocketnews.data.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val provider: String = "openai",
    val apiKey: String = "",
    val model: String = "",
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
            provider = config?.provider ?: "openai",
            apiKey = config?.apiKey ?: "",
            model = config?.model ?: getDefaultModel(config?.provider ?: "openai")
        )
        if (config == null) {
            _uiState.value = _uiState.value.copy(model = getDefaultModel(_uiState.value.provider))
        }
    }

    fun save(provider: String, apiKey: String, model: String) {
        val baseUrl = when (provider) {
            "openai" -> "https://api.openai.com/v1"
            "openrouter" -> "https://openrouter.ai/api/v1"
            else -> "https://api.openai.com/v1"
        }
        val config = LlmProviderConfig(provider, apiKey, model, baseUrl)
        viewModelScope.launch {
            settingsRepository.saveConfig(config)
            _uiState.update { it.copy(saved = true) }
        }
    }

    fun onProviderChange(provider: String) {
        _uiState.update { it.copy(provider = provider, model = getDefaultModel(provider)) }
    }

    fun onApiKeyChange(apiKey: String) {
        _uiState.update { it.copy(apiKey = apiKey) }
    }

    fun onModelChange(model: String) {
        _uiState.update { it.copy(model = model) }
    }

    private fun getDefaultModel(provider: String): String {
        return when (provider) {
            "openai" -> "gpt-4o-mini"
            "openrouter" -> "openai/gpt-4o-mini"
            else -> "gpt-4o-mini"
        }
    }

    companion object {
        fun factory(settingsRepository: SettingsRepository) = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                SettingsViewModel(settingsRepository) as T
        }
    }
}
