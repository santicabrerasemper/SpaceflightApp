package com.example.spaceflightapp.data.remote

import com.example.spaceflightapp.data.remote.dto.ArticlesPageDto
import com.example.spaceflightapp.data.remote.dto.SpaceflightArticleDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SpaceflightNewsApi {
    @GET("v4/articles/")
    suspend fun getArticles(
        @Query("search") search: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("ordering") ordering: String = "-published_at"
    ): ArticlesPageDto

    @GET("v4/articles/{id}/")
    suspend fun getArticle(@Path("id") id: Long): SpaceflightArticleDto
}