package com.app.edcpoc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.app.edcpoc.data.model.User
import com.app.edcpoc.data.model.UserRole
import com.app.edcpoc.ui.theme.EdcpocTheme
import androidx.compose.material3.*
import com.app.edcpoc.ui.components.*
import com.app.edcpoc.ui.viewmodel.DialogState

@Composable
fun EDCHomeScreen(
    currentUser: User,
    onTransaksiClick: () -> Unit,
    onKeamananClick: () -> Unit,
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
                currentUser = currentUser,
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
            KeamananCard(onKeamananClick = onKeamananClick)
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
        item {
            ManajemenPINCard(onManajemenPINClick = onManajemenPINClick)
        }
        if (currentUser.role == UserRole.SUPERVISOR) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                SessionManagementCard(onSessionManagementClick = onSessionManagementClick)
            }
        }
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
            currentUser = User(
                "1", "supervisor", "John Supervisor", UserRole.SUPERVISOR
            ),
            onTransaksiClick = {},
            onKeamananClick = {},
            onManajemenPINClick = {},
            onSessionManagementClick = {},
            onLogoutClick = {},
            dialogState = DialogState(),
            dismissDialog = {}
        )
    }
}
