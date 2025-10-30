package com.app.edcpoc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun LoggingSection(logs: List<String>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(Color.White)
    ) {
        if (logs.isEmpty()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Logging", color = Color.Black)
                Text(text = "Belum ada log ISO.", color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
            }
        } else {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Logging", color = Color.Black)
                LazyColumn(modifier = Modifier
                    .padding(top = 8.dp)
                    .heightIn(max = 200.dp)) {
                    items(logs) { log ->
                        Text(text = "- $log", color = Color.DarkGray, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }
    }
}
