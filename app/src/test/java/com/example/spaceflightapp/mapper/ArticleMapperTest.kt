package com.example.spaceflightapp.mapper

import com.example.spaceflightapp.data.mapper.toDomain
import com.example.spaceflightapp.data.remote.dto.SpaceflightArticleDto
import org.junit.Assert.assertEquals
import org.junit.Test

class ArticleMapperTest {
    @Test
    fun `mapper applies defaults and filters refs without id`() {
        val dto = SpaceflightArticleDto(
            id = 1, title = "T", authors = null, url = "u",
            imageUrl = null, newsSite = null, summary = null,
            publishedAt = null, updatedAt = null, featured = null,
            launches = listOf(
                // one without id, one with id
                com.example.spaceflightapp.data.remote.dto.LaunchRefDto(null, "prov"),
                com.example.spaceflightapp.data.remote.dto.LaunchRefDto("uuid", "prov2")
            ),
            events = listOf(
                com.example.spaceflightapp.data.remote.dto.EventRefDto(null, "p"),
                com.example.spaceflightapp.data.remote.dto.EventRefDto(99, "p2")
            )
        )

        val a = dto.toDomain()
        assertEquals("", a.newsSite)             // newsSite.orEmpty()
        assertEquals("", a.summary)              // summary.orEmpty()
        assertEquals("", a.publishedAt)          // publishedAt.orEmpty()
        assertEquals(false, a.featured)          // featured ?: false
        assertEquals(1, a.launches.size)         // filtered out the one without id
        assertEquals("uuid", a.launches[0].id)
        assertEquals(1, a.events.size)           // filtered out the one without id
        assertEquals(99, a.events[0].id)
    }
}
