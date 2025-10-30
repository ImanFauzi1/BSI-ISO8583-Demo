package com.app.edcpoc

import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.edcpoc.data.model.KtpDataModel
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.app.edcpoc.ui.components.LoadingDialog
import com.app.edcpoc.ui.viewmodel.ApiUiState
import com.app.edcpoc.ui.viewmodel.ApiViewModel
import com.app.edcpoc.utils.Constants.FACE_RECOGNIZE
import com.app.edcpoc.utils.Constants.KTP_READ
import com.app.edcpoc.utils.Constants.MANUAL_KTP_READ
import com.app.edcpoc.utils.KtpReaderManager.payloadRequest
import com.app.edcpoc.utils.LogUtils
import kotlinx.coroutines.launch

class KtpPreviewActivity : ComponentActivity() {
    private val TAG = "KtpPreviewActivity"
    private val apiViewModel: ApiViewModel by viewModels()

    private var isLoading by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val data = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("ktpData", KtpDataModel::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("ktpData") as? KtpDataModel
        }
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (data != null) {
                        // Observe loading state dari ViewModel
                        val ktpState by apiViewModel.ktpState.collectAsState()
                        // Tampilkan loading jika state Loading
                        if (ktpState is ApiUiState.Loading) {
                            LoadingDialog()
                        }
                        KtpPreviewScreen(
                            data = data,
                            onBack = { finish() },
                            onSubmitKtp = { handleSubmitKtp(data) }
                        )
                    } else {
                        Text("Data KTP tidak ditemukan.", modifier = Modifier.padding(16.dp))
                    }
                }
            }
        }
        observeKtpState()
    }

    private fun observeKtpState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                apiViewModel.ktpState.collect { state ->
                    when (state) {
                        is ApiUiState.Success -> {
                            Toast.makeText(this@KtpPreviewActivity, "Sukses submit KTP", Toast.LENGTH_SHORT).show()
                        }
                        is ApiUiState.Error -> {
                            LogUtils.d(TAG, "Error submitting KTP: ${state.message}")
                            Toast.makeText(this@KtpPreviewActivity, state.message, Toast.LENGTH_SHORT).show()
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun handleSubmitKtp(data: KtpDataModel) {
        when(data.type) {
            KTP_READ, FACE_RECOGNIZE -> apiViewModel.sendKtpData(payloadRequest())
            MANUAL_KTP_READ -> apiViewModel.sendKtpDataSpv(payloadRequest())
            else -> {
                Toast.makeText(this, "Unknown KTP data type", Toast.LENGTH_SHORT).show()
                LogUtils.e("KtpPreviewActivity", "Unknown KTP data type: ${data.type}")
            }
        }
    }
}

@Composable
fun KtpPreviewScreen(data: KtpDataModel, onBack: () -> Unit, onSubmitKtp: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        AppBarWithBackButton(title = "Preview KTP", onBack = onBack)
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Baris gambar: Foto, Tanda Tangan, Sidik Jari
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!data.foto.isNullOrBlank()) {
                    val ktpBitmap = data.foto.base64ToBitmapOrNull()
                    if (ktpBitmap != null) {
                        Image(
                            bitmap = ktpBitmap.asImageBitmap(),
                            contentDescription = "Foto KTP",
                            modifier = Modifier.size(120.dp).padding(8.dp)
                        )
                    }
                }
                if (!data.tandaTangan.isNullOrBlank()) {
                    val ttdBitmap = data.tandaTangan.base64ToBitmapOrNull()
                    if (ttdBitmap != null) {
                        Image(
                            bitmap = ttdBitmap.asImageBitmap(),
                            contentDescription = "Tanda Tangan",
                            modifier = Modifier.size(120.dp).padding(8.dp)
                        )
                    }
                }
                if (!data.sidikJari.isNullOrBlank()) {
                    val sidikJariBitmap = data.sidikJari.base64ToBitmapOrNull()
                    if (sidikJariBitmap != null) {
                        Image(
                            bitmap = sidikJariBitmap.asImageBitmap(),
                            contentDescription = "Fingerprint Preview",
                            modifier = Modifier.size(120.dp).padding(8.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(thickness = 1.dp, color = Color(0xFFE0E0E0))
            Spacer(modifier = Modifier.height(16.dp))
            Text("NIK", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Gray)
            Text(data.nik, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))
            Text("Nama", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Gray)
            Text(data.nama, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
            Text("Jenis Kelamin", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Gray)
            Text(data.jenisKelamin, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
            Text("Tempat, Tanggal Lahir", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Gray)
            Text("${data.tempatLahir}, ${data.tanggalLahir}", fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
            Text("Alamat", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Gray)
            Text(data.alamat, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("RT/RW: ", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Gray)
                Text("${data.rt}/${data.rw}", fontSize = 16.sp)
            }
            Text("Kelurahan/Desa", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Gray)
            Text(data.kelurahan, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
            Text("Kecamatan", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Gray)
            Text(data.kecamatan, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
            Text("Kota/Kabupaten", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Gray)
            Text(data.kota, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
            Text("Provinsi", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Gray)
            Text(data.provinsi, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
            Text("Golongan Darah", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Gray)
            Text(data.golonganDarah, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
            Text("Agama", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Gray)
            Text(data.agama, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
            Text("Status Perkawinan", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Gray)
            Text(data.statusPerkawinan, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
            Text("Pekerjaan", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Gray)
            Text(data.pekerjaan, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
            Text("Kewarganegaraan", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Gray)
            Text(data.kewarganegaraan, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(thickness = 1.dp, color = Color(0xFFE0E0E0))
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onSubmitKtp() },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Submit", fontSize = 16.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBarWithBackButton(title: String, onBack: () -> Unit) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(android.R.drawable.ic_menu_revert),
                    contentDescription = "Back"
                )
            }
        }
    )
}

fun String.base64ToBitmapOrNull(): android.graphics.Bitmap? {
    return try {
        if (this.isBlank()) return null
        val decoded = Base64.decode(this, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
    } catch (_: Exception) {
        null
    }
}
