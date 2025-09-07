package com.example.spaceflightapp.core.di

import com.example.spaceflightapp.core.utils.constants.BASE_URL
import com.example.spaceflightapp.data.remote.SpaceflightNewsApi
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

val networkModule = module {
    single { HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY } }
    single { OkHttpClient.Builder().addInterceptor(get<HttpLoggingInterceptor>()).build() }
    single {
        Moshi.Builder()
            .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
            .build()
    }
    single {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(get())
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .build()
    }
    single<SpaceflightNewsApi> { get<Retrofit>().create(SpaceflightNewsApi::class.java) }
}