package com.app.edcpoc.data.model

data class Pin(
    val id: String,
    val cardNumber: String,
    val pinValue: String, // Encrypted
    val createdAt: Long,
    val createdBy: String, // User ID
    val status: PinStatus,
    val lastUsed: Long? = null,
    val attempts: Int = 0
)

data class PinRequest(
    val cardNumber: String,
    val pinType: PinType,
    val reason: String? = null,
    val requestedBy: String // User ID
)

data class PinResponse(
    val success: Boolean,
    val message: String,
    val pin: Pin? = null
)

enum class PinStatus {
    ACTIVE,
    BLOCKED,
    EXPIRED,
    PENDING_APPROVAL
}

enum class PinType {
    CREATE,
    CHANGE,
    REISSUE,
    VERIFICATION
}

data class PinVerification(
    val cardNumber: String,
    val pinValue: String,
    val verifiedBy: String, // User ID
    val verifiedAt: Long,
    val success: Boolean
)
