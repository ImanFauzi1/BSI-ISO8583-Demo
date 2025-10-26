package com.app.edcpoc.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.app.edcpoc.R
import com.app.edcpoc.utils.Constants.CHANGE_PIN
import com.app.edcpoc.utils.Constants.CREATE_PIN
import com.app.edcpoc.utils.Constants.END_DATE
import com.app.edcpoc.utils.Constants.FACE_RECOGNIZE
import com.app.edcpoc.utils.Constants.KTP_READ
import com.app.edcpoc.utils.Constants.LOGOFF
import com.app.edcpoc.utils.Constants.LOGON
import com.app.edcpoc.utils.Constants.MANUAL_KTP_READ
import com.app.edcpoc.utils.Constants.REISSUE_PIN
import com.app.edcpoc.utils.Constants.START_DATE
import com.app.edcpoc.utils.Constants.VERIFY_PIN
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
fun SecurityCard(onSecurityClick: () -> Unit) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = Color(0xFFBBDEFB),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "SECURITY",
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
                text = "Logon",
                icon = Icons.Default.Aod,
                modifier = Modifier.weight(1f),
                iconColor = Color(0xFFBBDEFB),
                onClick = {
                    commandValue = LOGON
                    onSecurityClick()
                }
            )
            ActionButton(
                text = "Logoff",
                icon = Icons.Default.Aod,
                modifier = Modifier.weight(1f),
                iconColor = Color(0xFFBBDEFB),
                onClick = {
                    commandValue = LOGOFF
                    onSecurityClick()
                }
            )
        }
    }
}

@Composable
fun EnrollmentCard(onEnrollmentClick: (String) -> Unit) {
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
                text = "Enrollment",
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
                text = "KTP Read",
                icon = Icons.Default.AddCard,
                modifier = Modifier.weight(1f),
                iconColor = Color(0xFF8D6E63),
                onClick = {
                    onEnrollmentClick(KTP_READ)
                }
            )
            ActionButton(
                text = "Manual KTP Read",
                icon = Icons.Default.AddCard,
                modifier = Modifier.weight(1f),
                iconColor = Color(0xFF8D6E63),
                onClick = {
                    onEnrollmentClick(MANUAL_KTP_READ)
                }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActionButton(
                text = "Face Recognize",
                icon = Icons.Default.AssignmentInd,
                modifier = Modifier.weight(1f),
                iconColor = Color(0xFF8D6E63),
                onClick = {
                    onEnrollmentClick(FACE_RECOGNIZE)
                }
            )
        }
    }

}

@Composable
fun ManajemenPINCard(onManajemenPINClick: () -> Unit) {
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
                    commandValue = CREATE_PIN
                    onManajemenPINClick()
                }
            )
            ActionButton(
                text = stringResource(R.string.btn_change_pin),
                icon = Icons.Default.Lock,
                modifier = Modifier.weight(1f),
                iconColor = Color(0xFFFFC107),
                onClick = {
                    commandValue = CHANGE_PIN
                    onManajemenPINClick()
                }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActionButton(
                text = stringResource(R.string.btn_reissue_pin),
                icon = Icons.Default.Lock,
                modifier = Modifier.weight(1f),
                onClick = {
                    commandValue = REISSUE_PIN
                    onManajemenPINClick()
                }
            )
            ActionButton(
                text = stringResource(R.string.btn_verification_pin),
                icon = Icons.Default.Check,
                modifier = Modifier.weight(1f),
                onClick = {
                    commandValue = VERIFY_PIN
                    onManajemenPINClick()
                }
            )
        }
    }
}

@Composable
fun SessionManagementCard(onSessionManagementClick: () -> Unit) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
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
                    commandValue = START_DATE
                    onSessionManagementClick()
                }
            )
            ActionButton(
                text = "Close Date",
                icon = Icons.Default.DateRange,
                modifier = Modifier.weight(1f),
                onClick = {
                    commandValue = END_DATE
                    onSessionManagementClick()
                }
            )
        }
    }
}
