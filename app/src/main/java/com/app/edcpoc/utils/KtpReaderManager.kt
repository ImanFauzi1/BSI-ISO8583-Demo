package com.app.edcpoc.utils

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Handler
import android.os.Looper
import android.util.Base64
import com.app.edcpoc.data.model.KtpReq
import com.app.edcpoc.utils.Constants.FINGERPRINT_MATCH_THRESHOLD
import com.app.edcpoc.utils.Constants.face64
import com.app.edcpoc.utils.Constants.indonesianIdentityCard
import com.app.edcpoc.utils.Constants.isTimeout
import com.app.edcpoc.utils.Constants.mScanner
import com.app.edcpoc.utils.Constants.sign64
import com.app.edcpoc.utils.EktpUtil.searchBankCard
import com.app.edcpoc.utils.EktpUtil.setData
import com.app.edcpoc.utils.EktpUtil.updateFingerprintImage
import com.app.edcpoc.utils.fingerprint.FingerPrintTask
import com.simo.ektp.EktpSdkZ90
import com.simo.ektp.GlobalVars.VALUE_AGAMA
import com.simo.ektp.GlobalVars.VALUE_ALAMAT
import com.simo.ektp.GlobalVars.VALUE_GOL_DARAH
import com.simo.ektp.GlobalVars.VALUE_JNS_KELAMIN
import com.simo.ektp.GlobalVars.VALUE_KAB
import com.simo.ektp.GlobalVars.VALUE_KEC
import com.simo.ektp.GlobalVars.VALUE_KEL
import com.simo.ektp.GlobalVars.VALUE_NAMA
import com.simo.ektp.GlobalVars.VALUE_NATIONALITY
import com.simo.ektp.GlobalVars.VALUE_NIK
import com.simo.ektp.GlobalVars.VALUE_PEKERJAAN
import com.simo.ektp.GlobalVars.VALUE_PROV
import com.simo.ektp.GlobalVars.VALUE_RT
import com.simo.ektp.GlobalVars.VALUE_RW
import com.simo.ektp.GlobalVars.VALUE_STATUS
import com.simo.ektp.GlobalVars.VALUE_TGL_LAHIR
import com.simo.ektp.GlobalVars.VALUE_TMP_LAHIR
import com.simo.ektp.GlobalVars.fmd
import com.zcs.sdk.card.CardReaderTypeEnum
import com.zcs.sdk.util.StringUtils

object KtpReaderManager {
    private val TAG = "KtpReaderManager"
    private var fingerprintAttempt: Int = 0
    private var ktpAttempt: Int = 0

    fun createFingerDialog(context: Context, message: String? = "Please place your finger on the scanner.", fingerprintResult: (dialog: ProgressDialog?, dialog1: DialogInterface?) -> Unit?) {
        DialogUtil.createDialog(
            context = context,
            title = "Capture...",
            message = message,
            showListener = { dialog, dialog1 ->
                LogUtils.d(TAG, "createFingerDialog: showListener")
                Thread {
                    fmd = FingerPrintTask.instance.captureFinger(dialog)
                    LogUtils.d(TAG, "Fingerprint captured: ${StringUtils.convertBytesToHex(fmd)}")
                    dialog1.cancel()
                }.start()
            },
            cancelListener = { dialog, dialog1 ->
                fingerprintResult(dialog, dialog1)
            },
        ).show()
    }

    /**
     * Enhanced fingerprint verification with attempt counter
     */
    fun verifyFingerprintWithAttempts(
        context: Context,
        fingerprintSvp: ByteArray,
        onSuccess: () -> Unit,
        onDeviceBlocked: () -> Unit
    ) {
        showFingerprintDialogWithCallback(context) {
            handleFingerprintResult(context, fingerprintSvp, onSuccess, onDeviceBlocked)
        }
    }

    /**
     * Enhanced KTP reading with attempt counter
     */
    fun readKtpWithAttempts(
        context: Context,
        expectedNik: String,
        onSuccess: () -> Unit,
        onDeviceBlocked: () -> Unit,
        onKtpVerified: () -> Unit
    ) {
        showKtpReadingDialogWithCallback(context) {
            handleKtpReadResult(context, expectedNik, onSuccess, onDeviceBlocked, onKtpVerified)
        }
    }

    /**
     * Reset all attempt counters
     */
    fun resetAttempts() {
        fingerprintAttempt = 0
        ktpAttempt = 0
        LogUtils.d(TAG, "All attempt counters reset")
    }

    private fun showFingerprintDialogWithCallback(context: Context, onFingerprintCaptured: () -> Unit) {
        createFingerDialog(context, "Please place your finger on the scanner") { _, _ ->
            onFingerprintCaptured()
        }
    }

    private fun showKtpReadingDialogWithCallback(context: Context, onKtpRead: () -> Unit) {
        createReadingDialog(context, "Reading KTP, Please wait...") { _, _ ->
            onKtpRead()
        }
    }

