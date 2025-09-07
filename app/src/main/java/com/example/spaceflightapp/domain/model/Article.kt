package com.example.spaceflightapp.domain.model

data class Article(
    val id: Long,
    val title: String,
    val url: String,
    val imageUrl: String?,
    val newsSite: String,
    val summary: String,
    val publishedAt: String,
    val updatedAt: String?,
    val featured: Boolean,
    val authors: List<Author>,
    val launches: List<LaunchRef>,
    val events: List<EventRef>
)

data class Author(
    val name: String,
    val socials: Socials
)

data class Socials(
    val x: String? = null,
    val youtube: String? = null,
    val instagram: String? = null,
    val linkedin: String? = null,
    val mastodon: String? = null,
    val bluesky: String? = null
)

data class LaunchRef(
    val id: String,
    val provider: String
)

data class EventRef(
    val id: Int,
    val provider: String
)