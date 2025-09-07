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
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class ArticleRepositoryDetailTest {

    private lateinit var server: MockWebServer
    private lateinit var api: SpaceflightNewsApi

    @Before
    fun setup() {
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

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `getArticle detail maps correctly`() = runTest {
        val body = """
            {
              "id": 7,
              "title": "Detail",
              "authors": [],
              "url": "https://example.com/d",
              "image_url": null,
              "news_site": "SN",
              "summary": "detail",
              "published_at": "2024-01-02T00:00:00Z",
              "updated_at": null,
              "featured": true,
              "launches": [],
              "events": []
            }
        """.trimIndent()
        server.enqueue(MockResponse().setResponseCode(200).setBody(body))

        val repo = ArticleRepositoryImpl(api)
        val art = repo.getArticleDetail(7)

        assertEquals(7L, art.id)
        assertEquals("Detail", art.title)

        val req = server.takeRequest()
        assertEquals("/v4/articles/7/", req.requestUrl!!.encodedPath)
    }

    @Test(expected = HttpException::class)
    fun `getArticle details returns HttpException on 404`() = runTest {
        server.enqueue(MockResponse().setResponseCode(404))

        val repo = ArticleRepositoryImpl(api)
        // Propagates the exception as there is no custom error handling
        repo.getArticleDetail(999)

        // Verify the route is correct
        val req = server.takeRequest()
        assertTrue(req.requestUrl!!.encodedPath == "/v4/articles/999/")
    }
}
