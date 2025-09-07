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

class GetArticleDetailUseCaseTest {

    private val repo: ArticleRepository = mockk(relaxed = true)
    private val useCase = GetArticleDetailUseCase(repo)

    @Test
    fun `delegates id and returns article`() = runTest {
        val expected = Article(7,"Detail","u",null,"SN","S","2024",null,true, emptyList(), emptyList(), emptyList())
        coEvery { repo.getArticleDetail(7) } returns expected

        val a = useCase(7)

        assertEquals(expected, a)
        coVerify(exactly = 1) { repo.getArticleDetail(7) }
    }

    @Test(expected = HttpException::class)
    fun `propagates exception`() = runTest {
        coEvery { repo.getArticleDetail(any()) } throws mockk<HttpException>()
        useCase(99)
    }
}