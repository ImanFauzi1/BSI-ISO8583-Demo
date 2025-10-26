package com.app.edcpoc.data.model

import kotlinx.serialization.Serializable

data class LogResponse(
    val code: Int,
    val message: String,
    val data: Any
)

@Serializable
data class LogTransactionRequest(
    val transaction_type: String,
    val pos_entrymode: String,
    val card_number: String,
    val status: String,
    val response_code_description: String,
    val remarks: String
)
