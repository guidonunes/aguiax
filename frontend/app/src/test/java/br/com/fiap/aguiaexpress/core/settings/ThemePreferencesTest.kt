package br.com.fiap.aguiaexpress.core.settings

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class ThemePreferencesTest {
    @get:Rule val temporary = TemporaryFolder()

    @Test
    fun `theme survives closing and reopening the preferences store`() = runBlocking {
        val file = File(temporary.root, "appearance.preferences_pb")
        ThemeMode.entries.forEach { expected ->
            val job = SupervisorJob()
            val store = PreferenceDataStoreFactory.create(scope = CoroutineScope(job + Dispatchers.IO), produceFile = { file })
            val preferences = ThemePreferences(store)
            preferences.setMode(expected)
            assertEquals(expected, preferences.mode.first())
            job.cancelAndJoin()

            val reopenedJob = SupervisorJob()
            val reopened = PreferenceDataStoreFactory.create(scope = CoroutineScope(reopenedJob + Dispatchers.IO), produceFile = { file })
            try { assertEquals(expected, ThemePreferences(reopened).mode.first()) }
            finally { reopenedJob.cancelAndJoin() }
        }
    }

    @Test
    fun `empty and unknown stored mode safely default to dark`() = runBlocking {
        val job = SupervisorJob()
        val store = PreferenceDataStoreFactory.create(scope = CoroutineScope(job + Dispatchers.IO),
            produceFile = { File(temporary.root, "appearance.preferences_pb") })
        try {
            val preferences = ThemePreferences(store)
            assertEquals(ThemeMode.DARK, preferences.mode.first())
            store.edit { it[stringPreferencesKey("theme_mode")] = "future-mode" }
            assertEquals(ThemeMode.DARK, preferences.mode.first())
        } finally { job.cancelAndJoin() }
    }
}
