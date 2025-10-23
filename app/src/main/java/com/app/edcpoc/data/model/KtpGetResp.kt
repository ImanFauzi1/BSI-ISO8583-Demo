package com.app.edcpoc.data.model

data class KtpGetResp(
    val status: Int,
    val message: String,
    val data: KtpGetData
)

data class KtpGetData(
    val error: Boolean,
    val ktp: GetKtp
)

data class GetKtp(
    val agama: String,
    val alamat: String,
    val desa: String,
    val foto: String,
    val golonganDarah: String,
    val id_cif: String?,
    val jenisKelamin: String,
    val kecamatan: String,
    val kelurahan: String,
    val kewarganegaraan: String,
    val kodePos: String,
    val kota: String,
    val masaBerlaku: String,
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