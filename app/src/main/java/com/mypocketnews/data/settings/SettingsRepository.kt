package com.mypocketnews.data.settings

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SettingsRepository(context: Context) {

    private val masterKey = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS, MasterKey.KeyParameterBuilder.AES256_GCM_HKDF_SHA256)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM_HKDF_SHA256)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "mpn_settings",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private companion object {
        const val KEY_PROVIDER = "pref_provider"
        const val KEY_API_KEY = "pref_api_key"
        const val KEY_MODEL = "pref_model"
        const val KEY_BASE_URL = "pref_base_url"
    }

    fun getConfig(): LlmProviderConfig? {
        val apiKey = prefs.getString(KEY_API_KEY, "") ?: ""
        if (apiKey.isBlank()) return null

        return LlmProviderConfig(
            provider = prefs.getString(KEY_PROVIDER, "openai") ?: "openai",
            apiKey = apiKey,
            model = prefs.getString(KEY_MODEL, "") ?: "",
            baseUrl = prefs.getString(KEY_BASE_URL, "") ?: ""
        )
    }

    fun saveConfig(config: LlmProviderConfig) {
        prefs.edit().apply {
            putString(KEY_PROVIDER, config.provider)
            putString(KEY_API_KEY, config.apiKey)
            putString(KEY_MODEL, config.model)
            putString(KEY_BASE_URL, config.baseUrl)
            apply()
        }
    }

    fun isConfigured(): Boolean {
        val apiKey = prefs.getString(KEY_API_KEY, "") ?: ""
        return apiKey.isNotBlank()
    }
}
