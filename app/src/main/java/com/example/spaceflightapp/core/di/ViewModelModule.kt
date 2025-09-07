package com.example.spaceflightapp.core.di

import androidx.lifecycle.SavedStateHandle
import com.example.spaceflightapp.ui.detail.ArticleDetailViewModel
import com.example.spaceflightapp.ui.list.ArticleListViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { (state: SavedStateHandle) -> ArticleListViewModel(get(), state) }
    viewModel { (articleId: Long) -> ArticleDetailViewModel(get(), articleId) }
}