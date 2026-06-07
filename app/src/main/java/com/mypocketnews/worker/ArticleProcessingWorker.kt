package com.mypocketnews.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerFactory
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.mypocketnews.data.db.AppDatabase
import com.mypocketnews.data.db.ArticleStatus
import com.mypocketnews.data.extraction.ArticleExtractor
import com.mypocketnews.data.extraction.ExtractionException
import com.mypocketnews.data.llm.LlmClientFactory
import com.mypocketnews.data.llm.LlmAuthException
import com.mypocketnews.data.llm.LlmException
import com.mypocketnews.data.settings.SettingsRepository
import com.mypocketnews.notifications.AppNotifier
import okhttp3.OkHttpClient

class ArticleProcessingWorker(
    appContext: Context,
    params: WorkerParameters,
    private val db: AppDatabase,
    private val settingsRepository: SettingsRepository,
    private val okHttpClient: OkHttpClient
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val articleId = inputData.getLong(KEY_ARTICLE_ID, -1L)
        if (articleId == -1L) return Result.failure()

        val article = db.articleDao().getById(articleId)
            ?: return Result.failure()

        val notifier = AppNotifier()

        db.articleDao().update(article.copy(status = ArticleStatus.PROCESSING))

        setForeground(
            ForegroundInfo(
                articleId.toInt(),
                notifier.buildProcessingNotification(applicationContext)
            )
        )

        return try {
            val extracted = ArticleExtractor(okHttpClient).extract(article.url)

            val config = settingsRepository.getConfig()
                ?: run {
                    db.articleDao().update(
                        article.copy(
                            status = ArticleStatus.FAILED,
                            errorMessage = "No LLM provider configured"
                        )
                    )
                    notifier.postError(applicationContext, "No LLM provider configured", articleId.toInt(), extracted.title.ifBlank { null })
                    return Result.failure()
                }

            val summary = try {
                LlmClientFactory.create(config, okHttpClient).summarise(extracted.title, extracted.bodyText)
            } catch (e: LlmAuthException) {
                db.articleDao().update(
                    article.copy(
                        status = ArticleStatus.FAILED,
                        errorMessage = "Invalid API key — check Settings"
                    )
                )
                notifier.postError(applicationContext, "Invalid API key — check Settings", articleId.toInt(), extracted.title.ifBlank { null })
                return Result.failure()
            } catch (e: LlmException) {
                db.articleDao().update(
                    article.copy(
                        status = ArticleStatus.FAILED,
                        errorMessage = e.message
                    )
                )
                notifier.postError(applicationContext, e.message ?: "LLM error", articleId.toInt(), extracted.title.ifBlank { null })
                return Result.failure()
            }

            db.articleDao().update(
                article.copy(
                    status = ArticleStatus.DONE,
                    title = extracted.title,
                    summary = summary,
                    bodyText = extracted.bodyText,
                    providerUsed = config.provider,
                    modelUsed = config.model,
                    processedAt = System.currentTimeMillis()
                )
            )

            notifier.postCompletion(applicationContext, extracted.title, articleId.toInt())
            Result.success()
        } catch (e: ExtractionException) {
            db.articleDao().update(
                article.copy(
                    status = ArticleStatus.FAILED,
                    errorMessage = e.message
                )
            )
            notifier.postError(applicationContext, e.message ?: "Extraction failed", articleId.toInt(), null)
            Result.failure()
        }
    }

    companion object {
        const val KEY_ARTICLE_ID = "article_id"
    }
}

class AppWorkerFactory(
    private val db: AppDatabase,
    private val settingsRepository: SettingsRepository,
    private val okHttpClient: OkHttpClient
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return if (workerClassName == ArticleProcessingWorker::class.java.name) {
            ArticleProcessingWorker(appContext, workerParameters, db, settingsRepository, okHttpClient)
        } else null
    }
}
