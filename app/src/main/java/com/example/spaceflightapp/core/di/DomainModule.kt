package com.example.spaceflightapp.core.di

import com.example.spaceflightapp.domain.usecase.GetArticleDetailUseCase
import com.example.spaceflightapp.domain.usecase.GetArticlesUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { GetArticlesUseCase(get()) }
    factory { GetArticleDetailUseCase(get()) }
}