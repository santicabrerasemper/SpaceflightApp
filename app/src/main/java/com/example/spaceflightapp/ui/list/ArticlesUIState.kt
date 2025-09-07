package com.example.spaceflightapp.ui.list

import com.example.spaceflightapp.core.utils.helpers.UiError
import com.example.spaceflightapp.domain.model.Article

data class ArticlesUiState(
    val isLoading: Boolean = true,
    val items: List<Article> = emptyList(),
    val isRefreshing: Boolean = false,
    val isPaging: Boolean = false,
    val query: String = "",
    val error: UiError? = null,
    val offset: Int = 0,
    val endReached: Boolean = false,
    val hasLoadedOnce: Boolean = false
)