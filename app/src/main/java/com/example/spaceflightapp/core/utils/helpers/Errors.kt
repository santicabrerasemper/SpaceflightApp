package com.example.spaceflightapp.core.utils.helpers

import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.coroutines.CancellationException

sealed class UiError {
    data object Network : UiError()
    data class Server(val code: Int) : UiError()
    data object Unknown : UiError()
    data class WithMessage(val message: String) : UiError()
}

fun Throwable.toUiError(): UiError = when (this) {
    is CancellationException -> throw this
    is UnknownHostException, is ConnectException, is SocketTimeoutException, is IOException -> UiError.Network
    is HttpException -> UiError.Server(code())
    else -> UiError.Unknown
}

