package com.app.edcpoc.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PsamInputDialog(
    defaultPCID: String = "2024BB12121100000000000000095067",
    defaultConfig: String = "E743E49AC5FD1A180D28AB938B1F3F6FC6F008D3BE955670BE598C9E084837F1",
    onSubmit: (pcid: String, config: String) -> Unit,
    onCancel: () -> Unit
) {
    var pcid by remember { mutableStateOf(defaultPCID) }
    var config by remember { mutableStateOf(defaultConfig) }

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Input PSAM") },
        text = {
            Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                OutlinedTextField(
                    value = pcid,
                    onValueChange = { pcid = it },
                    label = { Text("PCID") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = config,
                    onValueChange = { config = it },
                    label = { Text("CONFIG FILE") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSubmit(pcid, config) }, modifier = Modifier.padding(end = 8.dp)) {
                Text("OK")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onCancel) {
                Text("Cancel")
            }
        }
    )
}

