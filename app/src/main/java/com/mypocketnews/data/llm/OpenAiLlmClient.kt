package com.mypocketnews.data.llm

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import com.mypocketnews.data.settings.LlmProviderConfig

class OpenAiLlmClient(
    private val config: LlmProviderConfig,
    private val okHttpClient: OkHttpClient
) : LlmClient {

    override suspend fun summarise(title: String, bodyText: String): String = withContext(Dispatchers.IO) {
        val requestBody = JSONObject().apply {
            put("model", config.model)
            put("max_tokens", 1024)
            put("temperature", 0.3)
            put("messages", org.json.JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", "You are a news summariser. Summarise the key points of the following article concisely, in the same language as the article. Be factual and cover all main topics.")
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", "$title\n\n$bodyText")
                })
            })
        }.toString()

        val request = okhttp3.Request.Builder()
            .url("${config.baseUrl}/chat/completions")
            .header("Authorization", "Bearer ${config.apiKey}")
            .header("Content-Type", "application/json")
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .build()

        val response = okHttpClient.newCall(request).execute()

        if (response.code == 401 || response.code == 403) {
            throw LlmAuthException("Invalid API key — check Settings")
        }

        if (!response.isSuccessful) {
            val errorBody = response.body?.string()?.take(200) ?: ""
            throw LlmException("LLM API error ${response.code}: $errorBody")
        }

        val responseBody = response.body?.string() ?: throw LlmException("Empty LLM response")
        val json = JSONObject(responseBody)
        json.getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
    }
}
