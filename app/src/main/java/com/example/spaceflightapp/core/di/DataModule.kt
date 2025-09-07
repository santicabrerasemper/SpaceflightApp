package com.example.spaceflightapp.core.di

import com.example.spaceflightapp.data.repository.ArticleRepositoryImpl
import com.example.spaceflightapp.domain.repository.ArticleRepository
import org.koin.dsl.module

val dataModule = module {
    single<ArticleRepository> { ArticleRepositoryImpl(get()) }
}