package com.app.edcpoc.data.repository

import com.app.edcpoc.data.model.*
import kotlinx.coroutines.delay

class TransactionRepository {
    
    private val transactions = mutableListOf<Transaction>()
    
    suspend fun processTransaction(request: TransactionRequest, processedBy: String): TransactionResponse {
        // Simulate network delay
        delay(2000)
        
        // Check if daily session is active
        val sessionRepo = SessionRepository()
        if (!sessionRepo.isDailySessionActive()) {
            return TransactionResponse(false, null, "Daily session is not active")
        }
        
        // Validate PIN if required
        if (request.pin != null) {
            val pinRepo = PinRepository()
            val pinResponse = pinRepo.verifyPin(request.cardNumber, request.pin, processedBy)
            if (!pinResponse.success) {
                return TransactionResponse(false, null, "PIN verification failed: ${pinResponse.message}")
            }
        }
        
        val transaction = Transaction(
            id = System.currentTimeMillis().toString(),
            cardNumber = request.cardNumber,
            amount = request.amount,
            type = request.type,
            status = TransactionStatus.APPROVED, // In real app, this would be determined by host response
            timestamp = System.currentTimeMillis(),
            processedBy = processedBy,
            terminalId = "EDC-2024-001",
            reference = generateReference()
        )
        
        transactions.add(transaction)
        
        // Update daily session transaction count
        // In real app, this would be handled by the session repository
        
        return TransactionResponse(true, transaction, "Transaction processed successfully")
    }
    
    suspend fun voidTransaction(transactionId: String, voidedBy: String): TransactionResponse {
        // Simulate network delay
        delay(1500)
        
        val transaction = transactions.find { it.id == transactionId }
        if (transaction == null) {
            return TransactionResponse(false, null, "Transaction not found")
        }
        
        if (transaction.status != TransactionStatus.APPROVED) {
            return TransactionResponse(false, null, "Transaction cannot be voided")
        }
        
        val voidedTransaction = transaction.copy(status = TransactionStatus.VOIDED)
        val index = transactions.indexOf(transaction)
        transactions[index] = voidedTransaction
        
        return TransactionResponse(true, voidedTransaction, "Transaction voided successfully")
    }
    
    suspend fun refundTransaction(transactionId: String, refundedBy: String): TransactionResponse {
        // Simulate network delay
        delay(1500)
        
        val transaction = transactions.find { it.id == transactionId }
        if (transaction == null) {
            return TransactionResponse(false, null, "Transaction not found")
        }
        
        if (transaction.status != TransactionStatus.APPROVED) {
            return TransactionResponse(false, null, "Transaction cannot be refunded")
        }
        
        val refundedTransaction = transaction.copy(status = TransactionStatus.REFUNDED)
        val index = transactions.indexOf(transaction)
        transactions[index] = refundedTransaction
        
        return TransactionResponse(true, refundedTransaction, "Transaction refunded successfully")
    }
    
    fun getTransactionsForCard(cardNumber: String): List<Transaction> {
        return transactions.filter { it.cardNumber == cardNumber }
    }
    
    fun getTransactionsByUser(userId: String): List<Transaction> {
        return transactions.filter { it.processedBy == userId }
    }
    
    fun getTodayTransactions(): List<Transaction> {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())
        return transactions.filter { 
            java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(java.util.Date(it.timestamp)) == today
        }
    }
    
    private fun generateReference(): String {
        return "TXN${System.currentTimeMillis().toString().takeLast(8)}"
    }
}
