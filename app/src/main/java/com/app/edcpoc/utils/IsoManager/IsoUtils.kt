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
                35 to (track2data?.padStart(37, '0') ?: "".padStart(37, '0')),
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

    fun isoLogonLogoff(): ByteArray? {
        try {
            val logonLogoff = mapOf(
                3 to "810000",
                11 to generateUniqueStan().padStart(6, '0'),
                22 to pos_entrymode,
                24 to "831",
                35 to (track2data?.padStart(37, '0') ?: "".padStart(37, '0')),
                41 to "TERM0001",
                42 to "ATM00010",
                52 to pinBlockOwn
            )
            LogUtils.i(TAG, "ISO logonlogoff Data: $logonLogoff")

            return IsoBuilder.createRequest("0800", logonLogoff)
        } catch (e: Exception) {
            LogUtils.e(TAG, "Error creating ISO logonlogoff message", e)
            return null
        }
    }

    fun isoReissuePIN(): ByteArray? {
        try {
            val reissuePin = mapOf(
                3 to "720000",
                11 to generateUniqueStan().padStart(6, '0'),
                22 to pos_entrymode,
                24 to "831",
                35 to (track2data?.padStart(37, '0') ?: "".padStart(37, '0')),
                41 to "TERM0001",
                42 to "ATM00010",
                48 to pinBlockConfirm,
                52 to pinBlockOwn
            )
            LogUtils.i(TAG, "ISO Reissue PIN Data: $reissuePin")

            return IsoBuilder.createRequest("0100", reissuePin)
        } catch (e: Exception) {
            LogUtils.e(TAG, "Error creating ISO Reissue PIN message", e)
            return null
        }
    }

    fun isoChangePIN(): ByteArray? {
        try {
            val changePin = mapOf(
                3 to "730000",
                11 to generateUniqueStan().padStart(6, '0'),
                22 to pos_entrymode,
                24 to "831",
                35 to (track2data?.padStart(37, '0') ?: "".padStart(37, '0')),
                41 to "TERM0001",
                42 to "ATM00010",
                48 to pinBlockConfirm,
                52 to pinBlockOwn
            )
            LogUtils.i(TAG, "ISO Change PIN Data: $changePin")

            return IsoBuilder.createRequest("0100", changePin)
        } catch (e: Exception) {
            LogUtils.e(TAG, "Error creating ISO Change PIN message", e)
            return null
        }
    }

    fun isoVerificationPIN(): ByteArray? {
        try {
            val verificationPin = mapOf(
                3 to "740000",
                11 to generateUniqueStan().padStart(6, '0'),
                22 to pos_entrymode,
                24 to "831",
                35 to (track2data?.padStart(37, '0') ?: "".padStart(37, '0')),
                41 to "TERM0001",
                42 to "ATM00010",
                52 to pinBlockOwn
            )
            LogUtils.i(TAG, "ISO Verification Pin Data: $verificationPin")

            return IsoBuilder.createRequest("0100", verificationPin)
        } catch (e: Exception) {
            LogUtils.e(TAG, "Error creating ISO Verification Pin message", e)
            return null
        }
    }
}