package com.example.spaceflightapp.domain.usecase

import com.example.spaceflightapp.domain.model.Article
import com.example.spaceflightapp.domain.repository.ArticleRepository

class GetArticleDetailUseCase(
    private val repository: ArticleRepository
) {
    suspend operator fun invoke(id: Long): Article =
        repository.getArticleDetail(id)
}