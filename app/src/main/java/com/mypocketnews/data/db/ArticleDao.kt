package com.mypocketnews.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {

    @Insert
    suspend fun insert(article: Article): Long

    @Update
    suspend fun update(article: Article)

    @Query("SELECT * FROM articles ORDER BY created_at DESC")
    fun getAll(): Flow<List<Article>>

    @Query("SELECT * FROM articles WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Article?

    @Query("SELECT * FROM articles WHERE id = :id LIMIT 1")
    fun getByIdFlow(id: Long): Flow<Article?>
}
