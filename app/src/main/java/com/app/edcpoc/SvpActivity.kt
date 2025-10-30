package com.app.edcpoc

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.app.edcpoc.ui.theme.EdcpocTheme
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.app.edcpoc.data.model.SvpRequestBody
import com.app.edcpoc.interfaces.EmvUtilInterface
import com.app.edcpoc.ui.components.LoadingDialog
import com.app.edcpoc.ui.screens.SvpLockedScreen
import com.app.edcpoc.ui.viewmodel.ApiUiState
import com.app.edcpoc.ui.viewmodel.ApiViewModel
import com.app.edcpoc.ui.viewmodel.ISOViewModel
import com.app.edcpoc.ui.viewmodel.SpvViewModel
import com.app.edcpoc.utils.Constants.FINGERPRINT_MESSAGE
import com.app.edcpoc.utils.Constants.START_DATE
import com.app.edcpoc.utils.Constants.cardNum
import com.app.edcpoc.utils.Constants.commandValue
import com.app.edcpoc.utils.Constants.feature64Kanan
import com.app.edcpoc.utils.Constants.pinBlockOwn
import com.app.edcpoc.utils.Constants.statusRet
import com.app.edcpoc.utils.Constants.step
import com.app.edcpoc.utils.Constants.track2data
import com.app.edcpoc.utils.CoreUtils.initializeEmvUtil
import com.app.edcpoc.utils.DialogUtil.createEmvDialog
import com.app.edcpoc.utils.EktpUtil
import com.app.edcpoc.utils.IsoManager.ISO8583
import com.app.edcpoc.utils.IsoManager.IsoHelper
import com.app.edcpoc.utils.KtpReaderManager.createFingerDialog
import com.app.edcpoc.utils.LogUtils
import com.app.edcpoc.utils.IsoManager.IsoUtils.generateIsoStartEndDate
import com.google.gson.Gson
import com.simo.ektp.GlobalVars.fmd
import com.zcs.sdk.util.StringUtils
import kotlinx.coroutines.launch
import kotlin.getValue

class SvpActivity : ComponentActivity(), EmvUtilInterface {
    private val TAG = "SvpActivity"
    private val ISOViewModel: ISOViewModel by viewModels()
    private val spvViewModel: SpvViewModel by viewModels()
    private val apiViewModel: ApiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (PreferenceManager.getSvpCardNum(this) != null) {
            startActivity(Intent(this, OfficerActivity::class.java))
            finish()
            return
        }

        ISOViewModel.emvUtil = initializeEmvUtil(this@SvpActivity, this)

        handleInitSdk()
        observeState()

