package com.app.edcpoc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.collectAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.edcpoc.data.model.UserRole
import com.app.edcpoc.ui.theme.EdcpocTheme
import com.app.edcpoc.ui.viewmodel.SessionViewModel

@Composable
fun SessionManagementScreen(
    currentUser: com.app.edcpoc.data.model.User,
    onBackClick: () -> Unit = {},
    sessionViewModel: SessionViewModel = viewModel()
) {
    val uiState by sessionViewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        sessionViewModel.updateSessionInfo()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Header
        HeaderSection(
            title = "Session Management",
            onBackClick = onBackClick
        )
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Current Status Card
            StatusCard(
                isDailySessionActive = uiState.isDailySessionActive,
                isUserLoggedIn = uiState.isUserLoggedIn,
                dailySession = uiState.dailySession,
                userSession = uiState.userSession
            )
            
            // Action Buttons based on role
            when (currentUser.role) {
                UserRole.SUPERVISOR -> {
                    SupervisorActions(
                        isDailySessionActive = uiState.isDailySessionActive,
                        isUserLoggedIn = uiState.isUserLoggedIn,
                        isLoading = uiState.isLoading,
                        onStartDate = { sessionViewModel.startDate(currentUser.id) },
                        onCloseDate = { sessionViewModel.closeDate(currentUser.id) },
                        onLogon = { sessionViewModel.logon(currentUser.id, currentUser.username, currentUser.role) },
                        onLogoff = { sessionViewModel.logoff() }
                    )
                }
                UserRole.OPERATOR -> {
                    OperatorActions(
                        isDailySessionActive = uiState.isDailySessionActive,
                        isUserLoggedIn = uiState.isUserLoggedIn,
                        isLoading = uiState.isLoading,
                        onLogon = { sessionViewModel.logon(currentUser.id, currentUser.username, currentUser.role) },
                        onLogoff = { sessionViewModel.logoff() }
                    )
                }
                UserRole.TELLER -> {
                    TellerActions(
                        isUserLoggedIn = uiState.isUserLoggedIn,
                        isLoading = uiState.isLoading,
                        onLogon = { sessionViewModel.logon(currentUser.id, currentUser.username, currentUser.role) },
                        onLogoff = { sessionViewModel.logoff() }
                    )
                }
            }
            
            // Error Message
            if (uiState.errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.errorMessage!!,
                            color = Color(0xFFD32F2F),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusCard(
    isDailySessionActive: Boolean,
    isUserLoggedIn: Boolean,
    dailySession: com.app.edcpoc.data.model.DailySession?,
    userSession: com.app.edcpoc.data.model.Session?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Current Status",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Daily Session Status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            color = if (isDailySessionActive) Color(0xFF4CAF50) else Color(0xFFF44336),
                            shape = RoundedCornerShape(6.dp)
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Daily Session: ${if (isDailySessionActive) "Active" else "Inactive"}",
                    fontSize = 14.sp,
                    color = Color(0xFF424242)
                )
            }
            
            // User Session Status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            color = if (isUserLoggedIn) Color(0xFF4CAF50) else Color(0xFFF44336),
                            shape = RoundedCornerShape(6.dp)
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "User Session: ${if (isUserLoggedIn) "Active" else "Inactive"}",
                    fontSize = 14.sp,
                    color = Color(0xFF424242)
                )
            }
            
            // Session Details
            if (dailySession != null) {
                Text(
                    text = "Date: ${dailySession.date}",
                    fontSize = 12.sp,
                    color = Color(0xFF757575)
                )
                dailySession.startTime?.let {
                    if (it > 0) {
                        Text(
                            text = "Started: ${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(dailySession.startTime))}",
                            fontSize = 12.sp,
                            color = Color(0xFF757575)
                        )
                    }
                }
            }
            
            if (userSession != null) {
                Text(
                    text = "User: ${userSession.username} (${userSession.role})",
                    fontSize = 12.sp,
                    color = Color(0xFF757575)
                )
            }
        }
    }
}

@Composable
fun SupervisorActions(
    isDailySessionActive: Boolean,
    isUserLoggedIn: Boolean,
    isLoading: Boolean,
    onStartDate: () -> Unit,
    onCloseDate: () -> Unit,
    onLogon: () -> Unit,
    onLogoff: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Supervisor Actions",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121)
        )
        
        // Start Date Button
        Button(
            onClick = onStartDate,
            enabled = !isDailySessionActive && !isLoading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50)
            )
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Start Date")
        }
        
        // Close Date Button
        Button(
            onClick = onCloseDate,
            enabled = isDailySessionActive && !isLoading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF44336)
            )
        ) {
            Icon(Icons.Default.Stop, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Close Date")
        }
        
        // Logon Button
        Button(
            onClick = onLogon,
            enabled = !isUserLoggedIn && !isLoading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2196F3)
            )
        ) {
            Icon(Icons.Default.Login, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logon")
        }
        
        // Logoff Button
        Button(
            onClick = onLogoff,
            enabled = isUserLoggedIn && !isLoading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF9800)
            )
        ) {
            Icon(Icons.Default.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logoff")
        }
    }
}

@Composable
fun OperatorActions(
    isDailySessionActive: Boolean,
    isUserLoggedIn: Boolean,
    isLoading: Boolean,
    onLogon: () -> Unit,
    onLogoff: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Operator Actions",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121)
        )
        
        if (!isDailySessionActive) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
            ) {
                Text(
                    text = "Daily session must be started by Supervisor before you can logon",
                    modifier = Modifier.padding(16.dp),
                    color = Color(0xFFE65100),
                    fontSize = 14.sp
                )
            }
        }
        
        // Logon Button
        Button(
            onClick = onLogon,
            enabled = isDailySessionActive && !isUserLoggedIn && !isLoading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2196F3)
            )
        ) {
            Icon(Icons.Default.Login, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logon")
        }
        
        // Logoff Button
        Button(
            onClick = onLogoff,
            enabled = isUserLoggedIn && !isLoading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF9800)
            )
        ) {
            Icon(Icons.Default.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logoff")
        }
    }
}

@Composable
fun TellerActions(
    isUserLoggedIn: Boolean,
    isLoading: Boolean,
    onLogon: () -> Unit,
    onLogoff: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Teller Actions",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121)
        )
        
        // Logon Button
        Button(
            onClick = onLogon,
            enabled = !isUserLoggedIn && !isLoading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2196F3)
            )
        ) {
            Icon(Icons.Default.Login, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logon")
        }
        
        // Logoff Button
        Button(
            onClick = onLogoff,
            enabled = isUserLoggedIn && !isLoading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF9800)
            )
        ) {
            Icon(Icons.Default.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logoff")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SessionManagementScreenPreview() {
    EdcpocTheme {
        SessionManagementScreen(
            currentUser = com.app.edcpoc.data.model.User(
                "1", "supervisor", "John Supervisor", com.app.edcpoc.data.model.UserRole.SUPERVISOR
            )
        )
    }
}
