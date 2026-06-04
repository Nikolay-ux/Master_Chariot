package com.nikolayux.masterchariot.data.di

import android.app.Application
import com.nikolayux.masterchariot.data.local.AppDatabase
import com.nikolayux.masterchariot.feature.car.data.CarRepositoryImpl
import com.nikolayux.masterchariot.feature.car.domain.CarRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideCarRepository(
        application: Application
    ): CarRepository {

        return CarRepositoryImpl(
            AppDatabase
                .getInstance(application)
                .carDao
        )
    }
}