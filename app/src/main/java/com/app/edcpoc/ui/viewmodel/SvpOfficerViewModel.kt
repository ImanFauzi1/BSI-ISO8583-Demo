package com.app.edcpoc.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

class SvpOfficerViewModel : ViewModel() {
    val TAG = "SvpViewModel"
    private val _uiState = MutableStateFlow(SvpUiState())
    val uiState: StateFlow<SvpUiState> = _uiState.asStateFlow()
    var emvUtil: EmvUtil? = null

    fun isoSendMessage(type: String? = null, isoBuilder: ByteArray?) {
        viewModelScope.launch {
            if (isoBuilder == null) {
                LogUtils.e(TAG, "Failed to create ISO message $type")
                return@launch
            }

            Thread.sleep(1000)
            _uiState.update { it.copy(stateType = commandValue, cardNum = cardNum, errorMessage = null) }

            IsoClient.sendMessage(isoBuilder) {response ->
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
                _uiState.update { it.copy(cardNum = cardNum, errorMessage = null)  }

                LogUtils.i(TAG, "ISO $type Successfully sent message.")
            }
        }
    }

    fun clearState() {
        _uiState.value = SvpUiState()
        cardNum = null
    }
}
data class SvpUiState(
    val stateType: String? = null,
    val cardNum: String? = null,
    val errorMessage: String? = null
)