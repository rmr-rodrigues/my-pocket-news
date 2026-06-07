package com.mypocketnews.data.settings

data class LlmProviderConfig(
    val provider: String,
    val apiKey: String,
    val model: String,
    val baseUrl: String
)
