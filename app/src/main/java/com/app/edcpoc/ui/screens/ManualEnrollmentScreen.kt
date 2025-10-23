package com.app.edcpoc.ui.screens

import android.R
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.edcpoc.ui.components.LoadingDialog
import com.app.edcpoc.ui.viewmodel.ApiViewModel
import com.app.edcpoc.ui.viewmodel.ManualEnrollmentUiState
import com.app.edcpoc.ui.viewmodel.ManualEnrollmentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualEnrollmentScreen(
    onBack: () -> Unit,
    onSubmit: (String, String) -> Unit,
    onScanFingerprint: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel: ManualEnrollmentViewModel = viewModel()
    val apiViewModel: ApiViewModel = viewModel()

    val uiState by viewModel.uiState.collectAsState()
    val fingerprintBitmap by viewModel.fingerprintBitmap.collectAsState()
    var nik by remember { mutableStateOf("") }
    var nama by remember { mutableStateOf("") }

    // Observe state dari ViewModel
    LaunchedEffect(uiState) {
        when (uiState) {
            is ManualEnrollmentUiState.Error -> {
                Toast.makeText(context, (uiState as ManualEnrollmentUiState.Error).message, Toast.LENGTH_SHORT).show()
            }
            is ManualEnrollmentUiState.Success -> {
                Toast.makeText(context, "Berhasil submit!", Toast.LENGTH_SHORT).show()
                viewModel.reset()
                nik = ""
                nama = ""
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manual Enrollment", fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(android.R.drawable.ic_menu_revert),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        containerColor = Color(0xFFF8F8F8)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp)
                    .align(Alignment.TopCenter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = nik,
                    onValueChange = { if (it.length <= 16 && it.all { c -> c.isDigit() }) nik = it },
                    label = { Text("NIK") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = { Text("Nama") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(28.dp))
                Text("Fingerprint", fontSize = 16.sp, color = Color.Gray, modifier = Modifier.align(Alignment.Start))
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .size(width = 220.dp, height = 96.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (fingerprintBitmap != null) {
                        Image(
                            bitmap = fingerprintBitmap!!.asImageBitmap(),
                            contentDescription = "Fingerprint Preview",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text("Belum scan", color = Color.DarkGray, fontSize = 14.sp, textAlign = TextAlign.Center)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { onScanFingerprint() },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Scan Fingerprint", fontSize = 15.sp)
                    }
                    Button(
                        onClick = {
                            onSubmit(nik, nama)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Submit", fontSize = 15.sp)
                    }
                }
                if (uiState is ManualEnrollmentUiState.Loading) {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
            if (uiState is ManualEnrollmentUiState.Loading) {
                LoadingDialog()
            }
        }
    }
}
