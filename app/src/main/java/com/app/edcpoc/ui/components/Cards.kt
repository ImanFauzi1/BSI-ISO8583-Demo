package com.app.edcpoc.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.app.edcpoc.R
import com.app.edcpoc.utils.Constants.commandValue

@Composable
fun TransaksiCard(onTransaksiClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFBBDEFB),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.transaksi_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionButton(
                    text = stringResource(R.string.btn_transaksi),
                    icon = Icons.Default.Star,
                    modifier = Modifier.weight(1f),
                    onClick = onTransaksiClick
                )
                ActionButton(
                    text = stringResource(R.string.btn_reissue_pin),
                    icon = Icons.Default.Lock,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun KeamananCard(onKeamananClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color(0xFFBBDEFB),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.keamanan_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionButton(
                    text = stringResource(R.string.btn_logon),
                    icon = Icons.Default.Lock,
                    modifier = Modifier.weight(1f),
                    iconColor = Color(0xFFFFC107),
                    onClick = {
                        commandValue = "logon"
                        onKeamananClick()
                    }
                )
                ActionButton(
                    text = stringResource(R.string.btn_logoff),
                    icon = Icons.Default.Close,
                    modifier = Modifier.weight(1f),
                    iconColor = Color(0xFF8D6E63),
                    onClick = {
                        commandValue = "logoff"
                        onKeamananClick()
                    }
                )
            }
        }
    }
}

@Composable
fun ManajemenPINCard(onManajemenPINClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color(0xFFBBDEFB),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.manajemen_pin_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionButton(
                    text = stringResource(R.string.btn_create_pin),
                    icon = Icons.Default.Add,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        commandValue = "createPIN"
                        onManajemenPINClick()
                    }
                )
                ActionButton(
                    text = stringResource(R.string.btn_change_pin),
                    icon = Icons.Default.Lock,
                    modifier = Modifier.weight(1f),
                    iconColor = Color(0xFFFFC107),
                    onClick = {
                        commandValue = "changePIN"
                        onManajemenPINClick()
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            ActionButton(
                text = stringResource(R.string.btn_verification_pin),
                icon = Icons.Default.Check,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    commandValue = "verifyPIN"
                    onManajemenPINClick()
                }
            )
        }
    }
}

@Composable
fun SessionManagementCard(onSessionManagementClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = Color(0xFFBBDEFB),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "SESSION MANAGEMENT",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionButton(
                    text = "Start Date",
                    icon = Icons.Default.DateRange,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        commandValue = "startDate"
                        onSessionManagementClick()
                    }
                )
                ActionButton(
                    text = "Close Date",
                    icon = Icons.Default.DateRange,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        commandValue = "closeDate"
                        onSessionManagementClick()
                    }
                )
            }
        }
    }
}
