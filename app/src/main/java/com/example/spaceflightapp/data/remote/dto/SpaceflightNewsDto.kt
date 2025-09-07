package com.example.spaceflightapp.data.remote.dto

import com.squareup.moshi.Json

data class ArticlesPageDto(
    @Json(name = "count") val count: Int,
    @Json(name = "next") val next: String?,
    @Json(name = "previous") val previous: String?,
    @Json(name = "results") val results: List<SpaceflightArticleDto>
)

data class SpaceflightArticleDto(
    @Json(name = "id") val id: Long,
    @Json(name = "title") val title: String,
    @Json(name = "authors") val authors: List<AuthorDto>?,
    @Json(name = "url") val url: String,
    @Json(name = "image_url") val imageUrl: String?,
    @Json(name = "news_site") val newsSite: String?,
    @Json(name = "summary") val summary: String?,
    @Json(name = "published_at") val publishedAt: String?,
    @Json(name = "updated_at") val updatedAt: String?,
    @Json(name = "featured") val featured: Boolean?,
    @Json(name = "launches") val launches: List<LaunchRefDto>?,
    @Json(name = "events") val events: List<EventRefDto>?
)

data class AuthorDto(
    @Json(name = "name") val name: String?,
    @Json(name = "socials") val socials: SocialsDto?
)

data class SocialsDto(
    @Json(name = "x") val x: String?,
    @Json(name = "youtube") val youtube: String?,
    @Json(name = "instagram") val instagram: String?,
    @Json(name = "linkedin") val linkedin: String?,
    @Json(name = "mastodon") val mastodon: String?,
    @Json(name = "bluesky") val bluesky: String?
)

data class LaunchRefDto(
    @Json(name = "launch_id") val launchId: String?,
    @Json(name = "provider") val provider: String?
)

data class EventRefDto(
    @Json(name = "event_id") val eventId: Int?,
    @Json(name = "provider") val provider: String?
)