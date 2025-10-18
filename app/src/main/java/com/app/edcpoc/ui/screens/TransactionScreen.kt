package com.app.edcpoc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Payment
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
import com.app.edcpoc.data.model.TransactionType
import com.app.edcpoc.ui.theme.EdcpocTheme

@Composable
fun TransactionScreen(
    onBackClick: () -> Unit = {},
    onTransactionSuccess: (com.app.edcpoc.data.model.Transaction) -> Unit = {}
) {
    var cardNumber by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var showPin by remember { mutableStateOf(false) }
    var selectedTransactionType by remember { mutableStateOf(TransactionType.SALE) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Header
        HeaderSection(
            title = "Transaction",
            onBackClick = onBackClick
        )
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Transaction Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Transaction Details",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Transaction Type Selection
                    Text(
                        text = "Transaction Type",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF424242)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TransactionType.values().forEach { type ->
                            FilterChip(
                                onClick = { selectedTransactionType = type },
                                label = { Text(type.name) },
                                selected = selectedTransactionType == type,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Card Number Field
                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = { cardNumber = it },
                        label = { Text("Card Number") },
                        leadingIcon = {
                            Icon(Icons.Default.CreditCard, contentDescription = "Card Number")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    
                    // Amount Field
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Amount") },
                        leadingIcon = {
                            Icon(Icons.Default.AttachMoney, contentDescription = "Amount")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    
                    // PIN Field
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { pin = it },
                        label = { Text("PIN") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = "PIN")
                        },
                        trailingIcon = {
                            IconButton(onClick = { showPin = !showPin }) {
                                Icon(
                                    imageVector = if (showPin) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showPin) "Hide PIN" else "Show PIN"
                                )
                            }
                        },
                        visualTransformation = if (showPin) VisualTransformation.None else PasswordVisualTransformation(),
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
                    
                    // Process Transaction Button
                    Button(
                        onClick = {
                            if (cardNumber.isNotEmpty() && amount.isNotEmpty() && pin.isNotEmpty()) {
                                isLoading = true
                                errorMessage = ""
                                successMessage = ""
                                
                                // Simulate transaction processing
                                // In real app, this would call the transaction repository
                                onTransactionSuccess(
                                    com.app.edcpoc.data.model.Transaction(
                                        id = System.currentTimeMillis().toString(),
                                        cardNumber = cardNumber,
                                        amount = amount.toDoubleOrNull() ?: 0.0,
                                        type = selectedTransactionType,
                                        status = com.app.edcpoc.data.model.TransactionStatus.APPROVED,
                                        timestamp = System.currentTimeMillis(),
                                        processedBy = "current_user",
                                        terminalId = "EDC-2024-001"
                                    )
                                )
                                
                                isLoading = false
                                successMessage = "Transaction processed successfully"
                                
                                // Clear form
                                cardNumber = ""
                                amount = ""
                                pin = ""
                            } else {
                                errorMessage = "Please fill in all fields"
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = !isLoading && cardNumber.isNotEmpty() && amount.isNotEmpty() && pin.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Payment, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Process Transaction")
                        }
                    }
                }
            }
            
            // Recent Transactions Card
            RecentTransactionsCard()
        }
    }
}

@Composable
fun RecentTransactionsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Recent Transactions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Mock recent transactions
            val recentTransactions = listOf(
                TransactionItem("****1234", "SALE", "Rp 50,000", "10:30 AM", Color(0xFF4CAF50)),
                TransactionItem("****5678", "REFUND", "Rp 25,000", "09:15 AM", Color(0xFFFF9800)),
                TransactionItem("****9012", "SALE", "Rp 100,000", "08:45 AM", Color(0xFF4CAF50))
            )
            
            recentTransactions.forEach { transaction ->
                TransactionRow(transaction)
                if (transaction != recentTransactions.last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun TransactionRow(transaction: TransactionItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = transaction.cardNumber,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF212121)
            )
            Text(
                text = "${transaction.type} • ${transaction.time}",
                fontSize = 12.sp,
                color = Color(0xFF757575)
            )
        }
        
        Text(
            text = transaction.amount,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = transaction.statusColor
        )
    }
}

data class TransactionItem(
    val cardNumber: String,
    val type: String,
    val amount: String,
    val time: String,
    val statusColor: Color
)

@Preview(showBackground = true)
@Composable
fun TransactionScreenPreview() {
    EdcpocTheme {
        TransactionScreen()
    }
}
