package com.app.edcpoc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.app.edcpoc.ui.theme.EdcpocTheme
import androidx.compose.material3.*
import com.app.edcpoc.ui.viewmodel.DialogState

@Composable
fun EDCHomeScreen(
    onTransaksiClick: () -> Unit,
    onEnrollmentClick: (String) -> Unit,
    onManajemenPINClick: () -> Unit,
    onSessionManagementClick: () -> Unit,
    onLogoutClick: () -> Unit,
    dialogState: DialogState,
    dismissDialog: () -> Unit
) {
    if (dialogState.showDialog) {
        AlertDialog(
            onDismissRequest = dismissDialog,
            title = { Text("Informasi") },
            text = { Text(dialogState.dialogMessage ?: "") },
            confirmButton = {
                Button(onClick = dismissDialog) {
                    Text("OK")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentPadding = PaddingValues(0.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item {
            HeaderSection(
                onLogoutClick = onLogoutClick
            )
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
//        item {
//            TransaksiCard(onTransaksiClick = onTransaksiClick)
//        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
        item {
            EnrollmentCard(onEnrollmentClick = onEnrollmentClick)
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
        item {
            ManajemenPINCard(onManajemenPINClick = onManajemenPINClick)
        }
        // SessionManagementCard bisa tetap tampil jika perlu, atau dihapus jika tidak ada user
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
        item {
            FooterSection()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EDCHomeScreenPreview() {
    EdcpocTheme {
        EDCHomeScreen(
            onTransaksiClick = {},
            onEnrollmentClick = {},
            onManajemenPINClick = {},
            onSessionManagementClick = {},
            onLogoutClick = {},
            dialogState = DialogState(),
            dismissDialog = {}
        )
    }
}
