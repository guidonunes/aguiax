package br.com.fiap.aguiaexpress.core.settings

enum class ThemeMode {
    SYSTEM, LIGHT, DARK;

    fun usesDarkTheme(systemDark: Boolean): Boolean = when (this) {
        SYSTEM -> systemDark
        LIGHT -> false
        DARK -> true
    }

    companion object {
        val DEFAULT = DARK
        fun fromStored(value: String?): ThemeMode = entries.firstOrNull { it.name == value } ?: DEFAULT
    }
}
