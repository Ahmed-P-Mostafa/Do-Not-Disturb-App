package com.polotika.dndapp

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {

    @Provides
    fun providesContext(@ApplicationContext context: Context) = context

    @Provides
    fun providesPreferencesServices(context: Context): PreferencesServices {
        return PreferencesServices(context)
    }

    @Provides
    fun provideCoroutineDispatcher(): Dispatchers {
        return Dispatchers
    }

}