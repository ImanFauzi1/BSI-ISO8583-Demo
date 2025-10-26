package com.app.edcpoc.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.app.edcpoc.utils.Constants.isTimeout
import com.app.edcpoc.utils.Constants.mScanner
import com.app.edcpoc.utils.KtpReaderManager.isMatchedFingerprint
import com.simo.ektp.GlobalVars.fmd
import com.zcs.sdk.util.LogUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SvpViewModelState(
    var isLoading: Boolean = false,
    var errorMessage: String? = null,
    var successMessage: String? = null
)
class SvpViewModel() : ViewModel() {
    private val TAG = "SvpViewModel"
    private val _uiState = MutableStateFlow(SvpViewModelState())
    val uiState: StateFlow<SvpViewModelState> = _uiState.asStateFlow()


    fun handleFingerprintResult(type: String, fmd2: ByteArray? = null) {
        if (isTimeout) {
            LogUtils.e(TAG, "Fingerprint capture timed out")
            mScanner?.setLedStatus(0, 1)
            throw Exception("Fingerprint capture timed out")
        }

        if (type == "MATCH_FINGERPRINT") {
            if (fmd2 == null) {
                LogUtils.e(TAG, "Fingerprint data is null")
                mScanner?.setLedStatus(0, 1)
                throw Exception("Fingerprint data is null")
            }

            val isMatched = isMatchedFingerprint(fmd, fmd2, null)
            if (isMatched) {
                LogUtils.i(TAG, "Fingerprint match successful")
            }
        }

        if (fmd == null) {
            throw Exception("Fingerprint fmd data is null")
        }

        mScanner?.setLedStatus(1, 0)
    }

    fun clearState() {
        _uiState.value = SvpViewModelState()
    }
}