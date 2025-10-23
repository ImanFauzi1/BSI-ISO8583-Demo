package com.app.edcpoc.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.edcpoc.data.repository.ApiRepository
import com.app.edcpoc.data.model.FaceCompareRequest
import com.app.edcpoc.data.model.FaceCompareTencentResponse
import com.app.edcpoc.data.model.KtpReq
import com.app.edcpoc.data.model.KtpResp
import com.app.edcpoc.data.model.LogResponse
import com.app.edcpoc.utils.LogUtils
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ApiUiState<out T> {
    object Idle : ApiUiState<Nothing>()
    object Loading : ApiUiState<Nothing>()
    data class Success<T>(val data: T) : ApiUiState<T>()
    data class Error(val message: String) : ApiUiState<Nothing>()
}

class ApiViewModel(
    private val repository: ApiRepository = ApiRepository()
) : ViewModel() {
    private val _ktpState = MutableStateFlow<ApiUiState<KtpResp>>(ApiUiState.Idle)
    val ktpState: StateFlow<ApiUiState<KtpResp>> = _ktpState

    private val _ktpSpvState = MutableStateFlow<ApiUiState<KtpResp>>(ApiUiState.Idle)
    val ktpSpvState: StateFlow<ApiUiState<KtpResp>> = _ktpSpvState

    private val _faceCompareState = MutableStateFlow<ApiUiState<FaceCompareTencentResponse>>(ApiUiState.Idle)
    val faceCompareState: StateFlow<ApiUiState<FaceCompareTencentResponse>> = _faceCompareState

    private val _logData = MutableStateFlow<ApiUiState<LogResponse>>(ApiUiState.Idle)
    val logData: StateFlow<ApiUiState<LogResponse>> = _logData

    fun sendKtpData(param: KtpReq) {
        _ktpState.value = ApiUiState.Loading
        viewModelScope.launch {
            val result = repository.sendKtpData(param)
            _ktpState.value = result.fold(
                onSuccess = { ApiUiState.Success(it) },
                onFailure = {
                    LogUtils.d("ApiViewModel", "Error sending KTP data: ${Gson().toJson(it)}")
                    ApiUiState.Error(it.message ?: "Unknown error")
                }
            )
        }
    }

    fun sendKtpDataSpv(param: KtpReq) {
        _ktpSpvState.value = ApiUiState.Loading
        viewModelScope.launch {
            val result = repository.sendKtpDataSpv(param)
            _ktpSpvState.value = result.fold(
                onSuccess = { ApiUiState.Success(it) },
                onFailure = { ApiUiState.Error(it.message ?: "Unknown error") }
            )
        }
    }

    fun faceCompare(param: FaceCompareRequest) {
        _faceCompareState.value = ApiUiState.Loading
        viewModelScope.launch {
            val result = repository.faceCompare(param)
            _faceCompareState.value = result.fold(
                onSuccess = { ApiUiState.Success(it) },
                onFailure = { ApiUiState.Error(it.message ?: "Unknown error") }
            )
        }
    }

    fun sendFingerLogData(param: KtpReq) {
        _logData.value = ApiUiState.Loading
        viewModelScope.launch {
            val result = repository.sendFingerLogData(param)
            _logData.value = result.fold(
                onSuccess = { ApiUiState.Success(it) },
                onFailure = { ApiUiState.Error(it.message ?: "Unknown error") }
            )
        }
    }

    fun sendFaceLogData(param: KtpReq) {
        _logData.value = ApiUiState.Loading
        viewModelScope.launch {
            val result = repository.sendFaceCompareLogData(param)
            _logData.value = result.fold(
                onSuccess = { ApiUiState.Success(it) },
                onFailure = { ApiUiState.Error(it.message ?: "Unknown error") }
            )
        }
    }

    fun resetFaceCompareState() {
        _faceCompareState.value = ApiUiState.Idle
    }
}
