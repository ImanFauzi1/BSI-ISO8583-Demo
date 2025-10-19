package com.app.edcpoc.utils.IsoManager

import com.app.edcpoc.utils.LogUtils
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import com.app.edcpoc.utils.Constants.tpdu
import com.zcs.sdk.util.StringUtils

object Iso8583Packer {
    private val TAG = "Iso8583Packer"

    private fun convertBitmapToHex(bitmap: BooleanArray): String {
        val sb = StringBuilder()
        var i = 0
        while (i < bitmap.size) {
            var nibble = 0

            for (j in 0 until 4) {
                nibble = (nibble shl 1) or if (bitmap[i + j]) 1 else 0
            }
            sb.append(Integer.toHexString(nibble))
            i += 4
        }

        return sb.toString().uppercase()
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun pack(mti: String, fields: Map<Int, String?>): ByteArray {
        val bitmap = BooleanArray(64)
        for (id in fields.keys) {
            if (id in 1..64) bitmap[id - 1] = true
        }

        LogUtils.e(TAG, "Bitmap: ${bitmap.joinToString("") { if (it) "1" else "0" }}")

        val bitmapHex = convertBitmapToHex(bitmap)
        LogUtils.e(TAG, "Bitmap Hex: $bitmapHex")

//        build message
        val body = StringBuilder()
        body.append(mti)
        body.append(bitmapHex)
        fields.toSortedMap().forEach { (_, v) ->
            body.append(v)
        }

        val isoBytes = body.toString().toByteArray(StandardCharsets.US_ASCII)

//        append tpdu
        val tpduBytes = (tpdu?.let { StringUtils.convertHexToBytes(it) } ?: ByteArray(0))
        val totalLength = isoBytes.size + tpduBytes.size

        val finalMsg = ByteBuffer.allocate(totalLength)
            .put(tpduBytes)
            .put(isoBytes)
            .array()

        // prevend 2-byte big endian length
        val buf = ByteBuffer.allocate(totalLength + 2)
        buf.putShort(totalLength.toShort())
        buf.put(finalMsg)

        val result = buf.array()
        LogUtils.i(TAG, "Final packed ISO (HEX): ${result.joinToString("") { "%02X".format(it) }}")
        return result

    }
    fun unpack(data: ByteArray): Map<String, Any> {
        val buffer = ByteBuffer.wrap(data)

        // read 2-byte length
        val length = buffer.short.toInt() and 0xFFFF

        // TPDU (5 bytes)
        val tpduBytes = ByteArray(5)
        buffer.get(tpduBytes)
        val tpdu = tpduBytes.joinToString("") { "%02X".format(it) }

        // MTI (4 chars)
        val mtiBytes = ByteArray(4)
        buffer.get(mtiBytes)
        val mti = String(mtiBytes, StandardCharsets.US_ASCII)

        // Bitmap (16 hex chars → 8 bytes)
        val bitmapBytes = ByteArray(8)
        buffer.get(bitmapBytes)
        val bitmapHex = bitmapBytes.joinToString("") { "%02X".format(it) }

        // Parse bitmap bits
        val bitmapBits = bitmapBytes.joinToString("") {
            it.toInt().and(0xFF).toString(2).padStart(8, '0')
        }

        val activeBits = mutableListOf<Int>()
        bitmapBits.forEachIndexed { idx, c ->
            if (c == '1') activeBits.add(idx + 1)
        }

        // sisanya field data (raw)
        val remaining = ByteArray(length - 5 - 4 - 8)
        buffer.get(remaining)
        val fieldsRaw = String(remaining, StandardCharsets.US_ASCII)

        return mapOf(
            "length" to length,
            "tpdu" to tpdu,
            "mti" to mti,
            "bitmapHex" to bitmapHex,
            "activeFields" to activeBits,
            "rawFields" to fieldsRaw
        )
    }
}