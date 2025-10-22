package com.app.edcpoc.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.edcpoc.data.model.KtpDataModel

@Composable
fun KtpDataDialog(
    data: KtpDataModel,
    onClose: () -> Unit,
    onSubmit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Data KTP") },
        text = {
            Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                Text("NIK: ${data.nik}")
                Text("Nama: ${data.nama}")
                Text("Jenis Kelamin: ${data.jenisKelamin}")
                Text("Tempat Lahir: ${data.tempatLahir}")
                Text("Tanggal Lahir: ${data.tanggalLahir}")
                Text("Alamat: ${data.alamat}")
                Text("RT: ${data.rt}")
                Text("RW: ${data.rw}")
                Text("Kelurahan: ${data.kelurahan}")
                Text("Kecamatan: ${data.kecamatan}")
                Text("Kota: ${data.kota}")
                Text("Provinsi: ${data.provinsi}")
                Text("Golongan Darah: ${data.golonganDarah}")
                Text("Agama: ${data.agama}")
                Text("Status: ${data.statusPerkawinan}")
                Text("Pekerjaan: ${data.pekerjaan}")
                Text("Kewarganegaraan: ${data.kewarganegaraan}")
                // Field lain jika perlu
            }
        },
        confirmButton = {
            Button(onClick = onSubmit, modifier = Modifier.padding(end = 8.dp)) {
                Text("Submit")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onClose) {
                Text("Close")
            }
        }
    )
}
