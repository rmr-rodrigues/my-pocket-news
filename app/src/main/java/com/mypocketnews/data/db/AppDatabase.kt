package com.mypocketnews.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

@Database(entities = [Article::class], version = 1)
@TypeConverters(ArticleStatusConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun articleDao(): ArticleDao
}

class ArticleStatusConverter {
    @TypeConverter
    fun fromArticleStatus(status: ArticleStatus): String = status.name

    @TypeConverter
    fun toArticleStatus(value: String): ArticleStatus = ArticleStatus.valueOf(value)
}
