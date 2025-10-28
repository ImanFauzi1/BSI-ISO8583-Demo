package com.idpay.victoriapoc.utils.IsoManagement

import android.content.Context
import com.app.edcpoc.PreferenceManager
import com.app.edcpoc.utils.IsoManager.Iso8583Packer
import com.app.edcpoc.utils.LogUtils
import com.zcs.sdk.util.StringUtils
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.Socket


object IsoClient {
    private val TAG = "ISOClient"

    fun connectSocket(hexToSend: String): String? {
        try {
            val socket = Socket("10.0.117.75", 30200)
            val dataOutputStream = DataOutputStream(socket.getOutputStream())
            dataOutputStream.write(hexStringToByteArray(hexToSend))
            val dataInputStream = DataInputStream(socket.getInputStream())
            val buffer = ByteArray(65535)
            val x = dataInputStream.read(buffer)
            val subArray = ByteArray(x)
            System.arraycopy(buffer, 0, subArray, 0, x)
            socket.close()
            return StringUtils.convertBytesToHex(subArray)
        } catch (e: IOException) {
            return e.stackTraceToString()
        }
    }
    fun hexStringToByteArray(s: String): ByteArray {
        val len = s.length
        val data = ByteArray(len / 2)

        var i = 0
        while (i < len) {
            data[i / 2] =
                ((s.get(i).digitToIntOrNull(16) ?: -1 shl 4) + s.get(i + 1).digitToIntOrNull(16)!!
                    ?: -1).toByte()
            i += 2
        }

        return data
    }
    fun sendMessage(context: Context, isoMsg: ByteArray, onResult: (Map<String, Any>?, error: String?) -> Unit) {
        Thread {
            var socket: Socket? = null
            var outStream: DataOutputStream? = null
            var inStream: DataInputStream? = null
            try {
                val host = PreferenceManager.getHost(context)
                val portStr = PreferenceManager.getHostPort(context)
                val port = portStr?.toIntOrNull()
                if (host.isNullOrEmpty() || port == null) {
                    onResult(null, "Host or port not set in settings")
                    return@Thread
                }
                socket  = Socket(host, port)
                socket.soTimeout = 10_000
                outStream = DataOutputStream(socket.getOutputStream())
                inStream = DataInputStream(socket.getInputStream())

                outStream.write(isoMsg)
                outStream.flush()

                // Read response length (2 bytes) then data
                val len = inStream.readUnsignedShort()
                val responseBytes = ByteArray(len)
                inStream.readFully(responseBytes)

                LogUtils.e(TAG, "Received response bytes: ${responseBytes.size}")
                LogUtils.e(TAG, "Response bytes (HEX): ${responseBytes.joinToString("") { "%02X".format(it) }}")

                // Untuk kebutuhan parsing string ISO, buang TPDU (5 bytes) dari responseBytes
                val stringIso = if (responseBytes.size > 5) {
                    val isoString = responseBytes.copyOfRange(5, responseBytes.size).toString(Charsets.US_ASCII)
                    isoString
                } else {
                    ""
                }

                onResult(
                    mapOf(
                        "rawBytes" to responseBytes,
                        "hex" to responseBytes.joinToString("") { "%02X".format(it) },
                        "string" to stringIso,
                        "parsed" to (Iso8583Packer.unpack(responseBytes) ?: emptyMap<String, Any>())
                    ),
                    null
                )
            } catch (e: Exception) {
                onResult(null, e.printStackTrace().toString())
                LogUtils.e(TAG, "Error sending ISO message: ${e.message}")
                LogUtils.e(TAG, "Stacktrace: ${e.stackTraceToString()}")
            } finally {
                try { outStream?.close() } catch (_: Throwable) {}
                try { inStream?.close() } catch (_: Throwable) {}
                try { socket?.close() } catch (_: Throwable) {}
            }
        }.start()

    }

//    fun sendHttpMessage(msg: ByteArray) {
//        val mediaType = "application/octet-stream".toMediaTypeOrNull()
//        val body = RequestBody.create(mediaType, msg)
//
//        val request = Request.Builder().url(ISO_URL).post(body).build()
//
//        try {
//            val client = OkHttpClient()
//            val response = client.newCall(request).execute()
//
//            if (response.isSuccessful) {
//                val responseBody = response.body
//                LogUtils.d(TAG,  "HTTP ISO message sent successfully. Response: ${Gson().toJson(response.body)}")
//            } else {
//                LogUtils.d(TAG,  "Failed to send HTTP ISO message. Response code: ${response.code}")
//            }
//        } catch (e: Exception) {
//            LogUtils.d(TAG,  "Error sending HTTP ISO message: ${e.message}")
//        }
//
//    }
}