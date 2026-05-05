package com.canopobd.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.canopobd.data.model.AstraJCodingModels
import com.canopobd.data.model.AstraJCodingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AstraJCodingViewModel : ViewModel() {

    data class CodingState(
        val categories: List<AstraJCodingModels.CodingCategory> = emptyList(),
        val selectedCategory: AstraJCodingModels.CodingCategory? = null,
        val selectedOption: AstraJCodingModels.CodingOption? = null,
        val currentValues: Map<String, String> = emptyMap(),
        val isLoading: Boolean = false,
        val isSaving: Boolean = false,
        val error: String? = null,
        val successMessage: String? = null
    )

    private val _state = MutableStateFlow(CodingState())
    val state: StateFlow<CodingState> = _state.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        _state.value = _state.value.copy(
            categories = AstraJCodingRepository.getAllCategories(),
            isLoading = false
        )
    }

    fun selectCategory(category: AstraJCodingModels.CodingCategory) {
        _state.value = _state.value.copy(
            selectedCategory = category,
            selectedOption = null
        )
    }

    fun selectOption(option: AstraJCodingModels.CodingOption) {
        _state.value = _state.value.copy(
            selectedOption = option
        )
    }

    fun clearSelection() {
        _state.value = _state.value.copy(
            selectedCategory = null,
            selectedOption = null
        )
    }

    fun setCurrentValue(optionId: String, value: String) {
        val newValues = _state.value.currentValues.toMutableMap()
        newValues[optionId] = value
        _state.value = _state.value.copy(
            currentValues = newValues,
            successMessage = null
        )
    }

    fun saveCoding(option: AstraJCodingModels.CodingOption, value: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true)

            try {
                kotlinx.coroutines.delay(500)

                val newValues = _state.value.currentValues.toMutableMap()
                newValues[option.id] = value

                _state.value = _state.value.copy(
                    currentValues = newValues,
                    isSaving = false,
                    successMessage = "${option.displayName} wurde auf ${value} geändert"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSaving = false,
                    error = "Fehler beim Speichern: ${e.message}"
                )
            }
        }
    }

    fun applyProfile(profile: AstraJCodingModels.CodingProfile) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true)

            try {
                kotlinx.coroutines.delay(1000)

                _state.value = _state.value.copy(
                    currentValues = profile.options,
                    isSaving = false,
                    successMessage = "Profil '${profile.name}' wurde angewendet"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSaving = false,
                    error = "Fehler beim Anwenden des Profils: ${e.message}"
                )
            }
        }
    }

    fun loadCurrentValues() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            kotlinx.coroutines.delay(500)
            _state.value = _state.value.copy(isLoading = false)
        }
    }

    fun backupCurrentSettings() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                successMessage = "Backup wurde erstellt"
            )
        }
    }

    fun clearMessages() {
        _state.value = _state.value.copy(
            error = null,
            successMessage = null
        )
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AstraJCodingViewModel() as T
        }
    }
}
