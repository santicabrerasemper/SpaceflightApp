package com.example.spaceflightapp.ui.detail
import com.example.spaceflightapp.core.utils.helpers.UiError
import com.example.spaceflightapp.domain.model.Article

sealed interface ArticleDetailUiState {
    object Loading : ArticleDetailUiState
    data class Content(val article: Article, val isRefreshing: Boolean = false) : ArticleDetailUiState
    data class Error(val error: UiError) : ArticleDetailUiState
}