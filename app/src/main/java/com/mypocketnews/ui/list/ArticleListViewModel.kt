package com.mypocketnews.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mypocketnews.data.db.AppDatabase
import com.mypocketnews.data.db.Article
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ArticleListViewModel(private val db: AppDatabase) : ViewModel() {

    val articles: StateFlow<List<Article>> = db.articleDao().getAll()
        .map { articles ->
            articles
        }

    companion object {
        fun factory(db: AppDatabase) = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ArticleListViewModel(db) as T
        }
    }
}
