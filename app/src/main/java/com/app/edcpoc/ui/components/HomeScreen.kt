package com.app.edcpoc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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

@Composable
fun EDCHomeScreen(
    currentUser: User,
    onTransaksiClick: () -> Unit,
    onKeamananClick: () -> Unit,
    onManajemenPINClick: () -> Unit,
    onSessionManagementClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        HeaderSection(
            currentUser = currentUser,
            onLogoutClick = onLogoutClick
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TransaksiCard(onTransaksiClick = onTransaksiClick)
            KeamananCard(onKeamananClick = onKeamananClick)
            ManajemenPINCard(onManajemenPINClick = onManajemenPINClick)
            if (currentUser.role == UserRole.SUPERVISOR) {
                SessionManagementCard(onSessionManagementClick = onSessionManagementClick)
            }
        }
        FooterSection()
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
            onLogoutClick = {}
        )
    }
}
