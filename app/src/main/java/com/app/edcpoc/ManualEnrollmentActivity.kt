package com.app.edcpoc

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.app.edcpoc.data.model.KtpReq
import com.app.edcpoc.ui.screens.ManualEnrollmentScreen
import com.app.edcpoc.ui.viewmodel.ApiUiState
import com.app.edcpoc.ui.viewmodel.ApiViewModel
import com.app.edcpoc.utils.Constants.base64Finger
import com.app.edcpoc.utils.Constants.feature64Kanan
import com.app.edcpoc.utils.Constants.isTimeout
import com.app.edcpoc.utils.Constants.mScanner
import com.app.edcpoc.utils.EktpUtil.updateFingerprintImage
import com.app.edcpoc.utils.KtpReaderManager.createFingerDialog
import com.app.edcpoc.utils.LogUtils
import com.app.edcpoc.utils.fingerprint.FingerPrintTask
import com.app.edcpoc.ui.viewmodel.ManualEnrollmentViewModel
import com.simo.ektp.GlobalVars.fmd
import kotlinx.coroutines.launch

class ManualEnrollmentActivity : ComponentActivity() {
    private val TAG = "ManualEnrollmentActivity"
    private val viewModel: ManualEnrollmentViewModel by viewModels()
    private val apiViewModel: ApiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ManualEnrollmentScreen(
                        onBack = { finish() },
                        onScanFingerprint = { startFingerprintCapture() },
                        onSubmit = { nik, name ->
                            submitManualEnrollment(nik, name)
                        }
                    )
                }
            }
        }
        observeKtpState()
    }

    private fun startFingerprintCapture() {
        LogUtils.i(TAG, "Fingerprint position tips: ")
        createFingerDialog(this, "Letakkan jari anda di pembaca sidik jari...") { _, _ ->
            handleFingerprintResult()
        }
    }

    private fun handleFingerprintResult() {
        if (isTimeout) {
            viewModel.reset()
            LogUtils.e("ManualEnrollmentActivity", "Fingerprint capture timed out")
            mScanner?.setLedStatus(0, 1)
            Toast.makeText(this@ManualEnrollmentActivity, "Fingerprint capture timed out", Toast.LENGTH_SHORT).show()
        } else {
            LogUtils.d("ManualEnrollmentActivity", "FEATURE64KANAN: $feature64Kanan")
            LogUtils.i("ManualEnrollmentActivity", "Fingerprint match successful")
            mScanner?.setLedStatus(1, 0)
            feature64Kanan = base64Finger.toString()
            updateFingerprintImage(FingerPrintTask.fi)

            val imageBytes = Base64.decode(base64Finger, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            viewModel.setFingerprintBitmap(bitmap)
        }
    }

    private fun submitManualEnrollment(nik: String, name: String) {
        if (nik.isBlank() || name.isBlank() || fmd == null) {
            viewModel.setErrorState("Lengkapi data dan scan fingerprint!")
            return
        }
        viewModel.setErrorState("")
        viewModel.setLoadingState()

        val body = KtpReq(
            nik = nik,
            nama = name,
            sidikJari = Base64.encodeToString(fmd, Base64.NO_WRAP)
        )
        apiViewModel.sendKtpDataSpv(body)
    }

    private fun observeKtpState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                apiViewModel.ktpSpvState.collect { state ->
                    when (state) {
                        is ApiUiState.Success -> {
                            Toast.makeText(this@ManualEnrollmentActivity, "Sukses submit KTP", Toast.LENGTH_SHORT).show()
                            // Matikan loading dengan setSuccessState
                            viewModel.setSuccessState("Sukses submit KTP")
                        }
                        is ApiUiState.Error -> {
                            LogUtils.d(TAG, "Error submitting KTP: ${state.message}")
                            Toast.makeText(this@ManualEnrollmentActivity, state.message, Toast.LENGTH_SHORT).show()
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}
