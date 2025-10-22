package com.app.edcpoc

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.edcpoc.data.model.UserRole
import com.app.edcpoc.interfaces.EmvUtilInterface
import com.app.edcpoc.ui.components.*
import com.app.edcpoc.ui.screens.ChangePinScreen
import com.app.edcpoc.ui.screens.CreatePinScreen
import com.app.edcpoc.ui.screens.KeamananScreen
import com.app.edcpoc.ui.screens.LoginScreen
import com.app.edcpoc.ui.screens.PinManagementScreen
import com.app.edcpoc.ui.screens.ReissuePinScreen
import com.app.edcpoc.ui.screens.SessionManagementScreen
import com.app.edcpoc.ui.screens.TransactionScreen
import com.app.edcpoc.ui.screens.VerificationPinScreen
import com.app.edcpoc.ui.theme.EdcpocTheme
import com.app.edcpoc.ui.viewmodel.AuthViewModel
import com.app.edcpoc.utils.Constants
import com.app.edcpoc.utils.Constants.commandValue
import com.app.edcpoc.utils.DialogUtil.createEmvDialog
import com.app.edcpoc.utils.EmvUtil
import com.app.edcpoc.utils.LogUtils
import com.google.gson.Gson
import com.idpay.victoriapoc.utils.IsoManagement.IsoUtils
import com.idpay.victoriapoc.utils.IsoManagement.IsoUtils.generateIsoStartEndDate
import com.zcs.sdk.util.StringUtils
import org.jpos.iso.ISOMsg


class MainActivity : ComponentActivity(), EmvUtilInterface {
    private val TAG = MainActivity::class.java.simpleName
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        authViewModel.initialize(this@MainActivity, this)

        setContent {
            EdcpocTheme {
                EDCMainApp(this@MainActivity)
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
fun EDCMainApp(context: Context, authViewModel: AuthViewModel = viewModel()) {
    var currentScreen by remember { mutableStateOf("login") }
    val authState by authViewModel.uiState.collectAsState()
    
    // Debug logging
    LaunchedEffect(authState.isLoggedIn, currentScreen) {
        println("DEBUG: isLoggedIn = ${authState.isLoggedIn}, currentScreen = $currentScreen, user = ${authState.currentUser?.name}")
    }
    
    // Determine which screen to show
    val screenToShow = when {
        !authState.isLoggedIn -> "login"
        authState.isLoggedIn && currentScreen == "login" -> "home"
        else -> currentScreen
    }
    
    when (screenToShow) {
        "login" -> {
            LoginScreen(
                onLoginSuccess = { 
                    currentScreen = "home"
                }
            )
        }
        "home" -> EDCHomeScreen(
            currentUser = authState.currentUser!!,
            onTransaksiClick = { createEmvDialog(context, emvUtil = authViewModel.emvUtil) },
            onKeamananClick = { createEmvDialog(context, emvUtil = authViewModel.emvUtil) },
            onManajemenPINClick = { createEmvDialog(context, emvUtil = authViewModel.emvUtil) },
            onSessionManagementClick = { createEmvDialog(context, emvUtil = authViewModel.emvUtil) },
            onLogoutClick = {
                authViewModel.logout()
                currentScreen = "login"
            },
            dialogState = authViewModel.dialogState.collectAsState().value,
            dismissDialog = { authViewModel.dismissDialog() }
        )
        "keamanan" -> KeamananScreen(
            onBackClick = { currentScreen = "home" }
        )
        "create_pin" -> CreatePinScreen(
            onBackClick = { currentScreen = "pin_management" },
            onPinCreated = { pin ->
                currentScreen = "pin_management"
            }
        )
        "pin_management" -> PinManagementScreen(
            currentUser = authState.currentUser!!,
            onBackClick = { currentScreen = "home" },
            onCreatePinClick = { currentScreen = "create_pin" },
            onChangePinClick = { currentScreen = "change_pin" },
            onReissuePinClick = { currentScreen = "reissue_pin" },
            onVerificationPinClick = { currentScreen = "verification_pin" }
        )
        "change_pin" -> ChangePinScreen(
            onBackClick = { currentScreen = "pin_management" },
            onPinChanged = { pin ->
                currentScreen = "pin_management"
            }
        )
        "reissue_pin" -> ReissuePinScreen(
            onBackClick = { currentScreen = "pin_management" },
            onPinReissued = { cardNumber ->
                currentScreen = "pin_management"
            }
        )
        "verification_pin" -> VerificationPinScreen(
            onBackClick = { currentScreen = "pin_management" },
            onPinVerified = { isValid ->
                currentScreen = "pin_management"
            }
        )
        "session_management" -> SessionManagementScreen(
            currentUser = authState.currentUser!!,
            onBackClick = { currentScreen = "home" }
        )
        "transaksi" -> TransactionScreen(
            onBackClick = { currentScreen = "home" },
            onTransactionSuccess = { transaction ->
                // Handle transaction success
                currentScreen = "home"
            }
        )
        else -> {
            // Fallback for unknown screen states
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Loading...",
                        fontSize = 18.sp,
                        color = Color(0xFF757575)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator()
                }
            }
        }
    }
}
