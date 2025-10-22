package com.app.edcpoc

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.edcpoc.ui.theme.EdcpocTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.text.style.TextAlign
import com.app.edcpoc.interfaces.EmvUtilInterface
import com.app.edcpoc.ui.viewmodel.ISOViewModel
import com.app.edcpoc.utils.Constants.commandValue
import com.app.edcpoc.utils.Constants.track2data
import com.app.edcpoc.utils.CoreUtils.initializeEmvUtil
import com.app.edcpoc.utils.DialogUtil.createEmvDialog
import com.idpay.victoriapoc.utils.IsoManagement.IsoUtils.generateIsoStartEndDate
import com.zcs.sdk.util.LogUtils
import com.zcs.sdk.util.StringUtils
import kotlin.getValue

class SvpActivity : ComponentActivity(), EmvUtilInterface {
    private val ISOViewModel: ISOViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (PreferenceManager.getSvpCardNum(this) != null) {
            startActivity(Intent(this, OfficerActivity::class.java))
            finish()
            return
        }

        ISOViewModel.emvUtil = initializeEmvUtil(this@SvpActivity, this)

        setContent {
            EdcpocTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SvpLockedScreen(
                        ISOViewModel = ISOViewModel,
                        onActivate = {
                            commandValue = "startDate"
                            ISOViewModel.emvUtil?.let { createEmvDialog(this, it)  }
                        },
                        onSuccess = { cardNum ->
                            saveSession(cardNum)
                        },
                        onError = {  }
                    )
                }
            }
        }
    }

    private fun saveSession(cardNum: String) {
        PreferenceManager.setSvpCardNum(this, track2data)
        Toast.makeText(this, "Aktivasi berhasil!", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, OfficerActivity::class.java))
        ISOViewModel.clearState()
        track2data = null
        finish()
    }

    override fun onDoSomething(context: Context) {
        when(commandValue) {
            "startDate" -> {
                val iso = generateIsoStartEndDate("0800", "910000")
                ISOViewModel.isoSendMessage(commandValue, StringUtils.convertHexToBytes(iso))
            }
        }
    }

    override fun onError(message: String) {

    }
}

@Composable
fun SvpLockedScreen(
    ISOViewModel: ISOViewModel,
    onActivate: () -> Unit,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    val currentOnSuccess by rememberUpdatedState(onSuccess)
    val currentOnError by rememberUpdatedState(onError)

    val uiState by ISOViewModel.uiState.collectAsState()

    LaunchedEffect(uiState.cardNum, uiState.errorMessage) {
        LogUtils.d("SvpLockedScreen", "UI State changed: $uiState")
        if (!uiState.cardNum.isNullOrEmpty()) {
            currentOnSuccess(uiState.cardNum!!)
        }
        if (!uiState.errorMessage.isNullOrEmpty()) {
            currentOnError(uiState.errorMessage!!)
            ISOViewModel.clearState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(0.85f)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Device Locked",
                tint = Color(0xFFB71C1C),
                modifier = Modifier
                    .size(64.dp)
                    .padding(bottom = 16.dp)
            )
            Text(
                text = "Device Locked",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB71C1C),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Text(
                text = "Perangkat ini terkunci. Silakan lakukan aktivasi untuk melanjutkan penggunaan aplikasi.",
                fontSize = 16.sp,
                color = Color(0xFF616161),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            Button(
                onClick = onActivate,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                )
            ) {
                Text(text = "Aktivasi", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
