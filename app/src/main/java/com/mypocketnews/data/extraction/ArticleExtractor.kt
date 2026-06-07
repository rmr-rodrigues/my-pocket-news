package com.mypocketnews.data.extraction

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.dankito.readability4j.Readability4J
import okhttp3.OkHttpClient
import org.jsoup.Jsoup

class ArticleExtractor(private val okHttpClient: OkHttpClient) {

    suspend fun extract(url: String): ExtractedArticle = withContext(Dispatchers.IO) {
        val response = okHttpClient.newCall(
            okhttp3.Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.9,pt;q=0.8,gl;q=0.7")
                .header("Accept-Encoding", "gzip, deflate, br")
                .header("Connection", "keep-alive")
                .header("Sec-Fetch-Dest", "document")
                .header("Sec-Fetch-Mode", "navigate")
                .header("Sec-Fetch-Site", "none")
                .header("Sec-Fetch-User", "?1")
                .header("Sec-CH-UA", "\"Not_A Brand\";v=\"8\", \"Chromium\";v=\"120\", \"Google Chrome\";v=\"120\"")
                .header("Sec-CH-UA-Mobile", "?1")
                .header("Sec-CH-UA-Platform", "\"Android\"")
                .header("Upgrade-Insecure-Requests", "1")
                .build()
        ).execute()

        if (!response.isSuccessful) {
            throw ExtractionException("Failed to fetch article: ${url} — HTTP ${response.code}")
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
