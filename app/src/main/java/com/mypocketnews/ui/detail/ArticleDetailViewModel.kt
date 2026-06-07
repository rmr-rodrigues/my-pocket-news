package com.mypocketnews.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mypocketnews.data.db.AppDatabase
import com.mypocketnews.data.db.Article
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ArticleDetailUiState {
    data object Loading : ArticleDetailUiState()
    data class Success(val article: Article) : ArticleDetailUiState()
    data object NotFound : ArticleDetailUiState()
}

class ArticleDetailViewModel(
    private val db: AppDatabase,
    private val articleId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow<ArticleDetailUiState>(ArticleDetailUiState.Loading)
    val uiState: StateFlow<ArticleDetailUiState> = _uiState.asStateFlow()

    init {
        loadArticle()
    }

    private fun loadArticle() {
        viewModelScope.launch {
            db.articleDao().getByIdFlow(articleId).collect { a ->
                _uiState.value = if (a != null) {
                    ArticleDetailUiState.Success(a)
                } else {
                    ArticleDetailUiState.NotFound
                }
            }
        }
    }

    companion object {
        fun factory(db: AppDatabase, articleId: Long) = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ArticleDetailViewModel(db, articleId) as T
        }
    }
}
