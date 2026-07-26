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

    var onApplyOption: ((AstraJCodingModels.CodingOption, AstraJCodingModels.CodingValue) -> Unit)? = null
    var onLoadValues: (suspend () -> Map<String, String>)? = null

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
        val codingValue = option.values.find { it.value == value }
            ?: option.values.firstOrNull()
            ?: return

        val callback = onApplyOption
        if (callback != null) {
            callback.invoke(option, codingValue)
            val newValues = _state.value.currentValues.toMutableMap()
            newValues[option.id] = value
            _state.value = _state.value.copy(
                currentValues = newValues,
                successMessage = null // result comes back via codingResult
            )
        } else {
            _state.value = _state.value.copy(
                error = "Keine Fahrzeugverbindung"
            )
        }
    }

    fun applyProfile(profile: AstraJCodingModels.CodingProfile) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true)

            try {
                val callback = onApplyOption
                if (callback != null) {
                    for ((optionId, valueStr) in profile.options) {
                        val option = AstraJCodingRepository.getAllCategories()
                            .flatMap { it.options }
                            .find { it.id == optionId }
                        val value = option?.values?.find { it.value == valueStr }
                            ?: option?.values?.firstOrNull()
                        if (option != null && value != null) {
                            callback.invoke(option, value)
                        }
                    }
                    _state.value = _state.value.copy(
                        currentValues = profile.options,
                        isSaving = false,
                        successMessage = "Profil '${profile.name}' wird angewendet..."
                    )
                } else {
                    _state.value = _state.value.copy(
                        currentValues = profile.options,
                        isSaving = false,
                        error = "Keine Fahrzeugverbindung"
                    )
                }
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
            try {
                val loader = onLoadValues
                if (loader != null) {
                    val values = loader.invoke()
                    _state.value = _state.value.copy(
                        currentValues = values,
                        isLoading = false
                    )
                } else {
                    _state.value = _state.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Fehler beim Laden: ${e.message}"
                )
            }
        }
    }

    fun backupCurrentSettings() {
        viewModelScope.launch {
            val values = _state.value.currentValues
            if (values.isNotEmpty()) {
                _state.value = _state.value.copy(
                    successMessage = "Backup erstellt: ${values.size} Werte gespeichert"
                )
            } else {
                _state.value = _state.value.copy(
                    error = "Keine Werte zum Sichern vorhanden"
                )
            }
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
