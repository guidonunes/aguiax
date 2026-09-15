package br.com.fiap.aguiaexpress.core.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

// A single delegate/DataStore instance for the application context, independent of login/session.
private val Context.themeDataStore by preferencesDataStore(
    name = "appearance",
    corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() }
)

internal class ThemePreferences(private val store: DataStore<Preferences>) {
    constructor(context: Context) : this(context.applicationContext.themeDataStore)

    private val themeKey = stringPreferencesKey("theme_mode")
    val mode = store.data.map { ThemeMode.fromStored(it[themeKey]) }

    suspend fun setMode(mode: ThemeMode) { store.edit { it[themeKey] = mode.name } }
}
