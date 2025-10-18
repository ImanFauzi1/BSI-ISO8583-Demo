package com.app.edcpoc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.edcpoc.ui.theme.EdcpocTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePinScreen(
    onBackClick: () -> Unit = {},
    onPinChanged: (String) -> Unit = {}
) {
    var cardNumber by remember { mutableStateOf("") }
    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var showOldPin by remember { mutableStateOf(false) }
    var showNewPin by remember { mutableStateOf(false) }
    var showConfirmPin by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Header
        TopAppBar(
            title = { Text("Change PIN") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Change PIN Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Change PIN",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Card Number Field
                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = { cardNumber = it },
                        label = { Text("Card Number") },
                        leadingIcon = {
                            Icon(Icons.Filled.CreditCard, contentDescription = "Card Number")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    
                    // Old PIN Field
                    OutlinedTextField(
                        value = oldPin,
                        onValueChange = { oldPin = it },
                        label = { Text("Old PIN") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = "Old PIN")
                        },
                        trailingIcon = {
                            IconButton(onClick = { showOldPin = !showOldPin }) {
                                Icon(
                                    imageVector = if (showOldPin) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showOldPin) "Hide PIN" else "Show PIN"
                                )
                            }
                        },
                        visualTransformation = if (showOldPin) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true
                    )
                    
                    // New PIN Field
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { newPin = it },
                        label = { Text("New PIN") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = "New PIN")
                        },
                        trailingIcon = {
                            IconButton(onClick = { showNewPin = !showNewPin }) {
                                Icon(
                                    imageVector = if (showNewPin) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showNewPin) "Hide PIN" else "Show PIN"
                                )
                            }
                        },
                        visualTransformation = if (showNewPin) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true
                    )
                    
                    // Confirm PIN Field
                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = { confirmPin = it },
                        label = { Text("Confirm New PIN") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = "Confirm PIN")
                        },
                        trailingIcon = {
                            IconButton(onClick = { showConfirmPin = !showConfirmPin }) {
                                Icon(
                                    imageVector = if (showConfirmPin) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showConfirmPin) "Hide PIN" else "Show PIN"
                                )
                            }
                        },
                        visualTransformation = if (showConfirmPin) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true
                    )
                    
                    // Error Message
                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            color = Color(0xFFD32F2F),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                    
                    // Success Message
                    if (successMessage.isNotEmpty()) {
                        Text(
                            text = successMessage,
                            color = Color(0xFF4CAF50),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                    
                    // Change PIN Button
                    Button(
                        onClick = {
                            if (cardNumber.isNotEmpty() && oldPin.isNotEmpty() && newPin.isNotEmpty() && confirmPin.isNotEmpty()) {
                                if (newPin == confirmPin) {
                                    if (newPin.length == 6) {
                                        isLoading = true
                                        errorMessage = ""
                                        successMessage = ""
                                        
                                        // Simulate PIN change
                                        onPinChanged(newPin)
                                        
                                        isLoading = false
                                        successMessage = "PIN changed successfully"
                                        
                                        // Clear form
                                        cardNumber = ""
                                        oldPin = ""
                                        newPin = ""
                                        confirmPin = ""
                                    } else {
                                        errorMessage = "PIN must be 6 digits"
                                    }
                                } else {
                                    errorMessage = "New PIN and confirm PIN do not match"
                                }
                            } else {
                                errorMessage = "Please fill in all fields"
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = !isLoading && cardNumber.isNotEmpty() && oldPin.isNotEmpty() && newPin.isNotEmpty() && confirmPin.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Change PIN")
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChangePinScreenPreview() {
    EdcpocTheme {
        ChangePinScreen()
    }
}
