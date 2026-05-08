package com.nikolayux.masterchariot.feature.connect.di

import android.content.Context
import com.nikolayux.masterchariot.data.bluetooth.BluetoothService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object BluetoothModule {
    @Provides
    @Singleton
    fun provideBluetoothService(@ApplicationContext context: Context): BluetoothService {
        return BluetoothService(context)
    }
}