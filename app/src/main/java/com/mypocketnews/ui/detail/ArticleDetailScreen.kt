package com.mypocketnews.ui.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mypocketnews.data.db.ArticleStatus

@Composable
fun ArticleDetailScreen(
    viewModel: ArticleDetailViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    when (uiState) {
                        is ArticleDetailUiState.Success -> {
                            Text((uiState as ArticleDetailUiState.Success).article.title)
                        }
                        else -> Text("Article")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (uiState) {
            is ArticleDetailUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is ArticleDetailUiState.NotFound -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Article not found")
                }
            }
            is ArticleDetailUiState.Success -> {
                val article = (uiState as ArticleDetailUiState.Success).article
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = article.title,
                        style = MaterialTheme.typography.headlineSmall
                    )

                    if (article.status == ArticleStatus.FAILED) {
                        Text(
                            text = article.errorMessage ?: "Unknown error",
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    if (article.status == ArticleStatus.DONE) {
                        Text("Summary", style = MaterialTheme.typography.titleLarge)
                        Text(article.summary)

                        Text("Full article", style = MaterialTheme.typography.titleLarge)
                        Text(article.bodyText)
                    }

                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.url))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Open original")
                    }
                }
            }
        }
    }
}
