package com.app.edcpoc

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.MediaStore
import android.util.Base64
import android.view.KeyEvent
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.edcpoc.data.model.FaceCompareRequest
import com.app.edcpoc.data.model.KtpDataModel
import com.app.edcpoc.interfaces.EmvUtilInterface
import com.app.edcpoc.ui.components.*
import com.app.edcpoc.ui.theme.EdcpocTheme
import com.app.edcpoc.ui.viewmodel.ApiUiState
import com.app.edcpoc.ui.viewmodel.ApiViewModel
import com.app.edcpoc.ui.viewmodel.ISOViewModel
import com.app.edcpoc.utils.Constants.FACE_COMPARE_SCORE_THRESHOLD
import com.app.edcpoc.utils.Constants.FACE_RECOGNIZE
import com.app.edcpoc.utils.Constants.KTP_READ
import com.app.edcpoc.utils.Constants.MANUAL_KTP_READ
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
import com.app.edcpoc.utils.EktpUtil
import com.app.edcpoc.utils.EktpUtil.setData
import com.app.edcpoc.utils.EktpUtil.updateFingerprintImage
import com.app.edcpoc.utils.KtpReaderManager.createFingerDialog
import com.app.edcpoc.utils.KtpReaderManager.createReadingDialog
import com.app.edcpoc.utils.KtpReaderManager.isMatchedFingerprint
import com.app.edcpoc.utils.KtpReaderManager.payloadRequest
import com.app.edcpoc.utils.LogUtils
import com.app.edcpoc.utils.Utility.clearVar
import com.app.edcpoc.utils.Utility.convertBmpToBase64
import com.app.edcpoc.utils.Utility.readPsamConfigSuccess
import com.google.gson.Gson
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
import kotlinx.coroutines.launch
import java.util.Arrays
import java.util.logging.Level.CONFIG

class MainActivity : ComponentActivity(), EmvUtilInterface {
    companion object {
        var enrollType: String = ""
        private const val CAMERA_REQUEST_CODE = 1001
        private const val IDENTITY_PERMISSION_REQUEST_CODE = 1024
    }
    private val TAG = MainActivity::class.java.simpleName
    private val identityPermissions = arrayOf(Manifest.permission.CAMERA)
    private val isoViewModel: ISOViewModel by viewModels()
    private val apiViewModel: ApiViewModel by viewModels()

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

        handlePermissions()
        checkPsamConfiguration()
        handleInitSdk()

        observeKtpState()

