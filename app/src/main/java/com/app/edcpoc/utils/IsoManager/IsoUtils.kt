package com.idpay.victoriapoc.utils.IsoManagement

import com.app.edcpoc.utils.Constants.pinBlockConfirm
import com.app.edcpoc.utils.Constants.pinBlockOwn
import com.app.edcpoc.utils.Constants.pos_entrymode
import com.app.edcpoc.utils.Constants.track2data
import com.app.edcpoc.utils.CoreUtils.generateUniqueStan
import com.app.edcpoc.utils.LogUtils

object IsoUtils {
    private val TAG = "IsoUtils"

    fun isoStartEndDate(): ByteArray? {
        try {
            val startEndDate = mapOf(
                3 to "910000",
                11 to generateUniqueStan().padStart(6, '0'),
                22 to pos_entrymode,
                24 to "831",
                35 to track2data.padStart(37, '0'),
                41 to "TERM0001",
                42 to "ATM00010",
                52 to pinBlockOwn
            )
            LogUtils.i(TAG, "ISO Start/End Date Data: $startEndDate")

            return IsoBuilder.createRequest("0800", startEndDate)
        } catch (e: Exception) {
            LogUtils.e(TAG, "Error creating ISO Start/End Date message", e)
            return null
        }
    }

//    fun logon(): ISOMsg? {
//        val logonData = mapOf(
//            3 to "810000",
//            11 to generateUniqueStan(),
//            22 to pos_entrymode,
//            24 to "831",
//            35 to track2data,
//            41 to "TERM0001",
//            42 to "ATM00010",
//            52 to pinBlockConfirm
//        )
//        LogUtils.i(TAG, "ISO Logon Data: $logonData")
//
//        val msg = IsoBuilder.createRequest("0800", logonData)
//        LogUtils.i(TAG, "ISO Logon Message: ${String(msg.pack())}")
//        return IsoClient.sendMessage(msg)
//    }
}