package com.app.edcpoc.data.model

data class Transaction(
    val id: String,
    val cardNumber: String,
    val amount: Double,
    val type: TransactionType,
    val status: TransactionStatus,
    val timestamp: Long,
    val processedBy: String, // User ID
    val terminalId: String,
    val reference: String? = null
)

data class TransactionRequest(
    val cardNumber: String,
    val amount: Double,
    val type: TransactionType,
    val pin: String? = null
)

data class TransactionResponse(
    val success: Boolean,
    val transaction: Transaction? = null,
    val message: String
)

enum class TransactionType {
    SALE,
    REFUND,
    VOID,
    PREAUTH,
    COMPLETION
}

enum class TransactionStatus {
    PENDING,
    APPROVED,
    DECLINED,
    VOIDED,
    REFUNDED
}
