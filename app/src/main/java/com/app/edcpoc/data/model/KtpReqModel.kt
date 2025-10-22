package com.app.edcpoc.data.model

import kotlinx.serialization.Serializable


@Serializable
data class KtpReq(
    val statusPerkawinan: String? = null,
    val pekerjaan: String? = null,
    val tandaTangan: String? = null,
    val sidikJari: String? = null,
    val provinsi: String? = null,
    val tempatLahir: String? = null,
    val jenisKelamin: String? = null,
    val rt: String? = null,
    val rw: String? = null,
    val kecamatan: String? = null,
    val kota: String? = null,
    val tanggalLahir: String? = null,
    val kelurahan: String? = null,
    val nama: String? = null,
    val agama: String? = null,
    val nik: String? = null,
    val kodePos: String? = null,
    val kewarganegaraan: String? = null,
    val golonganDarah: String? = null,
    val masaBerlaku: String? = null,
    val alamat: String? = null,
    val foto: String? = null,
    val desa: String? = null,
    val fingerprintVerification: String? = null,
    val faceRecognitionVerification: String? = null,
    val FaceRecognitionSimilarity: Double? = null,
    val reason: String? = null,
)

@Serializable
data class AmountRequest(
    val amount: Int
)

@Serializable
data class FaceCompareRequest(
    val ImageA: String,
    val ImageB: String
)

