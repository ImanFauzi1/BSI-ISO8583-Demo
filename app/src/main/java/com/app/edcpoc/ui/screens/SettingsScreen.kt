package com.app.edcpoc.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.edcpoc.PreferenceManager
import com.app.edcpoc.ui.components.AppBarWithBackButton
import com.app.edcpoc.ui.viewmodel.SettingsViewModel
import com.app.edcpoc.ui.viewmodel.PingStatus
import com.app.edcpoc.utils.Utility
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SettingsScreen(
    onSaveSettings: (gatewayUrl: String, host: String, hostPort: String, tms: String, tmsPort: String) -> Unit,
    onBack: () -> Unit,
    onPing: () -> Unit
) {
    var gatewayUrl by remember { mutableStateOf("") }
    var host by remember { mutableStateOf("") }
    var tms by remember { mutableStateOf("") }
    var gatewayPort by remember { mutableStateOf("80") }
    var hostPort by remember { mutableStateOf("80") }
    var tmsPort by remember { mutableStateOf("80") }
    var showPingDialog by remember { mutableStateOf(false) }
    val pingItems = listOf(
        "Ping ke Gateway",
        "Telnet ke Gateway",
        "Ping ke Host",
        "Telnet ke Host",
        "Ping ke TMS",
        "Telnet ke TMS"
    )
    val settingsViewModel: SettingsViewModel = viewModel()
    val pingStatuses by settingsViewModel.pingStatuses.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        gatewayUrl = PreferenceManager.getGatewayUrl(context) ?: ""
        gatewayPort = PreferenceManager.getGatewayPort(context) ?: "80"
        host = PreferenceManager.getHost(context) ?: ""
        hostPort = PreferenceManager.getHostPort(context) ?: "80"
        tms = PreferenceManager.getTms(context) ?: ""
        tmsPort = PreferenceManager.getTmsPort(context) ?: "80"
    }

    fun startPingSequence() {
        showPingDialog = true
        settingsViewModel.startPingSequence(
            gatewayUrl = gatewayUrl,
            gatewayPort = gatewayPort,
            host = host,
            hostPort = hostPort,
            tms = tms,
            tmsPort = tmsPort
        )
    }


    if (showPingDialog) {
        AlertDialog(
            onDismissRequest = { showPingDialog = false },
            title = { Text("Ping Status") },
            text = {
                Column {
                    pingItems.forEachIndexed { idx, label ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(label, modifier = Modifier.weight(1f))
                            when (pingStatuses[idx]) {
                                PingStatus.IDLE -> Icon(Icons.Default.HourglassEmpty, contentDescription = "Idle")
                                PingStatus.LOADING -> CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                PingStatus.SUCCESS -> Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = Color.Green)
                                PingStatus.FAIL -> Icon(Icons.Default.Error, contentDescription = "Fail", tint = Color.Red)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showPingDialog = false }) {
                    Text("Tutup")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        AppBarWithBackButton(
            title = "Settings",
            onBack = onBack
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = gatewayUrl,
            onValueChange = { gatewayUrl = it },
            label = { Text("Gateway") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
//        Spacer(modifier = Modifier.height(8.dp))
//        OutlinedTextField(
//            value = gatewayPort,
//            onValueChange = { gatewayPort = it.filter { c -> c.isDigit() } },
//            label = { Text("Gateway Port") },
//            singleLine = true,
//            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
//            modifier = Modifier.fillMaxWidth(),
//            shape = RoundedCornerShape(12.dp)
//        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = host,
            onValueChange = { host = it },
            label = { Text("Host") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = hostPort,
            onValueChange = { hostPort = it.filter { c -> c.isDigit() } },
            label = { Text("Host Port") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = tms,
            onValueChange = { tms = it },
            label = { Text("TMS") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = tmsPort,
            onValueChange = { tmsPort = it.filter { c -> c.isDigit() } },
            label = { Text("TMS Port") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { onSaveSettings(gatewayUrl, host, hostPort, tms, tmsPort) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Simpan", fontSize = 15.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = {
                    startPingSequence()
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Ping", fontSize = 15.sp)
            }

        }
    }
}
