package com.example.spaceflightapp.data

import com.example.spaceflightapp.data.remote.SpaceflightNewsApi
import com.example.spaceflightapp.data.repository.ArticleRepositoryImpl
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class ArticleRepositoryImplTest {

    private lateinit var server: MockWebServer
    private lateinit var api: SpaceflightNewsApi

    @Before fun setup() {
        server = MockWebServer().also { it.start() }
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        api = retrofit.create(SpaceflightNewsApi::class.java)
    }

    @After fun tearDown() { server.shutdown() }

    @Test
    fun `getArticles maps the list correctly`() = runTest {
        val body = """
        {"count":1,"next":null,"previous":null,
         "results":[{"id":1,"title":"Hello","authors":[],
         "url":"https://x","image_url":null,"news_site":"SN","summary":"S",
         "published_at":"2024-01-01T00:00:00Z","updated_at":null,"featured":false,
         "launches":[],"events":[]}]}
        """.trimIndent()
        server.enqueue(MockResponse().setResponseCode(200).setBody(body))

        val repo = ArticleRepositoryImpl(api)
        val list = repo.getArticles(null, 20, 0)

        assertEquals(1, list.size)
        assertEquals("Hello", list.first().title)

        val req = server.takeRequest()
        assertTrue(req.requestUrl!!.encodedPath == "/v4/articles/")
    }

    @Test
    fun `repo sends ordering-limit-offset-search`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200)
                .setBody("""{"count":0,"next":null,"previous":null,"results":[]}""")
        )

        val repo = ArticleRepositoryImpl(api)
        repo.getArticles(search = "mars", limit = 30, offset = 60)

        val req = server.takeRequest()
        assertEquals("/v4/articles/", req.requestUrl!!.encodedPath)
        assertEquals("-published_at", req.requestUrl!!.queryParameter("ordering"))
        assertEquals("30", req.requestUrl!!.queryParameter("limit"))
        assertEquals("60", req.requestUrl!!.queryParameter("offset"))
        assertEquals("mars", req.requestUrl!!.queryParameter("search"))
    }

}