        setContent {
            EdcpocTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val svpState by apiViewModel.svpData.collectAsState()
                    val isLoading = svpState is ApiUiState.Loading

                    if (isLoading) {
                        LoadingDialog()
                    }

                    SvpLockedScreen(
                        ISOViewModel = ISOViewModel,
                        onActivate = {
                            val result = IsoHelper.buildOpenConnectionWithModel()
                            if (result != null) {
                                val (packed, iso) = result

                                ISOViewModel.isoSendMessage(
                                    this@SvpActivity,
                                    commandValue,
                                    packed
                                ) { success, error ->
//                                    ISOViewModel.setIsVisibleStartDate(true)
                                    if (error != null) {
                                        Toast.makeText(
                                            this@SvpActivity,
                                            error,
                                            Toast.LENGTH_LONG
                                        )
                                            .show()
                                        return@isoSendMessage
                                    }
                                    try {
                                        val unpack = ISO8583.unpackFromHex(success!!, iso)
                                        val getRc =
                                            StringUtils.convertHexToASCII(unpack.get("39"))
                                        val get48 = unpack["48"] ?: ""

                                        PreferenceManager.setTPK(
                                            this@SvpActivity,
                                            StringUtils.convertHexToASCII(get48)
                                        )

                                        alertDialog(
                                            isShowDialog = false,
                                            getRc,
                                            packed,
                                            success,
                                            unpack
                                        )

                                        ISOViewModel.setIsVisibleStartDate(true)
                                    } catch (e: Exception) {
                                        AlertDialog.Builder(this@SvpActivity)
                                            .setTitle("ISO FAILED")
                                            .setMessage("response raw: $success\n\nerr message=${e.message}")
                                            .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
                                            .show()
                                        LogUtils.e(
                                            TAG,
                                            "Error unpacking ISO8583 message: ${e.printStackTrace()}"
                                        )
                                    }
                                }
                            }
                        },
                        onStartDate = {
                            commandValue = START_DATE
                            ISOViewModel.emvUtil?.let { createEmvDialog(this, it) }
                        },
                        onSuccess = { cardNum ->
//                            saveSession()
                        },
                        onError = { },
                        onSettingClick = {
                            startActivity(Intent(this@SvpActivity, SettingsActivity::class.java))
                        }
                    )
                }
            }
        }
    }

    fun handleInitSdk() {
        EktpUtil.initialize()
        EktpUtil.ektpOpen()
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    apiViewModel.getSvpData.collect { state ->
                        when (state) {
                            is ApiUiState.Success -> {
                                val data = state.data.data
                                LogUtils.d(TAG, "SVP Data Received: \\${data.id}")
                                if (data.sidikJari != null) {
                                    PreferenceManager.setSVPUserId(
                                        this@SvpActivity,
                                        data.id.toString()
                                    )
                                    PreferenceManager.setFingerprintData(
                                        this@SvpActivity,
                                        data.sidikJari
                                    )
                                    handleMatchingFingerprint(data.sidikJari)
                                } else {
                                    handleEnrollFingerprint()
                                }
                                apiViewModel.resetSvpDataState()
                            }

                            is ApiUiState.Error -> {
                                LogUtils.d(TAG, "Error: \\${state.message}")
                            }

                            else -> {}
                        }
                    }
                }
                launch {
                    apiViewModel.svpData.collect { state ->
                        when (state) {
                            is ApiUiState.Success -> {
                                LogUtils.d(TAG, "SVP Data Sent Successfully")
                                Toast.makeText(
                                    this@SvpActivity,
                                    "Sidik jari berhasil di-enroll.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Thread.sleep(700)

                                val body = SvpRequestBody(
                                    pan = cardNum!!,
                                )

                                apiViewModel.getSvpData(body)
                                apiViewModel.resetSvpDataState()
                            }

                            is ApiUiState.Error -> {
                                LogUtils.d(TAG, "Error sending SVP Data: \\${state.message}")
                                Toast.makeText(this@SvpActivity, state.message, Toast.LENGTH_SHORT)
                                    .show()
                            }

                            else -> {}
                        }
                    }
                }
            }
        }
    }

    private fun saveSession() {
        PreferenceManager.setSvpCardNum(this, track2data)
        Toast.makeText(this, "Aktivasi berhasil!", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, OfficerActivity::class.java))
        ISOViewModel.clearState()
        track2data = null
        finish()
    }

    private fun isoSendMessage() {
        try {
            val body = SvpRequestBody(
                pan = cardNum!!,
            )
            apiViewModel.getSvpData(body)
        } catch (e: Exception) {
            LogUtils.e(TAG, "ISO Send Message Error: ${e.message}")
        }
    }

    private fun handleMatchingFingerprint(sidikJari: String) {
        createFingerDialog(this, FINGERPRINT_MESSAGE) { _, _ ->
            try {
                spvViewModel.handleFingerprintResult(
                    type = "MATCH_FINGERPRINT",
                    Base64.decode(sidikJari, Base64.NO_WRAP)
                )

                Toast.makeText(this, "Sidik jari cocok. Aktivasi selesai.", Toast.LENGTH_SHORT)
                    .show()

                // TODO(refactor): remove this if possible
                Thread.sleep(700)
//                saveSession()
                // End TODO

                val result = IsoHelper.buildStartDateWithModel()
                if (result != null) {
                    val (pack, iso) = result
                    ISOViewModel.isoSendMessage(this, commandValue, pack) { success, error ->
                        if (error != null) {
                            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                            return@isoSendMessage
                        }
                        try {
                            val unpack = ISO8583.unpackFromHex(success!!, iso)
                            val getRc = StringUtils.convertHexToASCII(unpack.get("39"))
                            val tpkKey = PreferenceManager.getTPK(this)

                            val unpackPayload = ISO8583.unpackFromHex(pack, iso)

                            AlertDialog.Builder(this)
                                .setTitle("$getRc - ISO Success")
                                .setMessage(
                                    "tpk:$tpkKey\n\npayload:$pack\n\nPIN:${unpackPayload["52"]}\n\nResponse Code: $getRc\n\n\nresponse raw: $success\n\n" + unpack.entries.joinToString(
                                        "\n"
                                    ) { "${it.key}: ${it.value}" })
                                .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
                                .show()

                            saveSession()
                        } catch (e: Exception) {
                            AlertDialog.Builder(this)
                                .setTitle("ISO Start Date FAILEDD")
                                .setMessage(
                                    "statusRet=${statusRet}" +
                                            "response raw: $success\n\niso payload:$pack\n\ntpk:${
                                                PreferenceManager.getTPK(
                                                    this
                                                )
                                            }\n\npinblock:$pinBlockOwn\n\nerr message=${e.message}"
                                )
                                .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
                                .show()
                            LogUtils.e(
                                TAG,
                                "Error unpacking ISO8583 message: ${e.printStackTrace()}"
                            )
                        }

                    }
                }
            } catch (e: Exception) {
                LogUtils.e(TAG, "Fingerprint Matching Error: ${e.printStackTrace()}")
            }
        }
    }

    private fun handleEnrollFingerprint() {
        AlertDialog.Builder(this)
            .setTitle("Fingerprint Enrollment")
            .setMessage("Sidik jari belum terdaftar. Apakah Anda ingin enroll sidik jari sekarang?")
            .setPositiveButton("Enroll") { dialog, _ ->
                dialog.dismiss()
                createFingerDialog(this, FINGERPRINT_MESSAGE) { _, _ ->
                    try {
                        spvViewModel.handleFingerprintResult(type = "ENROLLMENT", null)

                        LogUtils.d(TAG, "FEATURE64KANAN: $feature64Kanan")
                        val body = SvpRequestBody(
                            pan = cardNum!!,
                            sidikJari = Base64.encodeToString(fmd, Base64.NO_WRAP)
                        )
                        apiViewModel.sendSvpData(body)

                    } catch (e: Exception) {
                        LogUtils.e(TAG, "Fingerprint Enrollment Error: ${e.message}")
                    }
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onDoSomething(context: Context) {
        when (commandValue) {
            START_DATE -> {
                if (step == 1) {

                    runOnUiThread {
                        val dialog = AlertDialog.Builder(this)
                            .setMessage("Kartu sudah di swipe")
                            .setCancelable(false)
                            .create()
                        dialog.show()
                        Handler(Looper.getMainLooper()).postDelayed({
                            dialog.dismiss()

                            LogUtils.i(TAG, "Start/End Date Step 1")
                            LogUtils.i(TAG, "Opening EMV Dialog for Start/End Date Step 1")
                            ISOViewModel.emvUtil?.let {
                                createEmvDialog(
                                    this,
                                    it,
                                    message = "Insert or Swipe Officer Card"
                                )
                            }

                        }, 2000)
                    }

                    step++
                } else {

                    isoSendMessage()
                }
            }

            else -> LogUtils.e(TAG, "Unknown command value: $commandValue")
        }
    }

    override fun onError(message: String) {
        LogUtils.e(TAG, "EMV Error: $message")
    }


    private fun alertDialog(
        isShowDialog: Boolean,
        getRc: String,
        pack: String,
        success: String,
        unpack: Map<String, Any>
    ) {
        runOnUiThread {
            val tpk = PreferenceManager.getTPK(this@SvpActivity)

            LogUtils.d(TAG, "tpk: $tpk")
            LogUtils.d(TAG, "payload: $pack")
            LogUtils.d(TAG, "Response Code: $getRc")
            LogUtils.d(TAG, "response raw: $success")
            LogUtils.d(
                TAG,
                "unpack: ${unpack.entries.joinToString("\n") { "${it.key}: ${it.value}" }}"
            )

            AlertDialog.Builder(this@SvpActivity)
                .setTitle("$getRc - ISO Success")
                .setMessage(
                    "tpk:$tpk\n\npayload:$pack\n\nResponse Code: $getRc\n\n\nresponse raw: $success\n\n" + unpack.entries.joinToString(
                        "\n"
                    ) { "${it.key}: ${it.value}" })
                .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }
}

