package com.innugo.files.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "innugo_prefs")

class PreferencesManager private constructor(context: Context) {

    private val dataStore = context.applicationContext.dataStore

    companion object {
        val KEY_PINNED_FOLDERS = stringSetPreferencesKey("pinned_folders")
        val KEY_VIEW_MODE = stringPreferencesKey("view_mode")
        val KEY_PENDING_CLEANUP = stringPreferencesKey("pending_cleanup")

        @Volatile
        private var INSTANCE: PreferencesManager? = null

        fun getInstance(context: Context): PreferencesManager =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: PreferencesManager(context.applicationContext).also { INSTANCE = it }
            }
    }

    val pinnedFolders: Flow<Set<String>> = dataStore.data.map { prefs ->
        prefs[KEY_PINNED_FOLDERS] ?: emptySet()
    }

    val viewMode: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_VIEW_MODE] ?: "list"
    }

    val pendingCleanup: Flow<String?> = dataStore.data.map { prefs ->
        prefs[KEY_PENDING_CLEANUP]
    }

    suspend fun togglePinnedFolder(path: String) {
        dataStore.edit { prefs ->
            val current = prefs[KEY_PINNED_FOLDERS] ?: emptySet()
            prefs[KEY_PINNED_FOLDERS] = if (path in current) current - path else current + path
        }
    }

    suspend fun setViewMode(mode: String) {
        dataStore.edit { prefs -> prefs[KEY_VIEW_MODE] = mode }
    }

    suspend fun setPendingCleanup(path: String?) {
        dataStore.edit { prefs ->
            if (path == null) prefs.remove(KEY_PENDING_CLEANUP)
            else prefs[KEY_PENDING_CLEANUP] = path
        }
    }
}
