package com.app.edcpoc.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ManualEnrollmentUiState {
    object Idle : ManualEnrollmentUiState()
    object Loading : ManualEnrollmentUiState()
    data class OnSubmit(val nik: String, val name: String) : ManualEnrollmentUiState()
    data class Success(val message: String = "Success") : ManualEnrollmentUiState()
    data class Error(val message: String) : ManualEnrollmentUiState()
}

class ManualEnrollmentViewModel : ViewModel() {
    private val TAG = "ManualEnrollmentViewModel"
    private val _uiState = MutableStateFlow<ManualEnrollmentUiState>(ManualEnrollmentUiState.Idle)
    val uiState: StateFlow<ManualEnrollmentUiState> = _uiState

    private val _fingerprintBitmap = MutableStateFlow<Bitmap?>(null)
    val fingerprintBitmap: StateFlow<Bitmap?> = _fingerprintBitmap

    fun setFingerprintBitmap(bitmap: Bitmap?) {
        _fingerprintBitmap.value = bitmap
    }

    fun reset() {
        _uiState.value = ManualEnrollmentUiState.Idle
        _fingerprintBitmap.value = null
    }

    fun setErrorState(message: String) {
        _uiState.value = ManualEnrollmentUiState.Error(message)
    }

    fun setLoadingState() {
        _uiState.value = ManualEnrollmentUiState.Loading
    }

    fun setSuccessState(message: String) {
        _uiState.value = ManualEnrollmentUiState.Success(message)
    }
}
