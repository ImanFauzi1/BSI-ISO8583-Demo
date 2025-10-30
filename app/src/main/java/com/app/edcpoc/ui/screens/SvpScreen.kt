package com.app.edcpoc.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.edcpoc.ui.viewmodel.ISOViewModel
import com.app.edcpoc.utils.LogUtils


@Composable
fun SvpLockedScreen(
    ISOViewModel: ISOViewModel,
    onActivate: () -> Unit,
    onStartDate: () -> Unit,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit,
    onSettingClick: () -> Unit
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
            .fillMaxSize()
    ) {
        // IconButton Settings di pojok kanan atas
        IconButton(
            onClick = onSettingClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings Icon",
                tint = Color.DarkGray,
                modifier = Modifier.size(32.dp)
            )
        }
        // Konten utama tetap di tengah
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
                if (uiState.isVisibleStartDate) {
                    Button(
                        onClick = onStartDate,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)
                        )
                    ) {
                        Text(text = "Start Date", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

    }
}