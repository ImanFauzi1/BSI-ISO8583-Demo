package com.app.edcpoc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun LoggingSection(logs: List<String>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Logging", color = Color.Black)
            if (logs.isEmpty()) {
                Text(text = "Belum ada log ISO.", color = Color.Gray)
            } else {
                logs.forEach { log ->
                    Text(text = "- $log", color = Color.DarkGray, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}
