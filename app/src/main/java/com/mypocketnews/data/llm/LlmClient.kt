package com.mypocketnews.data.llm

interface LlmClient {
    suspend fun summarise(title: String, bodyText: String): String
}
