package com.example.spaceflightapp.ui.list

import androidx.lifecycle.SavedStateHandle
import com.example.spaceflightapp.domain.model.Article
import com.example.spaceflightapp.domain.repository.ArticleRepository
import com.example.spaceflightapp.domain.usecase.GetArticlesUseCase
import com.example.spaceflightapp.rules.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ArticleListViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    private class FakeRepo : ArticleRepository {
        var failNextFirstPage: Boolean = false
        var failNextSecondPage: Boolean = false

        override suspend fun getArticles(search: String?, limit: Int, offset: Int): List<Article> {
            if (offset == 0 && failNextFirstPage) {
                failNextFirstPage = false
                throw RuntimeException("fail first page")
            }
            if (offset == 20 && failNextSecondPage) {
                failNextSecondPage = false
                throw RuntimeException("fail second page")
            }
            return when (offset) {
                0   -> articles(1, 20, prefix = search ?: "A")
                20  -> articles(21, 10, prefix = search ?: "A") // < PAGE_SIZE -> endReached = true
                else -> emptyList()
            }
        }

        override suspend fun getArticleDetail(id: Long): Article =
            error("not used in list tests")

        private fun articles(startId: Int, count: Int, prefix: String): List<Article> =
            (startId until startId + count).map { i -> article(i.toLong(), "$prefix-$i") }

        private fun article(id: Long, title: String) = Article(
            id = id,
            title = title,
            url = "https://example.com/$id",
            imageUrl = null,
            newsSite = "SN",
            summary = "S",
            publishedAt = "2024-01-01",
            updatedAt = null,
            featured = false,
            authors = emptyList(),
            launches = emptyList(),
            events = emptyList()
        )
    }

    @Test
    fun `initial load returns 20 items`() = runTest {
        val vm = ArticleListViewModel(GetArticlesUseCase(FakeRepo()), SavedStateHandle())
        advanceTimeBy(400); advanceUntilIdle() // let debounce fire

        val s = vm.state.value
        assertFalse(s.isLoading)
        assertEquals(20, s.items.size)
        assertEquals(0, s.offset)
        assertFalse(s.endReached)
    }

    @Test
    fun `first load failure leaves empty list and sets error`() = runTest {
        val repo = object : ArticleRepository {
            override suspend fun getArticles(search: String?, limit: Int, offset: Int) =
                throw RuntimeException("fail first page")
            override suspend fun getArticleDetail(id: Long) = error("unused")
        }
        val vm = ArticleListViewModel(GetArticlesUseCase(repo), SavedStateHandle())

        advanceTimeBy(400); advanceUntilIdle()

        val s = vm.state.value
        assertFalse(s.isLoading)
        assertTrue(s.items.isEmpty())
        assertNotNull(s.error)
        assertEquals(0, s.offset)
    }

    @Test
    fun `query change with debounce resets to first page`() = runTest {
        val vm = ArticleListViewModel(GetArticlesUseCase(FakeRepo()), SavedStateHandle())
        advanceTimeBy(400); advanceUntilIdle()

        vm.onQueryChanged("mars")
        advanceTimeBy(400); advanceUntilIdle()

        val s = vm.state.value
        assertEquals("mars", s.query)
        assertEquals(20, s.items.size)
        assertTrue(s.items.first().title.startsWith("mars-"))
        assertEquals(0, s.offset)
    }

    @Test
    fun `loadNextPage appends and marks endReached`() = runTest {
        val vm = ArticleListViewModel(GetArticlesUseCase(FakeRepo()), SavedStateHandle())
        advanceTimeBy(400); advanceUntilIdle()

        vm.loadNextPage()
        advanceUntilIdle()

        val s = vm.state.value
        assertEquals(30, s.items.size)
        assertEquals(20, s.offset)
        assertTrue(s.endReached)
    }

    @Test
    fun `soft refresh failure keeps items`() = runTest {
        val repo = FakeRepo()
        val vm = ArticleListViewModel(GetArticlesUseCase(repo), SavedStateHandle())
        advanceTimeBy(400); advanceUntilIdle()

        val before = vm.state.value.items
        repo.failNextFirstPage = true

        vm.refresh()
        advanceUntilIdle()

        val after = vm.state.value
        assertEquals(before, after.items)
        assertFalse(after.isRefreshing)
        assertNotNull(after.error)
    }

    @Test
    fun `endReached prevents extra loadNextPage`() = runTest {
        val repo = object : ArticleRepository {
            override suspend fun getArticles(search: String?, limit: Int, offset: Int): List<Article> {
                return when (offset) {
                    0 -> (1..20).map { Article(it.toLong(),"A-$it","u",null,"SN","S","2024",null,false, emptyList(), emptyList(), emptyList()) }
                    20 -> (21..25).map { Article(it.toLong(),"A-$it","u",null,"SN","S","2024",null,false, emptyList(), emptyList(), emptyList()) }
                    else -> emptyList()
                }
            }
            override suspend fun getArticleDetail(id: Long) = error("unused")
        }

        val vm = ArticleListViewModel(GetArticlesUseCase(repo), SavedStateHandle())
        advanceTimeBy(400); advanceUntilIdle()

        vm.loadNextPage(); advanceUntilIdle()
        val afterPage2 = vm.state.value

        vm.loadNextPage(); advanceUntilIdle()
        val afterIgnored = vm.state.value

        assertTrue(afterPage2.endReached)
        assertEquals(afterPage2.items, afterIgnored.items)
        assertEquals(afterPage2.offset, afterIgnored.offset)
    }

    @Test
    fun `rapid query changes only apply the latest one`() = runTest {
        val vm = ArticleListViewModel(GetArticlesUseCase(FakeRepo()), SavedStateHandle())
        advanceTimeBy(400); advanceUntilIdle()

        vm.onQueryChanged("m")
        advanceTimeBy(200) // still within debounce window
        vm.onQueryChanged("mars")
        advanceTimeBy(400); advanceUntilIdle()

        val s = vm.state.value
        assertEquals("mars", s.query)
        assertTrue(s.items.first().title.startsWith("mars-"))
        assertNull(s.error)
    }

    @Test
    fun `pagination error keeps items and sets error`() = runTest {
        val repo = FakeRepo()
        val vm = ArticleListViewModel(GetArticlesUseCase(repo), SavedStateHandle())
        advanceTimeBy(400); advanceUntilIdle()

        repo.failNextSecondPage = true
        vm.loadNextPage()
        advanceUntilIdle()

        val s = vm.state.value
        assertEquals(20, s.items.size)
        assertNotNull(s.error)
        assertFalse(s.isLoading)
    }

    @Test
    fun `error is cleared after a successful load`() = runTest {
        val repo = FakeRepo().apply { failNextFirstPage = true }
        val vm = ArticleListViewModel(GetArticlesUseCase(repo), SavedStateHandle())
        advanceTimeBy(400); advanceUntilIdle()

        assertNotNull(vm.state.value.error)

        vm.onQueryChanged("new")
        advanceTimeBy(400); advanceUntilIdle()

        val s = vm.state.value
        assertNull(s.error)
        assertFalse(s.isLoading)
    }

    @Test
    fun `empty results keep list empty and set endReached true`() = runTest {
        val repo = object : ArticleRepository {
            override suspend fun getArticles(search: String?, limit: Int, offset: Int) = emptyList<Article>()
            override suspend fun getArticleDetail(id: Long) = error("not used in list tests")
        }
        val vm = ArticleListViewModel(GetArticlesUseCase(repo), SavedStateHandle())
        advanceTimeBy(400); advanceUntilIdle()

        val s = vm.state.value
        assertTrue(s.items.isEmpty())
        assertTrue(s.endReached)
        assertNull(s.error)
    }

    @Test
    fun `multiple refresh calls do not duplicate items or leave loading stuck`() = runTest {
        val vm = ArticleListViewModel(GetArticlesUseCase(FakeRepo()), SavedStateHandle())
        advanceTimeBy(400); advanceUntilIdle()

        val before = vm.state.value.items
        repeat(3) { vm.refresh() }
        advanceUntilIdle()

        val after = vm.state.value
        assertEquals(before, after.items)
        assertFalse(after.isLoading)
        assertFalse(after.isRefreshing)
    }

    @Test
    fun `successful refresh clears error`() = runTest {
        val repo = FakeRepo().apply { failNextFirstPage = true }
        val vm = ArticleListViewModel(GetArticlesUseCase(repo), SavedStateHandle())
        advanceTimeBy(400); advanceUntilIdle()
        assertNotNull(vm.state.value.error)

        vm.refresh()
        advanceUntilIdle()

        assertNull(vm.state.value.error)
    }
}
