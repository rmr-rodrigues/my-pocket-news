package com.mypocketnews

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.mypocketnews.data.db.Article
import com.mypocketnews.data.db.ArticleStatus
import com.mypocketnews.worker.ArticleProcessingWorker
import kotlinx.coroutines.launch

class ShareActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            val app = application as MyPocketNewsApp

            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            val url = extractUrl(sharedText)

            if (url == null) {
                Toast.makeText(this@ShareActivity, "Not a valid URL", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }

            if (!app.settingsRepository.isConfigured()) {
                val intent = Intent(this@ShareActivity, MainActivity::class.java).apply {
                    putExtra("start_destination", "settings")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)
                finish()
                return@launch
            }

            val article = Article(
                url = url,
                status = ArticleStatus.PENDING,
                createdAt = System.currentTimeMillis()
            )
            val articleId = app.database.articleDao().insert(article)

            val work = OneTimeWorkRequestBuilder<ArticleProcessingWorker>()
                .setInputData(androidx.work.Data.Builder()
                    .putLong(ArticleProcessingWorker.KEY_ARTICLE_ID, articleId)
                    .build())
                .build()

            WorkManager.getInstance(this@ShareActivity).enqueue(work)

            Toast.makeText(this@ShareActivity, "Saving article…", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun extractUrl(text: String?): String? {
        if (text.isNullOrEmpty()) return null
        val trimmed = text.trim()
        if (Patterns.WEB_URL.matcher(trimmed).matches()) {
            return trimmed
        }
        val parts = trimmed.split(Regex("\\s+"))
        for (part in parts) {
            if (Patterns.WEB_URL.matcher(part).matches()) {
                return part
            }
        }
        return null
    }
}
