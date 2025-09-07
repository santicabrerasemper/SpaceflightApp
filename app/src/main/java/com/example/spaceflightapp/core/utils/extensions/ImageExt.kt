package com.example.spaceflightapp.core.utils.extensions

import android.widget.ImageView
import coil.load
import com.example.spaceflightapp.R

fun ImageView.loadArticleThumb(url: String?) {
    val data = url?.trim()?.takeIf { it.isNotEmpty() && it.lowercase() != "null" }

    this.load(data) {
        crossfade(true)
        placeholder(R.drawable.ic_placeholder)
        error(R.drawable.ic_placeholder)
        fallback(R.drawable.ic_placeholder)
    }
}
