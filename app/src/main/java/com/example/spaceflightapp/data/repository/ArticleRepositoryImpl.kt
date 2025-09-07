package com.example.spaceflightapp.data.repository

import com.example.spaceflightapp.data.mapper.toDomain
import com.example.spaceflightapp.data.remote.SpaceflightNewsApi
import com.example.spaceflightapp.domain.model.Article
import com.example.spaceflightapp.domain.repository.ArticleRepository

class ArticleRepositoryImpl(
    private val api: SpaceflightNewsApi
) : ArticleRepository {

    override suspend fun getArticles(search: String?, limit: Int, offset: Int): List<Article> {
        val page = api.getArticles(search = search, limit = limit, offset = offset)
        return page.results.map { it.toDomain() }
    }

    override suspend fun getArticleDetail(id: Long): Article {
        return api.getArticle(id).toDomain()
    }
}