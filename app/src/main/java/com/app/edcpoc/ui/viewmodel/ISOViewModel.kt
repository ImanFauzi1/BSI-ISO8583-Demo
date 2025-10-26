package com.app.edcpoc.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.edcpoc.utils.Constants.cardNum
import com.app.edcpoc.utils.Constants.commandValue
import com.app.edcpoc.utils.EmvUtil
import com.app.edcpoc.utils.LogUtils
import com.idpay.victoriapoc.utils.IsoManagement.IsoClient
import com.idpay.victoriapoc.utils.IsoManagement.IsoUtils.parseIsoResponse
import com.zcs.sdk.util.StringUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ISOViewModel : ViewModel() {
    val TAG = "ISOViewModel"
    private val _uiState = MutableStateFlow(SvpUiState())
    val uiState: StateFlow<SvpUiState> = _uiState.asStateFlow()
    var emvUtil: EmvUtil? = null
    private val _dialogState = MutableStateFlow(DialogState())
    val dialogState: StateFlow<DialogState> = _dialogState.asStateFlow()

    fun isoSendMessage(type: String? = null, isoBuilder: ByteArray?) {
        viewModelScope.launch {
            if (isoBuilder == null) {
                _uiState.update { it.copy(errorMessage = "Failed to send ISO, please contact developer.", isIsoHandled = true) }
                LogUtils.e(TAG, "Failed to create ISO message $type")
                return@launch
            }

            Thread.sleep(1000)
            _uiState.update { it.copy(stateType = commandValue, cardNum = cardNum, iso = StringUtils.convertBytesToHex(isoBuilder), errorMessage = null) }

            IsoClient.sendMessage(isoBuilder) { response, error ->
                if (error != null) {
                    LogUtils.e(TAG, "ISO $type Error: $error")
                    _uiState.update { it.copy(errorMessage = "Gagal kirim ISO $type: $error", isIsoHandled = true) }
                    return@sendMessage
                }

                LogUtils.i(TAG, "ISO $type Response: $response")

                if (response == null) {
                    LogUtils.e(TAG, "Failed to receive ISO $type response")
                    _uiState.update { it.copy(errorMessage = "Gagal kirim ISO $type") }
                    return@sendMessage
                }

                val isoString = response["string"] as? String ?: ""
                val fields = parseIsoResponse(isoString)
                val message = when (commandValue) {
                    "logon" -> "Sukses logon"
                    "logoff" -> "Sukses logoff"
                    "startDate" -> "Sukses mulai tanggal"
                    "closeDate" -> "Sukses tutup tanggal"
                    else -> "ISO sukses dikirim"
                }
                val detail = fields.entries.joinToString("\n") { "${it.key}: ${it.value}" }
                _uiState.update { it.copy(cardNum = cardNum, iso = detail, errorMessage = null, isIsoHandled = true)  }

                LogUtils.i(TAG, "ISO $type Successfully sent message.")
            }
        }
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
    val cardNum: String? = null,
    val errorMessage: String? = null,
    val iso: String? = null,
    val isIsoHandled: Boolean = false
)