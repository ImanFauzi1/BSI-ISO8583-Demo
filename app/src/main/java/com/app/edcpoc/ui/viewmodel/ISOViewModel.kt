package com.app.edcpoc.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.edcpoc.PreferenceManager
import com.app.edcpoc.utils.Constants.cardNum
import com.app.edcpoc.utils.EmvUtil
import com.app.edcpoc.utils.LogUtils
import com.idpay.victoriapoc.utils.IsoManagement.IsoClient
import com.zcs.sdk.util.StringUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ISOViewModel : ViewModel() {
    val TAG = "ISOViewModel"
    private val _uiState = MutableStateFlow(SvpUiState())
    val uiState: StateFlow<SvpUiState> = _uiState.asStateFlow()
    var emvUtil: EmvUtil? = null
    private val _dialogState = MutableStateFlow(DialogState())
    val dialogState: StateFlow<DialogState> = _dialogState.asStateFlow()

    fun isoSendMessage(
        context: Context,
        type: String? = null,
        isoBuilder: String,
        callback: (String?, String?) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) } // Set loading true
            if (isoBuilder == null) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Failed to send ISO, please contact developer.",
                        isIsoHandled = true,
                        isLoading = false,
                        logIso = it.logIso + "Gagal membuat ISO message $type"
                    )
                }
                LogUtils.e(TAG, "Failed to create ISO message $type")
                return@launch
            }

            Thread.sleep(1000)

            try {
                val socket = withContext(Dispatchers.IO) {
                    IsoClient.connectSocket(isoBuilder)
                }
                if (isAllHex(socket!!)) {
                    try {
                        if (socket.length < (4 + 10 + 4 + 16)) {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    isIsoHandled = true,
                                    logIso = it.logIso + "Response hex: $socket\n"
                                )
                            }
                            LogUtils.e(
                                TAG,
                                "Hex data length is too short to be a valid ISO8583 message"
                            )
                        } else {
                            callback(socket, null)
                            _uiState.update { it.copy(isLoading = false, isIsoHandled = true) }
//                            _uiState.update { it.copy(isLoading = false, isIsoHandled = true, logIso = it.logIso + "Response ISO: $socket\nASCII: $toAscii") }
                        }
                    } catch (e: Exception) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isIsoHandled = true,
                                logIso = it.logIso + "Response message: ${e.message}\nresponse: $socket\n\nexception: ${e.stackTraceToString()}"
                            )
                        }
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            errorMessage = "Gagal kirim ISO $type: $socket",
                            isIsoHandled = true,
                            isLoading = false,
                            logIso = it.logIso + "Response Error: $socket"
                        )
                    }
                    LogUtils.e(TAG, "ISO $type Error: $socket")
                }
//                _uiState.update { it.copy(isLoading = false, isIsoHandled = true, logIso = it.logIso + "Response $socket") }
            } catch (e: Exception) {
                LogUtils.e(TAG, "ISO $type Exception: ${e.message}")
                _uiState.update {
                    it.copy(
                        errorMessage = "Gagal kirim ISO $type: ${e.stackTraceToString()}\n\n message=${e.message}",
                        isIsoHandled = true,
                        isLoading = false,
                        logIso = it.logIso + "ISO Exception: ${e.stackTraceToString()}\n]\nmessage=${e.message}"
                    )
                }
                return@launch
            }
        }
    }

    private fun isAllHex(str: String): Boolean {
        return str.matches(Regex("[0-9A-Fa-f]+"))
    }

    fun setLoading(isLoading: Boolean) {
        _uiState.update { it.copy(isLoading = isLoading) }
    }

    fun setIsVisibleStartDate(isVisibleStartDate: Boolean) {
        _uiState.update { it.copy(isLoading = isVisibleStartDate) }
    }

    fun clearState() {
        _uiState.value = SvpUiState()
        cardNum = null
    }

    fun dismissDialog() {
        _dialogState.update { it.copy(showDialog = false, dialogMessage = null) }
    }
}

data class DialogState(
    val showDialog: Boolean = false,
    val dialogMessage: String? = null
)

data class SvpUiState(
    val isLoading: Boolean = false,
    val stateType: String? = null,
    val isIsoHandled: Boolean = false,
    val iso: String? = null,
    val cardNum: String? = null,
    val errorMessage: String? = null,
    val logIso: List<String> = emptyList(),
    val isVisibleStartDate: Boolean = false
)