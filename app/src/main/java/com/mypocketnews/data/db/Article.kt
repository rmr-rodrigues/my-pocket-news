package com.mypocketnews.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "articles")
data class Article(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "url", index = true)
    val url: String = "",

    @ColumnInfo(name = "title", defaultValue = "")
    val title: String = "",

    @ColumnInfo(name = "summary", defaultValue = "")
    val summary: String = "",

    @ColumnInfo(name = "body_text", defaultValue = "")
    val bodyText: String = "",

    @ColumnInfo(name = "provider_used", defaultValue = "")
    val providerUsed: String = "",

    @ColumnInfo(name = "model_used", defaultValue = "")
    val modelUsed: String = "",

    @ColumnInfo(name = "status", defaultValue = "PENDING")
    val status: ArticleStatus = ArticleStatus.PENDING,

    @ColumnInfo(name = "error_message")
    val errorMessage: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "processed_at")
    val processedAt: Long? = null
)
