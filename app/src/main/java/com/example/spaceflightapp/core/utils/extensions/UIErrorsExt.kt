package com.example.spaceflightapp.core.utils.extensions

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.spaceflightapp.R
import com.example.spaceflightapp.core.utils.helpers.UiError

@DrawableRes
fun UiError.iconRes(): Int = when (this) {
    UiError.Network -> R.drawable.ic_wifi_off
    is UiError.Server,
    is UiError.WithMessage,
    UiError.Unknown -> R.drawable.ic_cloud_off
}

@StringRes
fun UiError.titleRes(): Int = when (this) {
    UiError.Network -> R.string.connection_lost_title
    is UiError.Server -> R.string.server_error_title
    is UiError.WithMessage,
    UiError.Unknown -> R.string.something_went_wrong_title
}

fun UiError.message(context: Context): String = when (this) {
    UiError.Network -> "" //
    is UiError.Server -> context.getString(R.string.server_error_detail, code)
    is UiError.WithMessage -> message
    UiError.Unknown -> ""
}
