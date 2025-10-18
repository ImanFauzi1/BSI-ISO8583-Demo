package com.app.edcpoc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.edcpoc.ui.theme.EdcpocTheme

@Composable
fun CreatePinScreen(
    onBackClick: () -> Unit = {},
    onPinCreated: (String) -> Unit = {}
) {
    var pin by remember { mutableStateOf("") }
    var showPin by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Header Section
        HeaderSection(
            onBackClick = onBackClick,
            title = "Create PIN"
        )

        // PIN Input Card
        PinInputCard(
            pin = pin,
            showPin = showPin,
            onShowPinToggle = { showPin = !showPin }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Numeric Keypad
        NumericKeypad(
            onNumberClick = { number ->
                if (pin.length < 6) {
                    pin += number
                }
            },
            onBackspaceClick = {
                if (pin.isNotEmpty()) {
                    pin = pin.dropLast(1)
                }
            },
            onClearClick = {
                pin = ""
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Confirm Button
        ConfirmButton(
            enabled = pin.length == 6,
            isLoading = isLoading,
            onClick = {
                isLoading = true
                // Simulate API call
                onPinCreated(pin)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun HeaderSection(
    onBackClick: () -> Unit,
    title: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFF1976D2),
                shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Back Button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = Color(0xFFBBDEFB),
                        shape = CircleShape
                    )
                    .clickable { onBackClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Title
            Column {
                Text(
                    text = "+ Create PIN",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Buat PIN Baru",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun PinInputCard(
    pin: String,
    showPin: Boolean,
    onShowPinToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Masukkan PIN (6 Digit)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF424242)
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // PIN Input Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(6) { index ->
                    PinDot(
                        isFilled = index < pin.length,
                        digit = if (showPin && index < pin.length) pin[index].toString() else ""
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Show/Hide PIN Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onShowPinToggle() }
            ) {
                Icon(
                    imageVector = if (showPin) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (showPin) "Hide PIN" else "Show PIN",
                    tint = Color(0xFF1976D2),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tampilkan",
                    color = Color(0xFF1976D2),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun PinDot(
    isFilled: Boolean,
    digit: String
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(
                color = if (isFilled) Color(0xFFE3F2FD) else Color(0xFFF5F5F5),
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = if (isFilled) Color(0xFF1976D2) else Color(0xFFE0E0E0),
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (digit.isNotEmpty()) {
            Text(
                text = digit,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
        }
    }
}

@Composable
fun NumericKeypad(
    onNumberClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    onClearClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row 1: 1, 2, 3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KeypadButton("1", { onNumberClick("1") }, modifier = Modifier.weight(1f))
            KeypadButton("2", { onNumberClick("2") }, modifier = Modifier.weight(1f))
            KeypadButton("3", { onNumberClick("3") }, modifier = Modifier.weight(1f))
        }
        
        // Row 2: 4, 5, 6
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KeypadButton("4", { onNumberClick("4") }, modifier = Modifier.weight(1f))
            KeypadButton("5", { onNumberClick("5") }, modifier = Modifier.weight(1f))
            KeypadButton("6", { onNumberClick("6") }, modifier = Modifier.weight(1f))
        }
        
        // Row 3: 7, 8, 9
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KeypadButton("7", { onNumberClick("7") }, modifier = Modifier.weight(1f))
            KeypadButton("8", { onNumberClick("8") }, modifier = Modifier.weight(1f))
            KeypadButton("9", { onNumberClick("9") }, modifier = Modifier.weight(1f))
        }
        
        // Row 4: 0, Backspace, Clear
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KeypadButton("0", { onNumberClick("0") }, modifier = Modifier.weight(1f))
            KeypadButton("", onBackspaceClick, modifier = Modifier.weight(1f), icon = Icons.Default.Backspace)
            KeypadButton("C", onClearClick, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun KeypadButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(60.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE3F2FD)),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = Color(0xFF1976D2),
                modifier = Modifier.size(24.dp)
            )
        } else {
            Text(
                text = text,
                color = Color(0xFF1976D2),
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ConfirmButton(
    enabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        enabled = enabled && !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (enabled) Color(0xFF757575) else Color(0xFFE0E0E0)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Confirm",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "CONFIRM",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreatePinScreenPreview() {
    EdcpocTheme {
        CreatePinScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun PinInputCardPreview() {
    EdcpocTheme {
        PinInputCard(
            pin = "1234",
            showPin = false,
            onShowPinToggle = {}
        )
    }
}
