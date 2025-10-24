package com.app.edcpoc.data.model

import kotlinx.serialization.Serializable

@Serializable
data class KtpResp (
    val status: Int,
    val message: String,
    val data: KtpDataModel
)

@Serializable
data class FaceCompareTencentResponse(
    val status: Int,
    val message: String,
    val data: FaceCompareResult
)

@Serializable
data class FaceCompareResult(
    val Score: Double,
    val FaceModelVersion: String,
    val RequestId: String
)

@Serializable
data class SvpResponse(
    val status: Int,
    val message: String,
    val data: SvpData
)

@Serializable
data class SvpData(
    val id: Int?,
    val sidikJari: String?
)