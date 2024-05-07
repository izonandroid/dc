package com.izontechnology.dcapp.di

import android.content.Context
import android.content.SharedPreferences
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.preference.PreferenceManager
import com.izontechnology.dcapp.base.common.Network
import com.izontechnology.dcapp.base.common.NetworkConnectivity
import com.izontechnology.dcapp.utils.wifi.WiFiManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

/**
 * The `AppModule` module is responsible for providing application-level dependencies.
 *
 * This module provides three singletons:
 * - `CoroutineContext` instance with IO dispatcher.
 * - `NetworkConnectivity` instance using application context.
 * - `SharedPreferences` instance using default preference manager with application context.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provides a singleton `CoroutineContext` instance with an IO dispatcher.
     * @return instance of CoroutineContext
     */
    @Provides
    @Singleton
    fun provideCoroutineContext(): CoroutineContext {
        return Dispatchers.IO
    }

    /**
     * Provides a singleton `NetworkConnectivity` instance with an application context.
     *
     * @param context
     * @return instance of NetworkConnectivity
     */
    @Provides
    @Singleton
    fun provideNetworkConnectivity(@ApplicationContext context: Context): NetworkConnectivity {
        return Network(context)
    }

    @Provides
    @Singleton
    fun provideTelephonyManager(@ApplicationContext context: Context): TelephonyManager {
        return context.getSystemService(TelephonyManager::class.java)
    }

    @Provides
    @Singleton
    fun provideSubscriptionManager(@ApplicationContext context: Context): SubscriptionManager {
        return context.getSystemService(SubscriptionManager::class.java)
    }

    /**
     * Provides a singleton `SharedPreferences` instance using the default preference manager and application context.
     * @param context
     * @return instance of SharedPreferences
     */
    @Singleton
    @Provides
    fun providesSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    /**  This method provides WiFiManager in app   */
    @Provides
    @Singleton
    fun provideWifiManager(
        @ApplicationContext context: Context,
    ): WiFiManager {
        return WiFiManager(context)
    }
}