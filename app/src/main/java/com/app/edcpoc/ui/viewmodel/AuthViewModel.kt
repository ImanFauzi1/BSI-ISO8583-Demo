package com.app.edcpoc.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.edcpoc.data.model.*
import com.app.edcpoc.data.repository.AuthRepository
import com.app.edcpoc.interfaces.EmvUtilInterface
import com.app.edcpoc.utils.EmvUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel(), EmvUtilInterface {
    
    private val authRepository = AuthRepository()

    private lateinit var emvUtil: EmvUtil
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun initialize(context: Context) {
        emvUtil = EmvUtil(context)
        emvUtil.initialize()
        emvUtil.emvOpen()

        emvUtil.setCallback(this)
//        dialogUtils = ShowDialogUtils(emvUtil)
//        dialogUtils.initialize()
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            val response = authRepository.login(username, password)
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isLoggedIn = response.success,
                currentUser = response.user,
                errorMessage = if (!response.success) response.message else null
            )
        }
    }
    
    fun logout() {
        authRepository.logout()
        _uiState.value = AuthUiState()
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun hasPermission(action: String): Boolean {
        val user = _uiState.value.currentUser ?: return false
        return authRepository.hasPermission(user, action)
    }

    override fun onDoSomething() {
        TODO("Not yet implemented")
    }

    override fun onError(message: String) {
        TODO("Not yet implemented")
    }
}

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val currentUser: User? = null,
    val errorMessage: String? = null
)
