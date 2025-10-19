package com.app.edcpoc.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.edcpoc.data.model.*
import com.app.edcpoc.data.repository.AuthRepository
import com.app.edcpoc.interfaces.EmvUtilInterface
import com.app.edcpoc.utils.Constants
import com.app.edcpoc.utils.Constants.commandValue
import com.app.edcpoc.utils.DialogUtil.createEmvDialog
import com.app.edcpoc.utils.EmvUtil
import com.app.edcpoc.utils.LogUtils
import com.idpay.victoriapoc.utils.IsoManagement.IsoClient
import com.idpay.victoriapoc.utils.IsoManagement.IsoUtils.isoChangePIN
import com.idpay.victoriapoc.utils.IsoManagement.IsoUtils.isoCreatePIN
import com.idpay.victoriapoc.utils.IsoManagement.IsoUtils.isoLogonLogoff
import com.idpay.victoriapoc.utils.IsoManagement.IsoUtils.isoStartEndDate
import com.idpay.victoriapoc.utils.IsoManagement.IsoUtils.isoVerificationPIN
import com.zcs.sdk.util.StringUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val TAG = "AuthViewModel"
    private val authRepository = AuthRepository()
    lateinit var emvUtil: EmvUtil
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // StateFlow for Compose dialog events
    private val _dialogState = MutableStateFlow(DialogState())
    val dialogState: StateFlow<DialogState> = _dialogState.asStateFlow()

    fun initialize(context: Context, emvUtilInterface: EmvUtilInterface) {
        // Use applicationContext to avoid Compose crash
        emvUtil = EmvUtil(context.applicationContext)
        emvUtil.initialize()
        emvUtil.emvOpen()
        emvUtil.setCallback(emvUtilInterface)
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

    private fun parseIsoResponse(iso: String): Map<String, String> {
        var idx = 0
        fun take(n: Int): String {
            val s = iso.substring(idx, (idx + n).coerceAtMost(iso.length))
            idx += n
            return s
        }
        return mapOf(
            "MTI" to take(4),
            "Bitmap" to take(16),
            "ProcessingCode" to take(6),
            "STI" to take(6),
            "F12" to take(6),
            "F13" to take(4),
            "F24" to take(3),
            "F37" to take(12),
            "F38" to take(6),
            "F39" to take(2),
            "F41" to take(8)
        )
    }

    fun isoSendMessage(type: String? = null, isoBuilder: ByteArray?) {
        viewModelScope.launch {
            if (isoBuilder == null) {
                LogUtils.e("AuthViewModel", "Failed to create ISO message $type")
                return@launch
            }

            IsoClient.sendMessage(isoBuilder) {response ->
                LogUtils.i("AuthViewModel", "ISO $type Response: $response")

                if (response == null) {
                    LogUtils.e("AuthViewModel", "Failed to receive ISO $type response")
                    _dialogState.update { it.copy(showDialog = true, dialogMessage = "Gagal kirim ISO $type") }
                    return@sendMessage
                }

                val isoString = response["string"] as? String ?: ""
                val fields = parseIsoResponse(isoString)
                val message = when (commandValue) {
                    "logon" -> "Sukses logon"
                    "logoff" -> "Sukses logoff"
                    else -> "ISO sukses dikirim"
                }
                val detail = fields.entries.joinToString("\n") { "${it.key}: ${it.value}" }
                _dialogState.update { it.copy(showDialog = true, dialogMessage = "$message\n$detail") }

                LogUtils.i(TAG, "ISO $type Successfully sent message.")
            }
        }
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
