package com.app.edcpoc.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.edcpoc.data.model.*
import com.app.edcpoc.data.repository.AuthRepository
import com.app.edcpoc.interfaces.EmvUtilInterface
import com.app.edcpoc.utils.Constants.commandValue
import com.app.edcpoc.utils.EmvUtil
import com.app.edcpoc.utils.LogUtils
import com.idpay.victoriapoc.utils.IsoManagement.IsoClient
import com.idpay.victoriapoc.utils.IsoManagement.IsoUtils.isoStartEndDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel(), EmvUtilInterface {
    private val TAG = "AuthViewModel"
    private val authRepository = AuthRepository()
    lateinit var emvUtil: EmvUtil
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // StateFlow for Compose dialog events
    private val _dialogState = MutableStateFlow(DialogState())
    val dialogState: StateFlow<DialogState> = _dialogState.asStateFlow()

    fun initialize(context: Context) {
        // Use applicationContext to avoid Compose crash
        emvUtil = EmvUtil(context.applicationContext)
        emvUtil.initialize()
        emvUtil.emvOpen()
        emvUtil.setCallback(this)
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

    fun isoStartCloseDate() {
        viewModelScope.launch {
            val isoDateBuilder = isoStartEndDate()

            if(isoDateBuilder == null) {
                LogUtils.e("AuthViewModel", "Failed to create ISO Start/End Date message")
                return@launch
            }

            // Log payload ByteArray as HEX (for backend debug)
            LogUtils.i("AuthViewModel", "ISO Payload (HEX): ${isoDateBuilder.joinToString("") { "%02X".format(it) }}")
            // Jika ingin log tanpa 2-byte length:
            LogUtils.i("AuthViewModel", "ISO Payload (HEX, no length): ${isoDateBuilder.drop(2).joinToString("") { "%02X".format(it) }}")

            IsoClient.sendMessage(isoDateBuilder) {response ->
                LogUtils.i("AuthViewModel", "ISO Start/End Date Response: $response")

                if (response == null) {
                    LogUtils.e("AuthViewModel", "Failed to receive ISO Start/End Date response")
                    return@sendMessage
                }

                LogUtils.i(TAG, "ISO Successfully sent Start/End Date message.")
            }
        }
    }

    // EmvUtilInterface implementation
    override fun onDoSomething() {
        // Example: trigger Compose dialog
        when(commandValue) {
            "startDate", "closeDate" -> {
                isoStartCloseDate()
            }
        }
        LogUtils.i("AuthViewModel", "commandValue=$commandValue")
    }

    override fun onError(message: String) {
        _dialogState.update { it.copy(showDialog = true, dialogMessage = message) }
    }

    fun dismissDialog() {
        _dialogState.update { it.copy(showDialog = false, dialogMessage = null) }
    }
}

// State for Compose dialog events
data class DialogState(
    val showDialog: Boolean = false,
    val dialogMessage: String? = null
)

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val currentUser: User? = null,
    val errorMessage: String? = null
)
