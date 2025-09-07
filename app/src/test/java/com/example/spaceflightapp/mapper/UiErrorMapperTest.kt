package com.example.spaceflightapp.mapper

import com.example.spaceflightapp.core.utils.helpers.UiError
import com.example.spaceflightapp.core.utils.helpers.toUiError
import kotlinx.coroutines.CancellationException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class UiErrorMapperTest {

    @Test
    fun `UnknownHostException maps to Network`() {
        val e = UnknownHostException("no internet")
        assertEquals(UiError.Network, e.toUiError())
    }

    @Test
    fun `ConnectException maps to Network`() {
        val e = ConnectException("connection refused")
        assertEquals(UiError.Network, e.toUiError())
    }

    @Test
    fun `SocketTimeoutException maps to Network`() {
        val e = SocketTimeoutException("timeout")
        assertEquals(UiError.Network, e.toUiError())
    }

    @Test
    fun `plain IOException maps to Network`() {
        val e = IOException("io")
        assertEquals(UiError.Network, e.toUiError())
    }

    @Test
    fun `HttpException maps to Server with code`() {
        val ex = httpException(code = 503)
        val mapped = ex.toUiError()
        assertTrue(mapped is UiError.Server)
        assertEquals(503, (mapped as UiError.Server).code)
    }

    @Test
    fun `RuntimeException with message maps to WithMessage`() {
        val e = RuntimeException("boom!")
        val mapped = e.toUiError()
        assertTrue(mapped is UiError.WithMessage)
        assertEquals("boom!", (mapped as UiError.WithMessage).message)
    }

    @Test
    fun `RuntimeException without message maps to Unknown`() {
        val e = RuntimeException() // message == null
        val mapped = e.toUiError()
        assertEquals(UiError.Unknown, mapped)
    }

    @Test
    fun `CancellationException is rethrown`() {
        assertThrows(CancellationException::class.java) {
            CancellationException("cancel").toUiError()
        }
    }

    private fun httpException(code: Int): HttpException {
        val body = "error".toResponseBody("application/json".toMediaType())
        val resp = Response.error<Any>(code, body)
        return HttpException(resp)
    }
}