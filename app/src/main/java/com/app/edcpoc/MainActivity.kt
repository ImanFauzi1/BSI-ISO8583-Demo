package com.app.edcpoc

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.edcpoc.interfaces.EmvUtilInterface
import com.app.edcpoc.ui.components.*
import com.app.edcpoc.ui.theme.EdcpocTheme
import com.app.edcpoc.ui.viewmodel.ISOViewModel
import com.app.edcpoc.utils.Constants
import com.app.edcpoc.utils.Constants.commandValue
import com.app.edcpoc.utils.CoreUtils.initializeEmvUtil
import com.app.edcpoc.utils.DialogUtil.createEmvDialog
import com.app.edcpoc.utils.LogUtils
import com.idpay.victoriapoc.utils.IsoManagement.IsoUtils
import com.zcs.sdk.util.StringUtils


class MainActivity : ComponentActivity(), EmvUtilInterface {
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

        setContent {
            EdcpocTheme {
                EDCHomeApp(
                    this@MainActivity,
                    onEnrollmentClick = {onEnrollmentClick(it)},
                    isoViewModel = isoViewModel
                )
            }
        }
    }

    fun onEnrollmentClick(enrollmentType: String) {

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
