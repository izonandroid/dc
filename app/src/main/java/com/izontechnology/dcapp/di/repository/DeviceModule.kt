package com.izontechnology.dcapp.di.repository

import com.izontechnology.dcapp.data.remote_service.DeviceInfoService
import com.izontechnology.dcapp.di.NetworkModule
import com.izontechnology.dcapp.domain.repository.deviceInfo.DeviceInfoRepository
import com.izontechnology.dcapp.domain.repository.deviceInfo.DeviceInfoRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Dagger module that provides dependencies for address management feature.
 * This module includes the [NetworkModule] to provide the [Retrofit] instance.
 */
@Module(includes = [NetworkModule::class])
@InstallIn(SingletonComponent::class)
class DeviceModule {

    /**
     * Provides the [DeviceInfoService] dependency.
     *
     * @param retrofit The [Retrofit] instance used for network operations.
     * @return The [DeviceInfoService] implementation.
     */
    @Singleton
    @Provides
    fun provideDeviceApi(retrofit: Retrofit): DeviceInfoService {
        return retrofit.create(DeviceInfoService::class.java)
    }

    /**
     * Provides the [DeviceInfoRepository] dependency.
     *
     * @param DeviceInfoService The [DeviceInfoService] implementation.
     * @return The [DeviceInfoRepository] implementation.
     */
    @Singleton
    @Provides
    fun provideDeviceRepository(deviceInfoService: DeviceInfoService): DeviceInfoRepository {
        return DeviceInfoRepositoryImpl(deviceInfoService)
    }
}