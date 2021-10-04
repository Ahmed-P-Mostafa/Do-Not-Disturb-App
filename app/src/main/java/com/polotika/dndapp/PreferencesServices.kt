package com.polotika.dndapp

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
private val Context.dataStore :DataStore<Preferences> by preferencesDataStore("data_store")

class PreferencesServices constructor(private val context: Context) {


    private val timeKey = longPreferencesKey("TIME_KEY")

    val getTime : Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[timeKey]?:-1
    }

    suspend fun setTime(value:Long){
        context.dataStore.edit { preferences ->
            preferences[timeKey] = value
        }
    }


}