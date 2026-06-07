package com.mypocketnews.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mypocketnews.data.db.AppDatabase
import com.mypocketnews.data.db.Article
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ArticleListViewModel(private val db: AppDatabase) : ViewModel() {

    val articles: StateFlow<List<Article>> = db.articleDao().getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    companion object {
        fun factory(db: AppDatabase) = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ArticleListViewModel(db) as T
        }
    }
}