    private fun handleFingerprintResult(
        context: Context,
        fingerprintSvp: ByteArray,
        onSuccess: () -> Unit,
        onDeviceBlocked: () -> Unit
    ) {
        LogUtils.d(TAG, "handleFingerprintResult called")
        LogUtils.d(TAG, "fingerprintSvp size: ${fingerprintSvp.size}")
        LogUtils.d(TAG, "fingerprintSvp hex: ${StringUtils.convertBytesToHex(fingerprintSvp)}")
        LogUtils.d(TAG, "isTimeout: $isTimeout")

        when {
            isTimeout -> {
                LogUtils.w(TAG, "Fingerprint capture timeout")
                // Don't increment attempt for timeout
                showFailureDialog(
                    context,
                    "Timeout",
                    "Capture fingerprint timeout",
                    onRetry = {
                        showFingerprintDialogWithCallback(context) {
                            handleFingerprintResult(context, fingerprintSvp, onSuccess, onDeviceBlocked)
                        }
                    }
                )
            }
            isMatchedFingerprint2(fingerprintSvp) -> {
                fingerprintAttempt = 0 // Reset on success
                LogUtils.i(TAG, "Fingerprint match successful")
                setFingerprintMatchSuccess()
                onSuccess()
            }
            else -> {
                fingerprintAttempt++
                LogUtils.w(TAG, "Fingerprint match failed, attempt: $fingerprintAttempt")

                if (fingerprintAttempt >= 3) {
                    LogUtils.e(TAG, "Maximum fingerprint attempts reached. Device blocked.")
                    showDeviceBlockedDialog(context, "fingerprint") {
                        onDeviceBlocked()
                    }
                } else {
                    setFingerprintMatchFailed()
                    val remainingAttempts = 3 - fingerprintAttempt

                    showFailureDialog(
                        context,
                        "Verifikasi Gagal",
                        "Fingerprint tidak cocok.\nSisa percobaan: $remainingAttempts\n\nMenyiapkan percobaan berikutnya..."
                    ) {
                        showFingerprintDialogWithCallback(context) {
                            handleFingerprintResult(context, fingerprintSvp, onSuccess, onDeviceBlocked)
                        }
                    }
                }
            }
        }
    }

    private fun handleKtpReadResult(
        context: Context,
        expectedNik: String,
        onSuccess: () -> Unit,
        onDeviceBlocked: () -> Unit,
        onKtpVerified: () -> Unit
    ) {
        if (indonesianIdentityCard == null || indonesianIdentityCard?.id.isNullOrEmpty()) {
            ktpAttempt++
            LogUtils.w(TAG, "KTP read failed, attempt: $ktpAttempt")

            EktpSdkZ90.instance().closePsamAndRfidReaders()

            if (ktpAttempt >= 3) {
                LogUtils.e(TAG, "Maximum KTP read attempts reached. Device blocked.")
                showDeviceBlockedDialog(context, "KTP") {
                    onDeviceBlocked()
                }
            } else {
                val remainingAttempts = 3 - ktpAttempt
                val logMsg = if (isTimeout) "Read card timeout" else "Read card failed or canceled"

                showFailureDialog(
                    context,
                    "Pembacaan KTP Gagal",
                    "$logMsg\nSisa percobaan: $remainingAttempts\n\nMenyiapkan percobaan berikutnya..."
                ) {
                    showKtpReadingDialogWithCallback(context) {
                        handleKtpReadResult(context, expectedNik, onSuccess, onDeviceBlocked, onKtpVerified)
                    }
                }
            }
        } else {
            LogUtils.i(TAG, "KTP read successful")

            indonesianIdentityCard?.let {
                setData(it)
            }

            if (VALUE_NIK == expectedNik) {
                ktpAttempt = 0 // Reset on success
                LogUtils.i(TAG, "KTP NIK verification successful")
                onKtpVerified()
            } else {
                ktpAttempt++
                LogUtils.w(TAG, "KTP NIK verification failed, attempt: $ktpAttempt")

                if (ktpAttempt >= 3) {
                    LogUtils.e(TAG, "Maximum KTP verification attempts reached. Device blocked.")
                    showDeviceBlockedDialog(context, "KTP verification") {
                        onDeviceBlocked()
                    }
                } else {
                    val remainingAttempts = 3 - ktpAttempt

                    showFailureDialog(
                        context,
                        "Verifikasi NIK Gagal",
                        "NIK pada KTP tidak sesuai.\nSisa percobaan: $remainingAttempts\n\nMenyiapkan percobaan berikutnya..."
                    ) {
                        showKtpReadingDialogWithCallback(context) {
                            handleKtpReadResult(context, expectedNik, onSuccess, onDeviceBlocked, onKtpVerified)
                        }
                    }
                }
            }
        }
    }

