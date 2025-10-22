package com.app.edcpoc

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.MediaStore
import android.util.Base64
import android.view.KeyEvent
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.edcpoc.data.model.KtpDataModel
import com.app.edcpoc.interfaces.EmvUtilInterface
import com.app.edcpoc.ui.components.*
import com.app.edcpoc.ui.theme.EdcpocTheme
import com.app.edcpoc.ui.viewmodel.ISOViewModel
import com.app.edcpoc.utils.Constants
import com.app.edcpoc.utils.Constants.base64Finger
import com.app.edcpoc.utils.Constants.commandValue
import com.app.edcpoc.utils.Constants.face64
import com.app.edcpoc.utils.Constants.feature64Kanan
import com.app.edcpoc.utils.Constants.indonesianIdentityCard
import com.app.edcpoc.utils.Constants.isTimeout
import com.app.edcpoc.utils.Constants.mScanner
import com.app.edcpoc.utils.Constants.psamProfile
import com.app.edcpoc.utils.Constants.sign64
import com.app.edcpoc.utils.Constants.signBites
import com.app.edcpoc.utils.Constants.signatureBitmap
import com.app.edcpoc.utils.CoreUtils.initializeEmvUtil
import com.app.edcpoc.utils.DialogUtil.createEmvDialog
import com.app.edcpoc.utils.EktpUtil.setData
import com.app.edcpoc.utils.EktpUtil.updateFingerprintImage
import com.app.edcpoc.utils.KtpReaderManager.createFingerDialog
import com.app.edcpoc.utils.KtpReaderManager.createReadingDialog
import com.app.edcpoc.utils.KtpReaderManager.isMatchedFingerprint
import com.app.edcpoc.utils.LogUtils
import com.app.edcpoc.utils.Utility.clearVar
import com.app.edcpoc.utils.Utility.convertBmpToBase64
import com.app.edcpoc.utils.Utility.readPsamConfigSuccess
import com.idpay.victoriapoc.utils.IsoManagement.IsoUtils
import com.idpay.victoriapoc.utils.fingerprint.FingerPrintTask
import com.simo.ektp.EktpSdkZ90
import com.simo.ektp.GlobalVars.CONFIG_FILE
import com.simo.ektp.GlobalVars.PCID
import com.simo.ektp.GlobalVars.VALUE_AGAMA
import com.simo.ektp.GlobalVars.VALUE_ALAMAT
import com.simo.ektp.GlobalVars.VALUE_FOTO
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
import com.simo.ektp.GlobalVars.VALUE_SIGNATURE
import com.simo.ektp.GlobalVars.VALUE_STATUS
import com.simo.ektp.GlobalVars.VALUE_TGL_LAHIR
import com.simo.ektp.GlobalVars.VALUE_TMP_LAHIR
import com.simo.ektp.GlobalVars.fmd
import com.simo.ektp.GlobalVars.mHits
import com.simo.ektp.Utils.getFingerPosTips
import com.simo.ektp.Utils.getSignBitmap
import com.zcs.sdk.util.StringUtils
import java.util.Arrays

class MainActivity : AppCompatActivity(), EmvUtilInterface {
    companion object {
        var enrollType: String = ""
        val ENROLLMENT_EKTP = "ektp_reader"
        val ENROLLMENT_EMPLOYEE = "employee_enrollment"
        val ENROLLMENT_TENCENT = "tencent_enrollment"
        private const val CAMERA_REQUEST_CODE = 1001
    }
    private val TAG = MainActivity::class.java.simpleName
    private val isoViewModel: ISOViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (PreferenceManager.getOfficerCardNum(this) == null) {
            startActivity(Intent(this, OfficerActivity::class.java))
            finish()
            return
        }
        if (PreferenceManager.getSvpCardNum(this) == null) {
            startActivity(Intent(this, SvpActivity::class.java))
            finish()
            return
        }
        enableEdgeToEdge()

        isoViewModel.emvUtil = initializeEmvUtil(this@MainActivity, this)

        checkPsamConfiguration()

