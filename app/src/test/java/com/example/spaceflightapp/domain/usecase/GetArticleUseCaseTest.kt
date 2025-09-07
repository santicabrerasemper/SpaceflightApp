package com.example.spaceflightapp.domain.usecase


import com.example.spaceflightapp.domain.model.Article
import com.example.spaceflightapp.domain.repository.ArticleRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException

class GetArticlesUseCaseTest {

    private val repo: ArticleRepository = mockk(relaxed = true)
    private val useCase = GetArticlesUseCase(repo)

    @Test
    fun `delegates params and returns list`() = runTest {
        val expected = listOf(
            Article(
                1,
                "T",
                "u",
                null,
                "SN",
                "S",
                "2024",
                null,
                false,
                emptyList(),
                emptyList(),
                emptyList()
            )
        )
        coEvery { repo.getArticles(search = "mars", limit = 30, offset = 60) } returns expected

        val result = useCase(search = "mars", limit = 30, offset = 60)

        assertEquals(expected, result)
        coVerify(exactly = 1) { repo.getArticles("mars", 30, 60) }
    }

    @Test(expected = HttpException::class)
    fun `propagates repository exception`() = runTest {
        coEvery { repo.getArticles(any(), any(), any()) } throws mockk<HttpException>()
        useCase(search = null, limit = 20, offset = 0)
    }
}