    fun isMatchedFingerprint(fmd1: ByteArray?, fmd2: ByteArray?, fmd3: ByteArray?): Boolean {
        var matchScore = 0
        matchScore = FingerPrintTask.instance.matchFingerprint(fmd1!!, fmd2!!)

        if (matchScore == 0 && fmd3 != null) {
            matchScore = FingerPrintTask.instance.matchFingerprint(fmd1, fmd3!!)
        }

        return matchScore >= FINGERPRINT_MATCH_THRESHOLD
    }
    private fun isMatchedFingerprint2(fingerprintSvp: ByteArray): Boolean {
        return try {
            // Ensure both fingerprint data are valid
            if (fmd == null || fmd.isEmpty()) {
                LogUtils.e(TAG, "fmd is null or empty")
                return false
            }

            if (fingerprintSvp.isEmpty()) {
                LogUtils.e(TAG, "fingerprintSvp is empty")
                return false
            }

            // Log fingerprint data for debugging
            LogUtils.d(TAG, "fmd size: ${fmd.size}, fingerprintSvp size: ${fingerprintSvp.size}")
            LogUtils.d(TAG, "fmd hex: ${StringUtils.convertBytesToHex(fmd)}")
            LogUtils.d(TAG, "fingerprintSvp hex: ${StringUtils.convertBytesToHex(fingerprintSvp)}")

            // Perform fingerprint matching
            val matchScore = FingerPrintTask.instance.matchFingerprint(fmd, fingerprintSvp)
            LogUtils.d(TAG, "Match score: $matchScore, Threshold: $FINGERPRINT_MATCH_THRESHOLD")

            val isMatch = matchScore >= FINGERPRINT_MATCH_THRESHOLD
            LogUtils.d(TAG, "isMatchedFingerprint result: $isMatch")

            isMatch
        } catch (e: Exception) {
            LogUtils.e(TAG, "Error in fingerprint matching: ${e.message}")
            false
        }
    }

    private fun setFingerprintMatchSuccess() {
        mScanner?.setLedStatus(1, 0) // Green LED
        updateFingerprintImage(FingerPrintTask.fi)
    }

    private fun setFingerprintMatchFailed() {
        mScanner?.setLedStatus(1, 0) // Green LED
    }

    private fun showFailureDialog(
        context: Context,
        title: String,
        message: String,
        onRetry: () -> Unit
    ) {
        val failureDialog = ProgressDialog(context).apply {
            setTitle(title)
            setMessage(message)
            isIndeterminate = true
            setCancelable(false)
        }
        failureDialog.show()

        // Delay 2 detik sebelum retry
        Handler(Looper.getMainLooper()).postDelayed({
            failureDialog.dismiss()
            onRetry()
        }, 2000)
    }

    private fun showDeviceBlockedDialog(
        context: Context,
        attemptType: String,
        onConfirm: () -> Unit
    ) {
        AlertDialog.Builder(context)
            .setTitle("Peringatan!")
            .setMessage("Sudah melakukan 3x percobaan $attemptType gagal. Device terblokir.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                onConfirm()
            }
            .setCancelable(false)
            .show()
    }

    fun createReadingDialog(context: Context, message: String? = "Reading Card, Please wait ...", handleCardResult: (dialog: ProgressDialog, dialog1: DialogInterface) -> Unit) {
        Utility.createDialog(
            context = context,
            title = "Reading...",
            message = message,
            showListener = { dialog, _ ->
                Thread {
                    searchBankCard(CardReaderTypeEnum.RF_CARD, dialog)
                }.start()
            },
            cancelListener = {dialog, dialog1 ->
                handleCardResult(dialog, dialog1)
            }
        ).show()
    }

    fun payloadRequest(): KtpReq {
        return KtpReq(
            statusPerkawinan = VALUE_STATUS,
            pekerjaan = VALUE_PEKERJAAN,
            tandaTangan = sign64,
            sidikJari = if(fmd != null) Base64.encodeToString(fmd, Base64.NO_WRAP) else "",
            provinsi = VALUE_PROV,
            tempatLahir = VALUE_TMP_LAHIR,
            jenisKelamin = VALUE_JNS_KELAMIN,
            rt = VALUE_RT,
            rw = VALUE_RW,
            kecamatan = VALUE_KEC,
            kota = VALUE_KAB,
            tanggalLahir = VALUE_TGL_LAHIR,
            kelurahan = VALUE_KEL,
            nama = VALUE_NAMA,
            agama = VALUE_AGAMA,
            nik = VALUE_NIK,
            kodePos = "",
            kewarganegaraan = VALUE_NATIONALITY,
            golonganDarah = VALUE_GOL_DARAH,
            masaBerlaku = "",
            alamat = VALUE_ALAMAT,
            foto = face64,
            desa = ""
        )
    }
}