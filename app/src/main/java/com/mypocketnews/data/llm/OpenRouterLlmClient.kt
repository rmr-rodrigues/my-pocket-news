package com.mypocketnews.data.llm

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import com.mypocketnews.data.settings.LlmProviderConfig
import com.mypocketnews.data.llm.LlmRateLimitException

class OpenRouterLlmClient(
    private val config: LlmProviderConfig,
    private val okHttpClient: OkHttpClient
) : LlmClient {

    override suspend fun summarise(title: String, bodyText: String): String = withContext(Dispatchers.IO) {
        val maxRetries = 3
        val delays = listOf(5000L, 10000L, 20000L)

        var lastException: LlmRateLimitException? = null

        for (attempt in 0 until maxRetries) {
            try {
                return@withContext doSummarise(title, bodyText)
            } catch (e: LlmRateLimitException) {
                lastException = e
                if (attempt < maxRetries - 1) {
                    kotlinx.coroutines.delay(delays[attempt])
                }
            } catch (e: LlmException) {
                throw e
            }
        }

        throw lastException ?: LlmRateLimitException("OpenRouter rate limit hit after $maxRetries retries")
    }

    private suspend fun doSummarise(title: String, bodyText: String): String {
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
            .header("HTTP-Referer", "android-app://com.mypocketnews")
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .build()

        val response = okHttpClient.newCall(request).execute()

        if (response.code == 401 || response.code == 403) {
            throw LlmAuthException("Invalid API key — check Settings")
        }

        if (response.code == 429) {
            val errorBody = response.body?.string()?.take(200) ?: ""
            throw LlmRateLimitException("OpenRouter rate limit hit (model: ${config.model}): $errorBody")
        }

        if (!response.isSuccessful) {
            val errorBody = response.body?.string()?.take(200) ?: ""
            throw LlmException("OpenRouter error ${response.code} (model: ${config.model}): $errorBody")
        }

        val responseBody = response.body?.string() ?: throw LlmException("Empty LLM response")
        val json = JSONObject(responseBody)
        return json.getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
    }
}
