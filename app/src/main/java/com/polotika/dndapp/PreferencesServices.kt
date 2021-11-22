package com.polotika.dndapp

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("data_store")

class PreferencesServices constructor(private val context: Context) {


    private val timeKey = longPreferencesKey("TIME_KEY")
    private val autoStartKey = booleanPreferencesKey("AUTO_START_KEY")

    val getTime: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[timeKey] ?: -1
    }
    val isAutoStartEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[autoStartKey] ?: false
    }

    suspend fun setTime(value: Long) {
        context.dataStore.edit { preferences ->
            preferences[timeKey] = value
        }
    }

    suspend fun setAutoStartEnabled() {
        context.dataStore.edit {
            it[autoStartKey] = true
        }
    }


}