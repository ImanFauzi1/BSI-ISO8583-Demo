package com.app.edcpoc

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.edcpoc.interfaces.EmvUtilInterface
import com.app.edcpoc.ui.components.*
import com.app.edcpoc.ui.theme.EdcpocTheme
import com.app.edcpoc.ui.viewmodel.AuthViewModel
import com.app.edcpoc.utils.Constants
import com.app.edcpoc.utils.Constants.commandValue
import com.app.edcpoc.utils.CoreUtils.initializeEmvUtil
import com.app.edcpoc.utils.DialogUtil.createEmvDialog
import com.app.edcpoc.utils.LogUtils
import com.idpay.victoriapoc.utils.IsoManagement.IsoUtils
import com.idpay.victoriapoc.utils.IsoManagement.IsoUtils.generateIsoStartEndDate
import com.zcs.sdk.util.StringUtils


class MainActivity : ComponentActivity(), EmvUtilInterface {
    private val TAG = MainActivity::class.java.simpleName
    private val authViewModel: AuthViewModel by viewModels()

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

        authViewModel.emvUtil = initializeEmvUtil(this@MainActivity, this)

        setContent {
            EdcpocTheme {
                // Hapus login officer di MainActivity, langsung tampilkan home
                EDCHomeApp(this@MainActivity)
            }
        }
    }


    // EmvUtilInterface implementation
    override fun onDoSomething(context: Context) {
        when(commandValue) {
            "startDate", "closeDate" -> {
                val proc = when(commandValue) {
                    "startDate" -> "910000"
                    "closeDate" -> "920000"
                    else -> ""
                }

                val iso = generateIsoStartEndDate("0800", proc)
                authViewModel.isoSendMessage(commandValue, StringUtils.convertHexToBytes(iso))
            }
            "logon", "logoff" -> {
                val proc = when(commandValue) {
                    "logon" -> "810000"
                    "logoff" -> "820000"
                    else -> ""
                }

                val iso = IsoUtils.generateIsoLogonLogoff("0800", proc)
                authViewModel.isoSendMessage(commandValue, StringUtils.convertHexToBytes(iso))
            }
            "createPIN" -> {
                if (Constants.step == 1) {
                    Thread.sleep(600)
                    createEmvDialog(this@MainActivity, emvUtil = authViewModel.emvUtil)
                    Constants.step++
                    return
                }
                Constants.step = 1
                val iso = IsoUtils.generateIsoCreatePIN()
                authViewModel.isoSendMessage(commandValue, StringUtils.convertHexToBytes(iso))
            }
            "reissuePIN" -> {
                if (Constants.step == 1) {
                    Thread.sleep(600)
                    createEmvDialog(this@MainActivity, emvUtil = authViewModel.emvUtil)
                    Constants.step++
                    return
                }
                Constants.step = 1

                val iso = IsoUtils.generateIsoReissuePIN()
                authViewModel.isoSendMessage(commandValue, StringUtils.convertHexToBytes(iso))
            }
            "verifyPIN" -> {
                val iso = IsoUtils.generateIsoVerificationPIN()
                authViewModel.isoSendMessage(commandValue, StringUtils.convertHexToBytes(iso))
            }
            "changePIN" -> {
                val iso = IsoUtils.generateIsoChangePIN()
                authViewModel.isoSendMessage(commandValue, StringUtils.convertHexToBytes(iso))
            }
        }
        LogUtils.i("AuthViewModel", "commandValue=$commandValue")
    }

    override fun onError(message: String) {
        LogUtils.e("EmvUtilInterface", "Error: $message")
    }
}

@Composable
fun EDCHomeApp(context: Context, authViewModel: AuthViewModel = viewModel()) {
    // Asumsikan user sudah login officer, langsung tampilkan home
    var currentScreen by remember { mutableStateOf("home") }
    val authState by authViewModel.uiState.collectAsState()

    when (currentScreen) {
        "home" -> EDCHomeScreen(
            // Hapus currentUser, tidak perlu dikirim
            onTransaksiClick = { createEmvDialog(context, emvUtil = authViewModel.emvUtil) },
            onKeamananClick = { createEmvDialog(context, emvUtil = authViewModel.emvUtil) },
            onManajemenPINClick = { createEmvDialog(context, emvUtil = authViewModel.emvUtil) },
            onSessionManagementClick = { createEmvDialog(context, emvUtil = authViewModel.emvUtil) },
            onLogoutClick = {
                // Logout officer, redirect ke OfficerActivity
                PreferenceManager.setOfficerLoggedIn(context, null)
                val intent = Intent(context, OfficerActivity::class.java)
                if (context is ComponentActivity) {
                    context.startActivity(intent)
                    context.finish()
                }
            },
            dialogState = authViewModel.dialogState.collectAsState().value,
            dismissDialog = { authViewModel.dismissDialog() }
        )
    }
}
