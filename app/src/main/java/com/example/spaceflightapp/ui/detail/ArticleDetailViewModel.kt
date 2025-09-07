package com.example.spaceflightapp.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spaceflightapp.core.utils.helpers.toUiError
import com.example.spaceflightapp.domain.usecase.GetArticleDetailUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class ArticleDetailViewModel(
    private val getArticleDetail: GetArticleDetailUseCase,
    private val articleId: Long
) : ViewModel() {

    private val _state = MutableStateFlow<ArticleDetailUiState>(ArticleDetailUiState.Loading)
    val state: StateFlow<ArticleDetailUiState> = _state.asStateFlow()

    init {
        _state.value = ArticleDetailUiState.Loading
        load()
    }

    fun retry() {
        _state.value = ArticleDetailUiState.Loading
        load()
    }

    fun refresh() {
        val current = _state.value
        _state.value = if (current is ArticleDetailUiState.Content)
            current.copy(isRefreshing = true)
        else
            ArticleDetailUiState.Loading
        load()
    }

    private fun load() {
        viewModelScope.launch {
            try {
                val article = getArticleDetail(articleId)
                _state.value = ArticleDetailUiState.Content(article, isRefreshing = false)
            } catch (ce: CancellationException) {
                throw ce
            } catch (e: Throwable) {
                Timber.tag("ArticleDetailViewModel").e(e, "Error fetching article detail")
                val current = _state.value
                _state.value =
                    if (current is ArticleDetailUiState.Content && current.isRefreshing) {
                        current.copy(isRefreshing = false)
                    } else {
                        ArticleDetailUiState.Error(e.toUiError())
                    }
            }
        }
    }
}
