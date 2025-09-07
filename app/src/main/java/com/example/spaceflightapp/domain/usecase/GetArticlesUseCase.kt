package com.example.spaceflightapp.domain.usecase

import com.example.spaceflightapp.domain.model.Article
import com.example.spaceflightapp.domain.repository.ArticleRepository

class GetArticlesUseCase(
    private val repository: ArticleRepository
) {
    suspend operator fun invoke(
        search: String?,
        limit: Int,
        offset: Int
    ): List<Article> = repository.getArticles(search, limit, offset)
}