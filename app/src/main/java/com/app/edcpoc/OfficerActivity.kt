package com.app.edcpoc

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.edcpoc.interfaces.EmvUtilInterface
import com.app.edcpoc.ui.theme.EdcpocTheme
import com.app.edcpoc.ui.viewmodel.ISOViewModel
import com.app.edcpoc.utils.Constants.END_DATE
import com.app.edcpoc.utils.Constants.LOGON
import com.app.edcpoc.utils.Constants.commandValue
import com.app.edcpoc.utils.Constants.track2data
import com.app.edcpoc.utils.CoreUtils.initializeEmvUtil
import com.app.edcpoc.utils.DialogUtil.createEmvDialog
import com.app.edcpoc.utils.IsoManager.ISO8583
import com.app.edcpoc.utils.IsoManager.IsoUtils
import com.app.edcpoc.utils.IsoManager.IsoUtils.generateIsoStartEndDate
import com.zcs.sdk.util.LogUtils
import com.zcs.sdk.util.StringUtils
import kotlin.getValue

class OfficerActivity : ComponentActivity(), EmvUtilInterface {
    private val ISOViewModel: ISOViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (PreferenceManager.getSvpCardNum(this).isNullOrEmpty()) {
            startActivity(Intent(this, SvpActivity::class.java))
            finish()
            return
        }

        if (PreferenceManager.getOfficerCardNum(this) != null){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        ISOViewModel.emvUtil = initializeEmvUtil(this@OfficerActivity, this)

        setContent {
            EdcpocTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    OfficerLoginScreen(
                        ISOViewModel = ISOViewModel,
                        onLogin = {
                            commandValue = LOGON
                            ISOViewModel.emvUtil?.let { createEmvDialog(this, it)  }
                        },
                        onError = { message ->
                            Toast.makeText(this@OfficerActivity, message, Toast.LENGTH_LONG).show()
                        },
                        onCloseDate = {
                            LogUtils.d("test", "Close Date clicked")
                            commandValue = END_DATE
                            ISOViewModel.emvUtil?.let { createEmvDialog(this, it) }
                        },
                        onSuccess = {
                            if (commandValue == END_DATE) {
                                onCloseDate()
                                return@OfficerLoginScreen
                            }
                            PreferenceManager.setOfficerLoggedIn(this@OfficerActivity, track2data)
                            startActivity(Intent(this, MainActivity::class.java))
                            track2data = null
                            finish()
                        }
                    )
                }
            }
        }
    }

    fun onCloseDate() {
        PreferenceManager.clearAll(this)
        startActivity(Intent(this, SvpActivity::class.java))
        finish()
    }

    override fun onDoSomething(context: Context) {
        when(commandValue) {
            LOGON -> {
                val iso = IsoUtils.generateIsoLogonLogoff("0800", "810000", track2data!!)
                val pack = ISO8583.packToHex(iso)
//                ISOViewModel.isoSendMessage(this@OfficerActivity, commandValue, StringUtils.convertHexToBytes(pack))
            }
            END_DATE -> {
                val iso = generateIsoStartEndDate("0800", "920000")
                val pack = ISO8583.packToHex(iso)

//                ISOViewModel.isoSendMessage(this@OfficerActivity,commandValue, StringUtils.convertHexToBytes(pack))
            }
        }
    }

    override fun onError(message: String) {
        Toast.makeText(this@OfficerActivity, message, Toast.LENGTH_LONG).show()
    }
}

@Composable
fun OfficerLoginScreen(
    ISOViewModel: ISOViewModel,
    onLogin: () -> Unit,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit,
    onCloseDate: () -> Unit
) {
    val uiState by ISOViewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (!uiState.cardNum.isNullOrEmpty()) {
            onSuccess(uiState.cardNum!!)
            return@LaunchedEffect
        }
        if (!uiState.errorMessage.isNullOrEmpty()) {
            onError(uiState.errorMessage!!)
            return@LaunchedEffect
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .background(Color.Transparent)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onCloseDate
                )
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close Date",
                tint = Color(0xFFB71C1C),
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "Close Date",
                color = Color(0xFFB71C1C),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Login Officer",
                tint = Color(0xFF1976D2),
                modifier = Modifier.size(64.dp).padding(bottom = 16.dp)
            )
            Text(
                text = "Login Officer",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Text(
                text = "Silakan login sebagai officer untuk melanjutkan ke aplikasi.",
                fontSize = 16.sp,
                color = Color(0xFF616161),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Button(
                onClick = { onLogin() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
            ) {
                Text(text = "Login", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