        setContent {
            var showKtpDialog by remember { mutableStateOf(false) }
            var ktpData by remember { mutableStateOf<KtpDataModel?>(null) }
            val context = LocalContext.current
            EdcpocTheme {
                if (showKtpDialog && ktpData != null) {
                    KtpDataDialog(
                        data = ktpData!!,
                        onClose = { showKtpDialog = false },
                        onSubmit = {
                            // TODO: handle submit action
                            showKtpDialog = false
                        }
                    )
                }
                EDCHomeApp(
                    this@MainActivity,
                    onEnrollmentClick = {onEnrollmentClick(it)},
                    isoViewModel = isoViewModel
                )
            }
            // Helper to trigger dialog from getData()
            LaunchedEffect(Unit) {
                DialogTriggerHelper.dialogTrigger = { data ->
                    ktpData = data
                    showKtpDialog = true
                }
            }
        }
    }

    fun onEnrollmentClick(enrollmentType: String) {
        when(enrollmentType) {
            "ktp_reader" -> {startEnrollment(enrollmentType)}
        }
    }

    private fun checkPsamConfiguration() {
        if (!readPsamConfigSuccess(this)) {
            showPsamConfigurationDialog()
        }
    }

    private fun showPsamConfigurationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Reading $psamProfile Failed")
            .setMessage("Reading \"$psamProfile\" failed. \n\n Please put $psamProfile in SD card root directory and import it.")
            .setIcon(R.drawable.ic_dialog_alert_yellow)
            .setOnKeyListener { _, keyCode, keyEvent ->
                handlePsamDialogKeyPress(keyCode, keyEvent)
            }
            .setPositiveButton("Import") { _, _ ->
                showManualPsamInputDialog()
            }
            .setNegativeButton("Exit") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun handlePsamDialogKeyPress(keyCode: Int, keyEvent: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_6 && keyEvent.action == KeyEvent.ACTION_DOWN) {
            System.arraycopy(mHits, 1, mHits, 0, mHits.size - 1)
            mHits[mHits.size - 1] = SystemClock.uptimeMillis()
            LogUtils.d(TAG, "System time: ${SystemClock.uptimeMillis()}")
            if (mHits[0] >= SystemClock.uptimeMillis() - 5000) {
                Arrays.fill(mHits, 0)
            }
        }
        return false
    }

    @SuppressLint("NewApi")
    private fun showManualPsamInputDialog() {
        // Ubah: jangan panggil setContent ulang, gunakan AlertDialog biasa atau state Compose utama
        AlertDialog.Builder(this)
            .setTitle("Manual PSAM Input")
            .setMessage("Masukkan PCID dan Config secara manual.")
            .setPositiveButton("OK") { dialog, _ ->
                // TODO: tampilkan input dialog Compose lewat state di setContent utama jika ingin
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun startEnrollment(type: String) {
        enrollType = type
        clearVar()
        indonesianIdentityCard = null
        fmd = null
        feature64Kanan = ""
        EktpSdkZ90.instance().closePsamAndRfidReaders()

        createReadingDialog(this@MainActivity, "Reading KTP, Please wait...") { _, _ ->
            handleCardReadResult()
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    private fun handleCardReadResult() {
        if (indonesianIdentityCard == null || indonesianIdentityCard?.id.isNullOrEmpty()) {
            EktpSdkZ90.instance().closePsamAndRfidReaders()
            showToast("Read Card Failed or Canceled")
        } else {
            LogUtils.i(TAG, "Card read successful")
            showToast("Read Card Success")

            indonesianIdentityCard?.let {
                setData(it)

                if (!VALUE_SIGNATURE.isNullOrEmpty()) {
                    signBites = StringUtils.convertHexToBytes(VALUE_SIGNATURE)
                    signatureBitmap = getSignBitmap(signBites, 168, 44)

                    signatureBitmap?.let { bitmap ->
                        sign64 = convertBmpToBase64(bitmap, Base64.NO_WRAP) ?: ""
                    }
                }
//                sign64 = base64Image(VALUE_SIGNATURE)
                face64 = base64Image(VALUE_FOTO)
            }

            when (enrollType) {
                ENROLLMENT_EKTP, ENROLLMENT_EMPLOYEE -> {
                    startFingerprintCapture()
                }
                ENROLLMENT_TENCENT -> {
                    openCamera()
                }
                else -> LogUtils.e(TAG, "Unknown enrollment type: $enrollType")
            }
        }
    }

    private fun startFingerprintCapture() {
        val message = getFingerPosTips(
            indonesianIdentityCard!!.fingerPositionFirst,
            indonesianIdentityCard!!.fingerPositionSecond
        )
        LogUtils.i(TAG, "Fingerprint position tips: $message")

        createFingerDialog(this@MainActivity, message) {_, _ ->
            indonesianIdentityCard?.let { setData(it) }
            handleFingerprintResult()
        }
    }

    private fun handleFingerprintResult() {
        when {
            isTimeout -> {
                showToast("Capture Fingerprint timeout")
            }
            isMatchedFingerprint(fmd, indonesianIdentityCard?.rightFinger, indonesianIdentityCard?.leftFinger) -> {
                LogUtils.d(TAG, "FEATURE64KANAN: $feature64Kanan")
                LogUtils.i(TAG, "Fingerprint match successful")
                setFingerPrintMatchPass()
                feature64Kanan = base64Finger.toString()
            }
            else -> {
                LogUtils.w(TAG, "Fingerprint match failed")
                setFingerPrintMatchFailed()
            }
        }
    }

    private fun setFingerPrintMatchPass() {
        mScanner?.setLedStatus(1, 0)
        updateFingerprintImage(FingerPrintTask.fi)
        indonesianIdentityCard?.let { setData(it) }
        getData()
    }

    private fun getData() {
        val data = KtpDataModel(
            agama = VALUE_AGAMA ?: "",
            alamat = VALUE_ALAMAT ?: "",
            desa = "",
            error = false,
            foto = VALUE_FOTO ?: "",
            golonganDarah = VALUE_GOL_DARAH ?: "",
            jenisKelamin = VALUE_JNS_KELAMIN ?: "",
            kecamatan = VALUE_KEC ?: "",
            kelurahan = VALUE_KEL ?: "",
            kewarganegaraan = VALUE_NATIONALITY ?: "",
            kodePos = "",
            kota = VALUE_KAB ?: "",
            masaBerlaku = "",
            message = "",
            nama = VALUE_NAMA ?: "",
            nik = VALUE_NIK ?: "",
            pekerjaan = VALUE_PEKERJAAN ?: "",
            provinsi = VALUE_PROV ?: "",
            rt = VALUE_RT ?: "",
            rw = VALUE_RW ?: "",
            sidikJari = "",
            statusPerkawinan = VALUE_STATUS ?: "",
            tandaTangan = "",
            tanggalLahir = VALUE_TGL_LAHIR ?: "",
            tempatLahir = VALUE_TMP_LAHIR ?: ""
        )
        DialogTriggerHelper.dialogTrigger?.invoke(data)
    }

    private fun setFingerPrintMatchFailed() {
        mScanner?.setLedStatus(1, 0)
        showToast("Finger Print Match Failed")
        Handler(Looper.getMainLooper()).post {
            // Additional UI updates if needed
        }
    }

    private fun base64Image(image: String?): String {
        if (image == null) return ""
        val imageBytes = StringUtils.convertHexToBytes(image)
        return Base64.encodeToString(imageBytes, Base64.NO_WRAP)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }


    // EmvUtilInterface implementation
    override fun onDoSomething(context: Context) {
        when(commandValue) {
             "logoff" -> {
                 val officerCardNum = PreferenceManager.getOfficerCardNum(context)
                 if (officerCardNum == null) {
                     LogUtils.e(TAG, "SVP Card Number is null")
                     Toast.makeText(context, "SVP Card Number not found.", Toast.LENGTH_LONG).show()
                     return
                 }
                val iso = IsoUtils.generateIsoLogonLogoff("0800", "820000", officerCardNum)
                isoViewModel.isoSendMessage(commandValue, StringUtils.convertHexToBytes(iso))
            }
            "createPIN" -> {
                val svpCardNumber = PreferenceManager.getSvpCardNum(context)
                if (svpCardNumber == null) {
                    LogUtils.e(TAG, "SVP Card Number is null")
                    Toast.makeText(context, "SVP Card Number not found.", Toast.LENGTH_LONG).show()
                    return
                }
                val iso = IsoUtils.generateIsoCreatePIN(svpCardNumber)
                isoViewModel.isoSendMessage(commandValue, StringUtils.convertHexToBytes(iso))
            }
            "reissuePIN" -> {
                val svpCardNumber = PreferenceManager.getSvpCardNum(context)
                if (svpCardNumber == null) {
                    LogUtils.e(TAG, "SVP Card Number is null")
                    Toast.makeText(context, "SVP Card Number not found.", Toast.LENGTH_LONG).show()
                    return
                }

                val iso = IsoUtils.generateIsoReissuePIN(svpCardNumber)
                isoViewModel.isoSendMessage(commandValue, StringUtils.convertHexToBytes(iso))
            }
            "verifyPIN" -> {
                val iso = IsoUtils.generateIsoVerificationPIN()
                isoViewModel.isoSendMessage(commandValue, StringUtils.convertHexToBytes(iso))
            }
            "changePIN" -> {
                val iso = IsoUtils.generateIsoChangePIN()
                isoViewModel.isoSendMessage(commandValue, StringUtils.convertHexToBytes(iso))
            }
        }
        LogUtils.i("AuthViewModel", "commandValue=$commandValue")
    }

    override fun onError(message: String) {
        LogUtils.e("EmvUtilInterface", "Error: $message")
    }
}

@Composable
fun EDCHomeApp(
    context: Context,
    onEnrollmentClick: (String) -> Unit,
    isoViewModel: ISOViewModel = viewModel(),

    ) {
    var currentScreen by remember { mutableStateOf("home") }
    val authState by isoViewModel.uiState.collectAsState()

    LaunchedEffect(authState) {
        if (commandValue == "logoff") {
            PreferenceManager.setOfficerLoggedIn(context, null)
            context.startActivity(Intent(context, OfficerActivity::class.java))
            return@LaunchedEffect
        }
    }

    when (currentScreen) {
        "home" -> EDCHomeScreen(
            onTransaksiClick = { isoViewModel.emvUtil?.let { createEmvDialog(context, it)} },
            onEnrollmentClick = { onEnrollmentClick(it) },
            onManajemenPINClick = { isoViewModel.emvUtil?.let { createEmvDialog(context, it)} },
            onSessionManagementClick = { isoViewModel.emvUtil?.let { createEmvDialog(context, it)} },
            onLogoutClick = {
                commandValue = "logoff"
                isoViewModel.emvUtil?.let { createEmvDialog(context, it) }
            },
            dialogState = isoViewModel.dialogState.collectAsState().value,
            dismissDialog = { isoViewModel.dismissDialog() }
        )
    }
}

object DialogTriggerHelper {
    var dialogTrigger: ((KtpDataModel) -> Unit)? = null
}
