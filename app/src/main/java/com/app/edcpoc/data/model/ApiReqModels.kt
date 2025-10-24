package com.app.edcpoc.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SvpRequestBody(
    val pan: String,
    val sidikJari: String? = null,
)
