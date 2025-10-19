package com.idpay.victoriapoc.utils.IsoManagement

import com.app.edcpoc.utils.IsoManager.Iso8583Packer
import com.app.edcpoc.utils.LogUtils
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

object IsoBuilder {
    private val TAG = "IsoBuilder"

    fun createRequest(mti: String, data: Map<Int, String?>): ByteArray {
        val spec = IsoRepository.specs[mti]
            ?: throw IllegalArgumentException("Spec untuk MTI $mti tidak ditemukan")

        val combineSpec = mutableMapOf<Int, String>().apply {
            spec.requiredFields.forEach { field ->
                data[field]?.let { put(field, it) }
                    ?: throw IllegalArgumentException("Field $field wajib diisi")
            }
            spec.optionalFields.forEach { field ->
                data[field]?.let { put(field, it) }
            }
        }
        LogUtils.i(TAG, "ISO Spec Data: $combineSpec")

        val packed = Iso8583Packer.pack(mti, combineSpec)
        println("Packed ISO8583 (HEX): " + packed.joinToString("") { "%02X".format(it) })

        return packed
//        return ISOMsg().apply {
//            packager = ISO87BPackager()
//            setMTI(mti)
//            spec.requiredFields.forEach { field ->
//                data[field]?.let { set(field, it) }
//                    ?: throw IllegalArgumentException("Field $field wajib diisi")
//            }
//            spec.optionalFields.forEach { field ->
//                data[field]?.let { set(field, it) }
//            }
//        }
    }
}