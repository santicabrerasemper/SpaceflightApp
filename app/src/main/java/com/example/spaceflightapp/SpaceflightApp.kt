package com.example.spaceflightapp

import android.app.Application
import com.example.spaceflightapp.core.di.dataModule
import com.example.spaceflightapp.core.di.domainModule
import com.example.spaceflightapp.core.di.networkModule
import com.example.spaceflightapp.core.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class SpaceflightApp: Application() {
    override  fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@SpaceflightApp)
            modules(
                networkModule,
                dataModule,
                domainModule,
                viewModelModule)
        }
    }
}