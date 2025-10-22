package com.app.edcpoc

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.edcpoc.ui.theme.EdcpocTheme

class OfficerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EdcpocTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    OfficerLoginScreen(
                        onLogin = {
                            PreferenceManager.setOfficerLoggedIn(this, true)
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        },
                        onLogout = {
                            PreferenceManager.setOfficerLoggedIn(this, false)
                            startActivity(Intent(this, OfficerActivity::class.java))
                            finish()
                        },
                        onCloseDate = {
                            PreferenceManager.clearAll(this)
                            startActivity(Intent(this, SvpActivity::class.java))
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun OfficerLoginScreen(
    onLogin: () -> Unit,
    onLogout: () -> Unit,
    onCloseDate: () -> Unit
) {
    val context = LocalContext.current
    var isLoggedIn by remember { mutableStateOf(false) }

    // Cek session officer saat composable pertama kali dipanggil
    LaunchedEffect(Unit) {
        isLoggedIn = PreferenceManager.isOfficerLoggedIn(context)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tombol Close Date di pojok kanan atas, selalu tampil
        IconButton(
            onClick = onCloseDate,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close Date",
                tint = Color(0xFFB71C1C),
                modifier = Modifier.size(28.dp)
            )
        }
        // Card dihilangkan, hanya Column biasa tanpa border/shadow
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(32.dp)
                .background(Color(0xFFF5F5F5))
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
            if (!isLoggedIn) {
                Button(
                    onClick = {
                        onLogin()
                        isLoggedIn = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
                ) {
                    Text(text = "Login", color = Color.White, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = {
                        onLogout()
                        isLoggedIn = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(text = "Logout", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
