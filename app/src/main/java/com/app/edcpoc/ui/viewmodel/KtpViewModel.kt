package com.app.edcpoc.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class KtpUiState(
    val isLoading: Boolean = false,
    val ktpData: String? = null,
    val errorMessage: String? = null
)

class KtpViewModel(): ViewModel() {
    val TAG = "KtpViewModel"

    private val _uiState = MutableLiveData(KtpUiState())
    val uiState : MutableLiveData<KtpUiState> = _uiState
}