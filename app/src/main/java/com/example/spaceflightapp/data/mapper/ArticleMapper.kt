package com.example.spaceflightapp.data.mapper

import com.example.spaceflightapp.data.remote.dto.*
import com.example.spaceflightapp.domain.model.*

private fun SocialsDto?.toDomain() = Socials(
    x = this?.x, youtube = this?.youtube, instagram = this?.instagram,
    linkedin = this?.linkedin, mastodon = this?.mastodon, bluesky = this?.bluesky
)

private fun AuthorDto.toDomain() = Author(
    name = name.orEmpty(),
    socials = socials.toDomain()
)

private fun LaunchRefDto.toDomain(): LaunchRef? =
    launchId?.let { LaunchRef(id = it, provider = provider.orEmpty()) }

private fun EventRefDto.toDomain(): EventRef? =
    eventId?.let { EventRef(id = it, provider = provider.orEmpty()) }

fun SpaceflightArticleDto.toDomain() = Article(
    id = id,
    title = title,
    url = url,
    imageUrl = imageUrl,
    newsSite = newsSite.orEmpty(),
    summary = summary.orEmpty(),
    publishedAt = publishedAt.orEmpty(),
    updatedAt = updatedAt,
    featured = featured ?: false,
    authors = (authors ?: emptyList()).map { it.toDomain() },
    launches = (launches ?: emptyList()).mapNotNull { it.toDomain() },
    events = (events ?: emptyList()).mapNotNull { it.toDomain() }
)