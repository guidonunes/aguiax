package br.com.fiap.aguiaexpress.core.settings

import org.junit.Assert.*
import org.junit.Test

class ThemeModeTest {
    @Test
    fun `explicit light and dark override both system appearances`() {
        listOf(false, true).forEach { systemDark ->
            assertFalse(ThemeMode.LIGHT.usesDarkTheme(systemDark))
            assertTrue(ThemeMode.DARK.usesDarkTheme(systemDark))
            assertEquals(systemDark, ThemeMode.SYSTEM.usesDarkTheme(systemDark))
        }
    }

    @Test
    fun `persisted modes round trip and absent or unknown settings follow system`() {
        ThemeMode.entries.forEach { assertEquals(it, ThemeMode.fromStored(it.name)) }
        listOf(null, "", "unknown").forEach { assertEquals(ThemeMode.SYSTEM, ThemeMode.fromStored(it)) }
    }
}
