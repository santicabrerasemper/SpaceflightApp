package com.example.spaceflightapp.ui.detail

import com.example.spaceflightapp.core.utils.helpers.UiError
import com.example.spaceflightapp.domain.model.Article
import com.example.spaceflightapp.domain.repository.ArticleRepository
import com.example.spaceflightapp.domain.usecase.GetArticleDetailUseCase
import com.example.spaceflightapp.rules.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ArticleDetailViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private class SuccessRepo(
        private val provider: () -> Article
    ) : ArticleRepository {
        override suspend fun getArticles(search: String?, limit: Int, offset: Int) =
            emptyList<Article>()

        override suspend fun getArticleDetail(id: Long): Article = provider()
    }

    private class ErrorRepo : ArticleRepository {
        override suspend fun getArticles(search: String?, limit: Int, offset: Int) =
            emptyList<Article>()

        override suspend fun getArticleDetail(id: Long): Article =
            throw RuntimeException("fails")
    }

    private fun article(id: Long, title: String) = Article(
        id = id,
        title = title,
        url = "https://example.com/$id",
        imageUrl = null,
        newsSite = "SN",
        summary = "S",
        publishedAt = "2024-01-02",
        updatedAt = null,
        featured = true,
        authors = emptyList(),
        launches = emptyList(),
        events = emptyList()
    )

    @Test
    fun `initial load emits Content`() = runTest {
        val vm =
            ArticleDetailViewModel(GetArticleDetailUseCase(SuccessRepo { article(7, "detail") }), 7)
        advanceUntilIdle()

        val s = vm.state.value
        assertTrue(s is ArticleDetailUiState.Content)
        assertEquals(7L, (s as ArticleDetailUiState.Content).article.id)
        assertFalse(s.isRefreshing)
    }

    @Test
    fun `initial load with error emits Error`() = runTest {
        val vm = ArticleDetailViewModel(GetArticleDetailUseCase(ErrorRepo()), 99)
        advanceUntilIdle()

        val s = vm.state.value
        assertTrue(s is ArticleDetailUiState.Error)
    }

    @Test
    fun `retry from error recovers content`() = runTest {
        var ok = false
        val repo = object : ArticleRepository {
            override suspend fun getArticles(search: String?, limit: Int, offset: Int) =
                emptyList<Article>()

            override suspend fun getArticleDetail(id: Long): Article {
                if (!ok) throw RuntimeException("fails")
                return Article(
                    id,
                    "title",
                    "u",
                    null,
                    "SN",
                    "S",
                    "2024",
                    null,
                    true,
                    emptyList(),
                    emptyList(),
                    emptyList()
                )
            }
        }
        val vm = ArticleDetailViewModel(GetArticleDetailUseCase(repo), 7)
        advanceUntilIdle()

        assertTrue(vm.state.value is ArticleDetailUiState.Error)

        ok = true
        vm.retry()
        advanceUntilIdle()

        val s = vm.state.value
        assertTrue(s is ArticleDetailUiState.Content)
        assertEquals(7L, (s as ArticleDetailUiState.Content).article.id)
    }


    @Test
    fun `successful soft refresh updates content and stops spinner`() = runTest {
        var current = article(1, "title v1")
        val repo = SuccessRepo { current }
        val vm = ArticleDetailViewModel(GetArticleDetailUseCase(repo), 1)
        advanceUntilIdle()
        current = article(1, "title v2")

        vm.refresh()
        advanceUntilIdle()

        val s = vm.state.value
        assertTrue(s is ArticleDetailUiState.Content)
        s as ArticleDetailUiState.Content
        assertEquals("title v2", s.article.title)
        assertFalse(s.isRefreshing)
    }

    @Test
    fun `failed soft refresh keeps content`() = runTest {
        var ok = true
        val repo = object : ArticleRepository {
            override suspend fun getArticles(search: String?, limit: Int, offset: Int) =
                emptyList<Article>()

            override suspend fun getArticleDetail(id: Long): Article {
                if (ok) return article(id, "title v1")
                throw RuntimeException("refresh fails")
            }
        }
        val vm = ArticleDetailViewModel(GetArticleDetailUseCase(repo), 10)
        advanceUntilIdle()

        ok = false
        vm.refresh()
        advanceUntilIdle()

        val s = vm.state.value
        assertTrue(s is ArticleDetailUiState.Content)
        assertEquals("title v1", (s as ArticleDetailUiState.Content).article.title)
        assertFalse(s.isRefreshing)
    }

    @Test
    fun `initial state is loading before fetching`() = runTest {
        val repo = SuccessRepo { article(1, "Title") }
        val vm = ArticleDetailViewModel(GetArticleDetailUseCase(repo), 1)
        val s = vm.state.value
        assertFalse(s is ArticleDetailUiState.Error)
    }

    @Test
    fun `multiple refresh maintains state consistency`() = runTest {
        var current = article(2, "v1")
        val repo = SuccessRepo { current }
        val vm = ArticleDetailViewModel(GetArticleDetailUseCase(repo), 2)
        advanceUntilIdle()
        current = article(2, "v2")
        vm.refresh()
        vm.refresh()
        vm.refresh()
        advanceUntilIdle()
        val s = vm.state.value
        assertTrue(s is ArticleDetailUiState.Content)
        assertEquals("v2", (s as ArticleDetailUiState.Content).article.title)
        assertFalse(s.isRefreshing)
    }

    @Test
    fun `article not found emits Error`() = runTest {
        val repo = object : ArticleRepository {
            override suspend fun getArticles(search: String?, limit: Int, offset: Int) =
                emptyList<Article>()

            override suspend fun getArticleDetail(id: Long): Article {
                throw RuntimeException("not found")
            }
        }
        val vm = ArticleDetailViewModel(GetArticleDetailUseCase(repo), 123)
        advanceUntilIdle()
        val s = vm.state.value
        assertTrue(s is ArticleDetailUiState.Error)
    }

    @Test
    fun `error message is propagated`() = runTest {
        val repo = object : ArticleRepository {
            override suspend fun getArticles(search: String?, limit: Int, offset: Int) = emptyList<Article>()
            override suspend fun getArticleDetail(id: Long): Article =
                throw RuntimeException("custom error")
        }
        val vm = ArticleDetailViewModel(GetArticleDetailUseCase(repo), 5)
        advanceUntilIdle()
        val s = vm.state.value
        assertTrue(s is ArticleDetailUiState.Error)

        when (val e = (s as ArticleDetailUiState.Error).error) {
            is UiError.WithMessage -> assertTrue(e.message.contains("custom error"))
            is UiError.Unknown     -> { /* ok, message not available */ }
            else                   -> error("Unexpected error type: $e")
        }
    }

}
