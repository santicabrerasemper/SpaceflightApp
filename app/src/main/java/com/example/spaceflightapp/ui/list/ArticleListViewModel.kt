package com.example.spaceflightapp.ui.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spaceflightapp.core.utils.helpers.relevanceScore
import com.example.spaceflightapp.core.utils.helpers.toUiError
import com.example.spaceflightapp.domain.model.Article
import com.example.spaceflightapp.domain.usecase.GetArticlesUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class ArticleListViewModel(
    private val getArticles: GetArticlesUseCase,
    private val savedState: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val KEY_QUERY = "query"
        private const val KEY_OFFSET = "offset"
        private const val PAGE_SIZE = 20
    }

    private val _state = MutableStateFlow(
        ArticlesUiState(
            query = savedState[KEY_QUERY] ?: "",
            offset = savedState[KEY_OFFSET] ?: 0
        )
    )
    val state: StateFlow<ArticlesUiState> = _state.asStateFlow()

    private val queryFlow = MutableStateFlow(_state.value.query)
    private var fetchJob: Job? = null

    init {
        observeQuery()
        onQueryChanged(_state.value.query)
    }

    fun onQueryChanged(newQuery: String) {
        _state.update { it.copy(query = newQuery, error = null) }
        savedState[KEY_QUERY] = newQuery
        queryFlow.value = newQuery
    }

    fun refresh() {
        val query = _state.value.query
        val showSpinner = _state.value.items.isEmpty()
        _state.update { it.copy(
            isRefreshing = !showSpinner,
            isLoading = showSpinner,
            error = null,
            endReached = false
        ) }
        fetchPage(query, offset = 0, append = false)
    }

    fun loadNextPage() {
        val s = _state.value
        if (s.isLoading || s.isRefreshing || s.endReached) return
        _state.update { it.copy(isPaging = true) }
        fetchPage(s.query, s.offset + PAGE_SIZE, append = true)
    }

    @OptIn(FlowPreview::class)
    private fun observeQuery() {
        viewModelScope.launch {
            queryFlow
                .debounce(350)
                .distinctUntilChanged()
                .collectLatest { query -> fetchFirstPage(query) }
        }
    }

    private fun fetchFirstPage(query: String) {
        _state.update {
            it.copy(
                isLoading = true,
                error = null,
                items = emptyList(),
                offset = 0,
                endReached = false
            )
        }
        fetchPage(query, offset = 0, append = false)
    }

    private fun fetchPage(query: String, offset: Int, append: Boolean) {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            try {

                val page = getArticles(query.takeIf { it.isNotBlank() }, PAGE_SIZE, offset)

                val filtered = if (query.isBlank()) page
                else page.filter { it.title.contains(query, ignoreCase = true) }

                _state.update { prev ->
                    val combined = if (append) (prev.items + filtered).distinctBy { it.id } else filtered
                    val ranked = if (query.isBlank()) combined else
                        combined.sortedWith(
                            compareByDescending<Article> { relevanceScore(it.title, query) }
                                .thenByDescending { it.publishedAt }
                        )
                    prev.copy(
                        isLoading = false,
                        isRefreshing = false,
                        items = ranked,
                        offset = offset,
                        endReached = page.size < PAGE_SIZE,
                        error = null,
                        hasLoadedOnce = true
                    )
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Timber.tag("ArticleListViewModel").e(e, "Error fetching articles")
                _state.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        isPaging = false,
                        error = e.toUiError(),
                        hasLoadedOnce = true
                    )
                }
            }
        }
    }
}
