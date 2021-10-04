package com.polotika.dndapp

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {

    @Provides
    fun providesContext(@ApplicationContext context: Context) = context

    @Provides
    fun providesPreferencesServices(context: Context):PreferencesServices{
        return PreferencesServices(context)
    }

}