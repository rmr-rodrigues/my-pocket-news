package com.mypocketnews.data.llm

import com.mypocketnews.data.settings.LlmProviderConfig
import okhttp3.OkHttpClient

object LlmClientFactory {
    fun create(config: LlmProviderConfig, okHttpClient: OkHttpClient): LlmClient =
        when (config.provider) {
            "openrouter" -> OpenRouterLlmClient(config, okHttpClient)
            else -> OpenAiLlmClient(config, okHttpClient)
        }
}
