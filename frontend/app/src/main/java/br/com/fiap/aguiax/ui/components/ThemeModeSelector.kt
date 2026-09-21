package br.com.fiap.aguiax.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import br.com.fiap.aguiax.core.settings.ThemeMode

private fun ThemeMode.label() = when (this) { ThemeMode.LIGHT -> "Claro"; ThemeMode.DARK -> "Escuro"; ThemeMode.SYSTEM -> "Sistema" }
private fun ThemeMode.icon() = when (this) {
    ThemeMode.LIGHT -> Icons.Default.LightMode
    ThemeMode.DARK -> Icons.Default.DarkMode
    ThemeMode.SYSTEM -> Icons.Default.SettingsBrightness
}

@Composable
fun ThemeModeSelector(mode: ThemeMode, onModeChange: (ThemeMode) -> Unit, enabled: Boolean = true) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }, enabled = enabled) {
            Icon(mode.icon(), contentDescription = "Tema: ${mode.label()}", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ThemeMode.entries.forEach { option ->
                DropdownMenuItem(text = { Text(option.label()) }, enabled = enabled,
                    modifier = Modifier.semantics { selected = mode == option },
                    leadingIcon = { Icon(option.icon(), contentDescription = null) },
                    trailingIcon = { if (mode == option) Icon(Icons.Default.Check, contentDescription = null) },
                    onClick = { expanded = false; onModeChange(option) })
            }
        }
    }
}
