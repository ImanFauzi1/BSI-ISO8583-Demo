package com.app.edcpoc

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.app.edcpoc.ui.screens.SettingsScreen

class Settings : ComponentActivity() {
    override fun onCreate(saveInstanceState: Bundle?) {
        super.onCreate(saveInstanceState)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SettingsScreen(
                        onBack = {},
                        onSaveSettings = { gatewayUrl, host, hostPort, tms, tmsPort ->
                            onSaveSettings(gatewayUrl, host, hostPort, tms, tmsPort)
                        },
                        onPing = {}
                    )
                }
            }
        }
    }

    private fun onSaveSettings(gatewayUrl: String?, host: String?, hostPort: String?, tms: String?, tmsPort: String?) {
        if (gatewayUrl.isNullOrBlank() || host.isNullOrBlank() || hostPort.isNullOrBlank() || tms.isNullOrBlank() || tmsPort.isNullOrBlank()) {
            Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
            return
        }
        PreferenceManager.setGatewayUrl(this, gatewayUrl)
        PreferenceManager.setHost(this, host)
        PreferenceManager.setHostPort(this, hostPort)
        PreferenceManager.setTms(this, tms)
        PreferenceManager.setTmsPort(this, tmsPort)
        Toast.makeText(this, "Settings berhasil disimpan", Toast.LENGTH_SHORT).show()
    }
}