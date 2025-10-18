package com.app.edcpoc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.edcpoc.ui.theme.EdcpocTheme

@Composable
fun ReissuePinScreen(
    onBackClick: () -> Unit = {},
    onPinReissued: (String) -> Unit = {}
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
            title = "Reissue PIN"
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
                // Simulate PIN reissue request
                onPinReissued(pin)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}





@Preview(showBackground = true)
@Composable
fun ReissuePinScreenPreview() {
    EdcpocTheme {
        ReissuePinScreen()
    }
}
