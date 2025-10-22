package com.app.edcpoc.data.model

import kotlinx.serialization.Serializable

@Serializable
data class KtpDataModel(
    val agama: String,
    val alamat: String,
    val desa: String,
    val error: Boolean,
    val foto: String,
    val golonganDarah: String,
    val jenisKelamin: String,
    val kecamatan: String,
    val kelurahan: String,
    val kewarganegaraan: String,
    val kodePos: String,
    val kota: String,
    val masaBerlaku: String,
    val message: String,
    val nama: String,
    val nik: String,
    val pekerjaan: String,
    val provinsi: String,
    val rt: String,
    val rw: String,
    val sidikJari: String,
    val statusPerkawinan: String,
    val tandaTangan: String,
    val tanggalLahir: String,
    val tempatLahir: String
)
