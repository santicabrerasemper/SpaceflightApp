package com.example.spaceflightapp.domain.repository

import com.example.spaceflightapp.domain.model.Article

interface ArticleRepository {
    suspend fun getArticles(search: String?, limit: Int, offset: Int): List<Article>
    suspend fun getArticleDetail(id: Long): Article
}