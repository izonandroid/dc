package com.izontechnology.dcapp.di

import android.content.Context
import com.izontechnology.dcapp.base.common.SharedPrefs
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

/**
 * This is a Dagger module used for providing an instance of SharedPrefs class.
 * Injected as a singleton component.
 */
@Module
@InstallIn(SingletonComponent::class)
object SharedPrefModule {

    /**
     * Provides an instance of SharedPrefs class using the [ApplicationContext] as dependency.
     *
     * @param context The application context used to instantiate SharedPrefs.
     * @return An instance of SharedPrefs using the provided context.
     */
    @Provides
    fun provideSharedPref(@ApplicationContext context: Context) : SharedPrefs {
        return SharedPrefs(context)
    }
}