package com.idpay.victoriapoc.utils.IsoManagement

data class ISOMessageSpec(
    val mti: String,
    val requiredFields: List<Int>,
    val optionalFields: List<Int> = listOf()
)

object IsoRepository {
    val specs = mapOf(
        "0800" to ISOMessageSpec(
            mti = "0800",
            requiredFields = listOf(3, 11, 24, 35, 41, 42, 52)
        ),
        "0100" to ISOMessageSpec(
            mti = "0100",
            requiredFields = listOf(3, 11, 24, 35, 41, 42, 52)
        ),
    )
}