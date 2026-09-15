package br.com.fiap.aguiaexpress.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import br.com.fiap.aguiaexpress.core.settings.ThemeMode
import br.com.fiap.aguiaexpress.core.settings.ThemePreferences
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

internal data class ThemeUiState(val mode: ThemeMode? = null, val isSaving: Boolean = false, val errorMessage: String? = null)

internal class ThemeViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = ThemePreferences(application)
    private val _state = MutableStateFlow(ThemeUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                preferences.mode.collect { mode -> _state.value = _state.value.copy(mode = mode) }
            } catch (error: IOException) {
                _state.value = _state.value.copy(mode = ThemeMode.SYSTEM,
                    errorMessage = "Não foi possível carregar a preferência de tema.")
            }
        }
    }

    fun selectMode(mode: ThemeMode) {
        if (_state.value.isSaving || _state.value.mode == mode) return
        _state.value = _state.value.copy(isSaving = true, errorMessage = null)
        viewModelScope.launch {
            try {
                preferences.setMode(mode)
                _state.value = _state.value.copy(mode = mode, isSaving = false)
            } catch (error: CancellationException) {
                throw error
            } catch (error: IOException) {
                _state.value = _state.value.copy(isSaving = false,
                    errorMessage = "Não foi possível salvar o tema. Tente novamente.")
            }
        }
    }

    fun dismissError() { _state.value = _state.value.copy(errorMessage = null) }
}