        setContent {
            EdcpocTheme {
                val ktpState by apiViewModel.ktpState.collectAsState()
                val ktpSpvState by apiViewModel.ktpSpvState.collectAsState()
                val faceCompareState by apiViewModel.faceCompareState.collectAsState()
                val logData by apiViewModel.logData.collectAsState()
                val isLoading = ktpState is ApiUiState.Loading ||
                                ktpSpvState is ApiUiState.Loading ||
                                faceCompareState is ApiUiState.Loading ||
                                logData is ApiUiState.Loading
                if (isLoading) {
                    LoadingDialog()
                }
                EDCHomeApp(
                    this@MainActivity,
                    onEnrollmentClick = {onEnrollmentClick(it)},
                    isoViewModel = isoViewModel
                )
            }
        }
    }

    private fun observeKtpState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                apiViewModel.faceCompareState.collect { state ->
                    when (state) {
                        is ApiUiState.Success -> {
                            if (state.data.data.Score >= FACE_COMPARE_SCORE_THRESHOLD) {
                                LogUtils.i(TAG, "Tencent face comparison passed with score: ${state.data.data.Score}")
                                showToast("Success")
                                handleSendLog("Success", "Face Compare Success", state.data.data.Score as Double)
                                getData()
                                apiViewModel.resetFaceCompareState()
                            }
                        }
                        is ApiUiState.Error -> {
                            LogUtils.d(TAG, "Error facing comparation: ${state.message}")
                            Toast.makeText(this@MainActivity, state.message, Toast.LENGTH_SHORT).show()
                            apiViewModel.resetFaceCompareState()
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun handleSendLog(status: String, message: String, similarity: Double? = 0.0) {
        val payload = payloadRequest()

        when(enrollType) {
            KTP_READ -> {
                val updatePayload = payload.copy(fingerprintVerification = status, reason = message, sidikJari = feature64Kanan)
                LogUtils.e(TAG, "Gson Finger Log: ${Gson().toJson(updatePayload)}")
                apiViewModel.sendFingerLogData(updatePayload)
            }
            FACE_RECOGNIZE -> {
                val updatePayload = payload.copy(faceRecognitionVerification = status, reason = message, FaceRecognitionSimilarity = similarity)
                apiViewModel.sendFaceLogData(updatePayload)
            }
            else -> LogUtils.e(TAG, "No log to send for enroll type: $enrollType")
        }
    }

    fun handleInitSdk() {
        EktpUtil.initialize()
        EktpUtil.ektpOpen()
    }

    override fun onDestroy() {
        super.onDestroy()
        EktpSdkZ90.instance().closePsamAndRfidReaders()
    }
    fun onEnrollmentClick(enrollmentType: String) {
        if (enrollmentType == MANUAL_KTP_READ) {
            val intent = Intent(this@MainActivity, ManualEnrollmentActivity::class.java)
            startActivity(intent)
            return
        }

        enrollType = enrollmentType
        indonesianIdentityCard = null
        feature64Kanan = ""
        fmd = null
        clearVar()

        EktpSdkZ90.instance().closePsamAndRfidReaders()

        createReadingDialog(this@MainActivity, "Reading KTP, Please wait...") { _, _ ->
            handleCardReadResult()
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
        val inputLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 16, 32, 16)
        }
        val pcidEditText = EditText(this).apply {
            hint = "PCID"
            setText("2024BB12121100000000000000095067")
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 16 }
        }
        val configEditText = EditText(this).apply {
            hint = "Config"
            setText("E743E49AC5FD1A180D28AB938B1F3F6FC6F008D3BE955670BE598C9E084837F1")
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        inputLayout.addView(pcidEditText)
        inputLayout.addView(configEditText)

        AlertDialog.Builder(this)
            .setTitle("Manual PSAM Input")
            .setView(inputLayout)
            .setMessage("Masukkan PCID dan Config secara manual.")
            .setPositiveButton("OK") { dialog, _ ->
                PCID = pcidEditText.text.toString()
                CONFIG_FILE = configEditText.text.toString()

                PreferenceManager.setPCID(this@MainActivity,PCID)
                PreferenceManager.setConfigFile(this@MainActivity, CONFIG_FILE)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .show()
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            handleCameraResult(data)
        }
    }

    private fun handleCameraResult(data: Intent?) {
        val photo = data?.extras?.get("data") as? Bitmap

        if (photo != null) {
            val photoBase64 = convertBmpToBase64(photo, Base64.NO_WRAP)
            LogUtils.d(TAG, "Photo captured and converted to base64")

            if (photoBase64 == null) {
                showToast("Failed to convert bitmap to base64")
                return
            }

            performTencentFaceComparison(photoBase64)
        } else {
            showToast("Failed to capture image")
        }
    }

    private fun performTencentFaceComparison(photoBase64: String) {
        val photoBytes = StringUtils.convertHexToBytes(VALUE_FOTO)
        val face64 = Base64.encodeToString(photoBytes, Base64.NO_WRAP)

        val body = FaceCompareRequest(
            ImageA = photoBase64,
            ImageB = face64
        )

        apiViewModel.faceCompare(body)
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
                face64 = base64Image(VALUE_FOTO)
            }

            when (enrollType) {
                KTP_READ -> {
                    startFingerprintCapture()
                }
                FACE_RECOGNIZE -> {
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
            type = enrollType,
            agama = VALUE_AGAMA ?: "",
            alamat = VALUE_ALAMAT ?: "",
            desa = "",
            error = false,
            foto = convertBitmap(VALUE_FOTO, 0, null),
            golonganDarah = VALUE_GOL_DARAH ?: "",
            jenisKelamin = VALUE_JNS_KELAMIN ?: "",
            kecamatan = VALUE_KEC ?: "",
            kelurahan = VALUE_KEL ?: "",
            kewarganegaraan = VALUE_NATIONALITY ?: "",
            kodePos = "",
            kota = VALUE_KAB ?: "",
            masaBerlaku = "SEUMUR HIDUP",
            message = "",
            nama = VALUE_NAMA ?: "",
            nik = VALUE_NIK ?: "",
            pekerjaan = VALUE_PEKERJAAN ?: "",
            provinsi = VALUE_PROV ?: "",
            rt = VALUE_RT ?: "",
            rw = VALUE_RW ?: "",
            sidikJariBytes = if (fmd != null) Base64.encodeToString(fmd, Base64.NO_WRAP) else "",
            sidikJari = base64Finger,
            statusPerkawinan = VALUE_STATUS ?: "",
            tandaTangan = sign64,
            tanggalLahir = VALUE_TGL_LAHIR ?: "",
            tempatLahir = VALUE_TMP_LAHIR ?: ""
        )
        val intent = Intent(this, KtpPreviewActivity::class.java)
        intent.putExtra("ktpData", data)
        startActivity(intent)
        // finish() DIHAPUS agar MainActivity tetap di back stack
    }

    private fun convertBitmap(photo: String?, offset: Int = 0, length: Int? = null): String? {
        if (photo != null) {
            val bytes = StringUtils.convertHexToBytes(photo)
            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            LogUtils.d(TAG, "convertBitmap: offset=$offset, length=$length, base64Size=${base64.length}, base64=${base64.substring(offset, length?.let { offset + it } ?: base64.length)}")
            return base64
        }
        return null
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

    private fun handlePermissions() {
        val requestPerms = getUnGrantedPermissions()
        if (requestPerms.isNotEmpty()) {
            requestPermissions(requestPerms.toTypedArray(), IDENTITY_PERMISSION_REQUEST_CODE)
        }
    }

    private fun getUnGrantedPermissions(): MutableList<String> {
        val requestPerms = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (permission in identityPermissions) {
                if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(permission)) {
                    requestPerms.add(permission)
                }
            }
        }
        return requestPerms
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
