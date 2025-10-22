package com.app.edcpoc.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.edcpoc.PreferenceManager
import com.app.edcpoc.data.model.*
import com.app.edcpoc.data.repository.SessionRepository
import com.app.edcpoc.utils.Constants.cardNum
import com.app.edcpoc.utils.Constants.commandValue
import com.app.edcpoc.utils.EmvUtil
import com.app.edcpoc.utils.LogUtils
import com.idpay.victoriapoc.utils.IsoManagement.IsoClient
import com.idpay.victoriapoc.utils.IsoManagement.IsoUtils.parseIsoResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SessionViewModel : ViewModel() {
    private val TAG = "SessionViewModel"
    private val sessionRepository = SessionRepository()
    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()
    
    fun startDate(cardNum: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            val success = sessionRepository.startDate(cardNum)
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isDailySessionActive = success,
                errorMessage = if (!success) "Failed to start daily session" else null
            )
            
            updateSessionInfo()
        }
    }
    
    fun closeDate(cardNum: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            val success = sessionRepository.closeDate(cardNum)
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isDailySessionActive = !success,
                errorMessage = if (!success) "Failed to close daily session" else null
            )
            
            updateSessionInfo()
        }
    }
    
    fun logon(userId: String, username: String, role: UserRole) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            val success = sessionRepository.logon(userId, username, role)
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isUserLoggedIn = success,
                errorMessage = if (!success) "Failed to logon" else null
            )
            
            updateSessionInfo()
        }
    }
    
    fun logoff() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            val success = sessionRepository.logoff()
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isUserLoggedIn = !success,
                errorMessage = if (!success) "Failed to logoff" else null
            )
            
            updateSessionInfo()
        }
    }

    fun updateSessionInfo() {
        _uiState.value = _uiState.value.copy(
            dailySession = sessionRepository.getCurrentDailySession(),
            userSession = sessionRepository.getCurrentUserSession(),
            isDailySessionActive = sessionRepository.isDailySessionActive(),
            isUserLoggedIn = sessionRepository.isUserLoggedIn()
        )
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class SessionUiState(
    val isLoading: Boolean = false,
    val isDailySessionActive: Boolean = false,
    val isUserLoggedIn: Boolean = false,
    val dailySession: DailySession? = null,
    val userSession: Session? = null,
    val errorMessage: String? = null
)
