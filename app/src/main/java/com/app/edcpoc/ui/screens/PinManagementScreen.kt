package com.app.edcpoc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.edcpoc.data.model.UserRole
import com.app.edcpoc.ui.theme.EdcpocTheme

@Composable
fun PinManagementScreen(
    currentUser: com.app.edcpoc.data.model.User,
    onBackClick: () -> Unit = {},
    onCreatePinClick: () -> Unit = {},
    onChangePinClick: () -> Unit = {},
    onReissuePinClick: () -> Unit = {},
    onVerificationPinClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Header
        HeaderSection(
            title = "PIN Management",
            onBackClick = onBackClick
        )
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Role-based actions
            when (currentUser.role) {
                UserRole.SUPERVISOR -> {
                    SupervisorPinActions(
                        onCreatePinClick = onCreatePinClick,
                        onChangePinClick = onChangePinClick,
                        onReissuePinClick = onReissuePinClick,
                        onVerificationPinClick = onVerificationPinClick
                    )
                }
                UserRole.OPERATOR -> {
                    OperatorPinActions(
                        onCreatePinClick = onCreatePinClick,
                        onReissuePinClick = onReissuePinClick,
                        onVerificationPinClick = onVerificationPinClick
                    )
                }
                UserRole.TELLER -> {
                    TellerPinActions(
                        onChangePinClick = onChangePinClick,
                        onVerificationPinClick = onVerificationPinClick
                    )
                }
            }
        }
    }
}

@Composable
fun SupervisorPinActions(
    onCreatePinClick: () -> Unit,
    onChangePinClick: () -> Unit,
    onReissuePinClick: () -> Unit,
    onVerificationPinClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Supervisor PIN Actions",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121)
        )
        
        // Create PIN Card
        PinActionCard(
            title = "Create PIN",
            description = "Create new PIN for card",
            icon = Icons.Default.Add,
            iconColor = Color(0xFF4CAF50),
            onClick = onCreatePinClick
        )
        
        // Change PIN Card
        PinActionCard(
            title = "Change PIN",
            description = "Change existing PIN",
            icon = Icons.Default.Edit,
            iconColor = Color(0xFF2196F3),
            onClick = onChangePinClick
        )
        
        // Reissue PIN Card
        PinActionCard(
            title = "Reissue PIN",
            description = "Approve PIN reissue requests",
            icon = Icons.Default.Refresh,
            iconColor = Color(0xFFFF9800),
            onClick = onReissuePinClick
        )
        
        // Verification PIN Card
        PinActionCard(
            title = "Verification PIN",
            description = "Verify PIN for audit purposes",
            icon = Icons.Default.Verified,
            iconColor = Color(0xFF9C27B0),
            onClick = onVerificationPinClick
        )
    }
}

@Composable
fun OperatorPinActions(
    onCreatePinClick: () -> Unit,
    onReissuePinClick: () -> Unit,
    onVerificationPinClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Operator PIN Actions",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121)
        )
        
        // Create PIN Card
        PinActionCard(
            title = "Create PIN",
            description = "Input PIN creation request",
            icon = Icons.Default.Add,
            iconColor = Color(0xFF4CAF50),
            onClick = onCreatePinClick
        )
        
        // Reissue PIN Card
        PinActionCard(
            title = "Reissue PIN",
            description = "Submit PIN reissue request",
            icon = Icons.Default.Refresh,
            iconColor = Color(0xFFFF9800),
            onClick = onReissuePinClick
        )
        
        // Verification PIN Card
        PinActionCard(
            title = "Verification PIN",
            description = "Verify PIN for transactions",
            icon = Icons.Default.Verified,
            iconColor = Color(0xFF9C27B0),
            onClick = onVerificationPinClick
        )
    }
}

@Composable
fun TellerPinActions(
    onChangePinClick: () -> Unit,
    onVerificationPinClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Teller PIN Actions",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121)
        )
        
        // Change PIN Card
        PinActionCard(
            title = "Change PIN",
            description = "Help customer change PIN",
            icon = Icons.Default.Edit,
            iconColor = Color(0xFF2196F3),
            onClick = onChangePinClick
        )
        
        // Verification PIN Card
        PinActionCard(
            title = "Verification PIN",
            description = "Verify customer PIN",
            icon = Icons.Default.Verified,
            iconColor = Color(0xFF9C27B0),
            onClick = onVerificationPinClick
        )
    }
}

@Composable
fun PinActionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = Color(0xFF757575)
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = Color(0xFFBDBDBD),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PinManagementScreenPreview() {
    EdcpocTheme {
        PinManagementScreen(
            currentUser = com.app.edcpoc.data.model.User(
                "1", "supervisor", "John Supervisor", com.app.edcpoc.data.model.UserRole.SUPERVISOR
            )
        )
    }
}
