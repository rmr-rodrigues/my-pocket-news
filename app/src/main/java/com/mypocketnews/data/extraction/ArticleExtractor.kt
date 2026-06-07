package com.mypocketnews.data.extraction

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.dankito.readability4j.Readability4J
import okhttp3.OkHttpClient
import org.jsoup.Jsoup

class ArticleExtractor(private val okHttpClient: OkHttpClient) {

    suspend fun extract(url: String): ExtractedArticle = withContext(Dispatchers.IO) {
        val response = okHttpClient.newCall(
            okhttp3.Request.Builder().url(url).build()
        ).execute()

        if (!response.isSuccessful) {
            throw ExtractionException("HTTP ${response.code}: ${response.message}")
        }

        val html = response.body?.string()
            ?: throw ExtractionException("Empty response body")

        val jsoupDocument = Jsoup.parse(html, url)
        val article = Readability4J(url, jsoupDocument).parse()

        val title = if (!article.title.isNullOrBlank()) article.title!!
        else jsoupDocument.title().ifEmpty { "" }

        val bodyText = Jsoup.parse(article.content ?: "").text()

        if (title.isBlank() && bodyText.isBlank()) {
            throw ExtractionException("Could not extract readable content from this page")
        }

        val truncatedBody = if (bodyText.length > 8000) {
            val boundary = bodyText.lastIndexOf('.', 8000).takeIf { it > 0 }
                ?: bodyText.lastIndexOf('\n', 8000).takeIf { it > 0 }
            if (boundary != null) bodyText.substring(0, boundary)
            else bodyText.substring(0, 8000)
        } else bodyText

        ExtractedArticle(title, truncatedBody)
    }
}
