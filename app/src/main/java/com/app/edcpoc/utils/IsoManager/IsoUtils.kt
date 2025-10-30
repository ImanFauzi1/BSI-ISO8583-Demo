package com.app.edcpoc.utils.IsoManager

import android.util.Log
import com.app.edcpoc.utils.Constants.commandValue
import com.app.edcpoc.utils.Constants.field48data
import com.app.edcpoc.utils.Constants.officerCardNum
import com.app.edcpoc.utils.Constants.pinBlockConfirm
import com.app.edcpoc.utils.Constants.pinBlockOwn
import com.app.edcpoc.utils.Constants.staticSN
import com.app.edcpoc.utils.Constants.track2data
import com.app.edcpoc.utils.CoreUtils.generateUniqueStan
import com.app.edcpoc.utils.LogUtils
import com.app.edcpoc.utils.Utility.simpleDateFormat
import com.idpay.victoriapoc.utils.IsoManagement.IsoBuilder
import com.zcs.sdk.util.StringUtils

object IsoUtils {
    private val TAG = "IsoUtils"
    private val NII = "0001"
    private val TID = "01000202"
    private val MID = "451000001000202"

    private fun generateBaseRequest(mti: String): Model8583Request {
        val model8583Request = Model8583Request()
        model8583Request.setMTI(mti)
        model8583Request.setTPDU("6000018053")
        return model8583Request
    }
    fun getSpecs(): HashMap<Int?, Model8583Bit?>? {
        val specs = HashMap<Int?, Model8583Bit?>();

        specs.put(
            3, Model8583Bit(
                3,
                "Processing Code",
                ISO8583.LEN_0,
                6
            )
        )
        specs.put(
            7, Model8583Bit(
                7,
                "Transmission Date & Time",
                ISO8583.LEN_0,
                10
            )
        )
        specs.put(
            11, Model8583Bit(
                11,
                "System Trace Audit Number",
                ISO8583.LEN_0,
                6
            )
        )
        specs.put(
            12, Model8583Bit(
                12,
                "Time, Local Transaction",
                ISO8583.LEN_0,
                6
            )
        )
        specs.put(
            13, Model8583Bit(
                13,
                "Date, Local Transaction",
                ISO8583.LEN_0,
                4
            )
        )
        specs.put(
            24, Model8583Bit(
                24,
                "NII",
                ISO8583.LEN_0,
                4
            )
        )
        specs.put(
            35, Model8583Bit(
                35,
                "Track 2 Data",
                ISO8583.LEN_0,
                40
            )
        )
        specs.put(
            37, Model8583Bit(
                37,
                "Retrieval Reference Number",
                ISO8583.LEN_0,
                24
            )
        )
        specs.put(
            38, Model8583Bit(
                38,
                "Authorization Identification Response",
                ISO8583.LEN_0,
                12
            )
        )
        specs.put(
            39, Model8583Bit(
                39,
                "Response Code",
                ISO8583.LEN_0,
                4
            )
        )
        specs.put(
            41, Model8583Bit(
                41,
                "Terminal ID",
                ISO8583.LEN_0,
                16
            )
        )
        specs.put(
            42, Model8583Bit(
                42,
                "Merchant ID",
                ISO8583.LEN_0,
                30
            )
        )
        specs.put(
            48, Model8583Bit(
                48,
                "Private Data",
                ISO8583.LEN_4HALF
            )
        )
        specs.put(
            52, Model8583Bit(
                52,
                "PIN Data",
                ISO8583.LEN_0,
                16
            )
        )
        specs.put(
            53, Model8583Bit(
                53,
                "Security Control Info",
                ISO8583.LEN_0,
                16
            )

        )
        specs.put(
            60, Model8583Bit(
                60,
                "Serial ID",
                ISO8583.LEN_4HALF
            )
        )
        return specs
    }
//    fun generateIsoStartEndDate(mti: String, processingCode: String): Model8583Request? {
//        try {
//            val model8583Request: Model8583Request = generateBaseRequest(mti)
//            Log.d(TAG, "Generating ISO message with Processing Code: $processingCode")
//
//            model8583Request.bits_sending?.add(
//                Model8583Bit(
//                    3,
//                    "Processing Code",
//                    ISO8583.LEN_0,
//                    processingCode
//                )
//            )
//            model8583Request.bits_sending?.add(
//                Model8583Bit(
//                    7,
//                    "Transmission Date & Time",
//                    ISO8583.LEN_0,
//                    simpleDateFormat()
//                )
//            )
//            model8583Request.bits_sending?.add(
//                Model8583Bit(
//                    11,
//                    "System Trace Audit Number",
//                    ISO8583.LEN_0,
//                    generateUniqueStan()
//                )
//            )
//            model8583Request.bits_sending?.add(
//                Model8583Bit(
//                    24,
//                    "NII",
//                    ISO8583.LEN_0,
//                    NII
//                )
//            )
//            model8583Request.bits_sending?.add(
//                Model8583Bit(
//                    35,
//                    "Track 2 Data",
//                    ISO8583.LEN_0,
//                    "9911019916781791D30091201000044010000"
////                    track2data?.replace('=', 'D')
//                ).setFunction("padStart")
//            )
//            model8583Request.bits_sending?.add(
//                Model8583Bit(
//                    41,
//                    "Terminal ID",
//                    ISO8583.LEN_0,
////                    StringUtils.convertStringToHex("TERM0001".padEnd(8, ' '))
////                    "12345678".padEnd(8, ' ')
//                    StringUtils.convertStringToHex(TID.padEnd(8, ' '))
//                )
//            )
//            model8583Request.bits_sending?.add(
//                Model8583Bit(
//                    42,
//                    "Merchant ID",
//                    ISO8583.LEN_0,
//                    StringUtils.convertStringToHex(MID.padEnd(15, ' '))
////                    "123456".padEnd(15, '0')
//                )
//            )
//            model8583Request.bits_sending?.add(
//                Model8583Bit(
//                    48,
//                    "Private Data",
//                    ISO8583.LEN_4HALF,
////                    officerCardNum
//                    "9911010026340396"
//                )
//            )
//            model8583Request.bits_sending?.add(
//                Model8583Bit(
//                    52,
//                    "PIN Data",
//                    ISO8583.LEN_0,
//                    pinBlockOwn
//                )
//            )
//            model8583Request.bits_sending?.add(
//                Model8583Bit(
//                    60,
//                    "Serial ID",
//                    ISO8583.LEN_0,
//                    "AB80263820"
//                )
//            )
//
//            model8583Request.setSpecs(getSpecs())
//            return model8583Request
//        } catch (e: Exception) {
//            LogUtils.e(TAG, "Error creating ISO message", e)
//            return null
//        }
//    }
fun generateIsoConnection(mti: String, processingCode: String): Model8583Request? {
    try {
        val model8583Request: Model8583Request = generateBaseRequest(mti)
        Log.d(TAG, "Generating ISO message with Processing Code: $processingCode")

        model8583Request.bits_sending?.add(
            Model8583Bit(
                3,
                "Processing Code",
                ISO8583.LEN_0,
                processingCode
            )
        )
        model8583Request.bits_sending?.add(
            Model8583Bit(
                7,
                "Transmission Date & Time",
                ISO8583.LEN_0,
                simpleDateFormat()
            )
        )
        model8583Request.bits_sending?.add(
            Model8583Bit(
                11,
                "System Trace Audit Number",
                ISO8583.LEN_0,
                generateUniqueStan()
            )
        )
        model8583Request.bits_sending?.add(
            Model8583Bit(
                24,
                "NII",
                ISO8583.LEN_0,
                NII
            )
        )
        model8583Request.bits_sending?.add(
            Model8583Bit(
                41,
                "Terminal ID",
                ISO8583.LEN_0,
                StringUtils.convertStringToHex(TID)
            )
        )
        model8583Request.bits_sending?.add(
            Model8583Bit(
                42,
                "Merchant ID",
                ISO8583.LEN_0,
                StringUtils.convertStringToHex(MID)
            )
        )
        model8583Request.bits_sending?.add(
            Model8583Bit(
                60,
                "Serial ID",
                ISO8583.LEN_4HALF,
                StringUtils.convertStringToHex(staticSN)
            )
        )

        model8583Request.setSpecs(getSpecs())
        return model8583Request
    } catch (e: Exception) {
        LogUtils.e(TAG, "Error creating ISO message", e)
        return null
    }
}
fun generateIsoStartEndDate(mti: String, processingCode: String): Model8583Request? {
    try {
        val model8583Request: Model8583Request = generateBaseRequest(mti)
        Log.d(TAG, "Generating ISO message with Processing Code: $processingCode")

        model8583Request.bits_sending?.add(
            Model8583Bit(
                3,
                "Processing Code",
                ISO8583.LEN_0,
                processingCode
            )
        )
        model8583Request.bits_sending?.add(
            Model8583Bit(
                7,
                "Transmission Date & Time",
                ISO8583.LEN_0,
                simpleDateFormat()
            )
        )
        model8583Request.bits_sending?.add(
            Model8583Bit(
                11,
                "System Trace Audit Number",
                ISO8583.LEN_0,
                generateUniqueStan()
            )
        )
        model8583Request.bits_sending?.add(
            Model8583Bit(
                24,
                "NII",
                ISO8583.LEN_0,
                NII
            )
        )

//        track2data = "9911019931030711=30091201000044010000"
//        track2data = "9911019916781791=30091201000044010000"
        val tk = "37" + track2data?.length?.let {
            if (it < 37) {
                track2data!!.replace('=', 'D').padEnd(36, '0') + "F"
            } else {
                track2data!!.replace('=', 'D') + "F"
            }
        }

        LogUtils.d(TAG, "tk2=$tk")
        model8583Request.bits_sending?.add(
            Model8583Bit(
                35,
                "Track 2 Data",
                ISO8583.LEN_0,
                tk
            )
        )

        model8583Request.bits_sending?.add(
            Model8583Bit(
                41,
                "Terminal ID",
                ISO8583.LEN_0,
                StringUtils.convertStringToHex(TID)
            )
        )
        model8583Request.bits_sending?.add(
            Model8583Bit(
                42,
                "Merchant ID",
                ISO8583.LEN_0,
                StringUtils.convertStringToHex(MID)
            )
        )
        model8583Request.bits_sending?.add(
            Model8583Bit(
                48,
                "Private Data",
                ISO8583.LEN_4HALF,
//                officerCardNum
//                StringUtils.convertStringToHex("9911010054404957")
                StringUtils.convertStringToHex(officerCardNum)
            )
        )
        Log.d("DEBUG", "PIN BLOCK $pinBlockOwn")
        model8583Request.bits_sending?.add(
            Model8583Bit(
                52,
                "PIN Data",
                ISO8583.LEN_0,
                pinBlockOwn
            )
        )
        model8583Request.bits_sending?.add(
            Model8583Bit(
                60,
                "Serial ID",
                ISO8583.LEN_4HALF,
                StringUtils.convertStringToHex(staticSN)
            )
        )

        model8583Request.setSpecs(getSpecs())
        return model8583Request
    } catch (e: Exception) {
        LogUtils.e(TAG, "Error creating ISO message", e)
        return null
    }
}

    fun isoStartEndDate(): ByteArray? {
        try {
            val proc = when(commandValue) {
                "startDate" -> "910000"
                "closeDate" -> "920000"
                else -> "000000"
            }
            val startEndDate = mapOf(
                3 to proc,
                11 to generateUniqueStan().padStart(6, '0'),
                24 to "831",
                35 to (track2data?.padStart(37, '0') ?: "".padStart(37, '0')),
                41 to "TERM0001",
                42 to "ATM00010",
                52 to pinBlockOwn
            )
            LogUtils.i(TAG, "ISO Data: $startEndDate")

            return IsoBuilder.createRequest("0800", startEndDate)
        } catch (e: Exception) {
            LogUtils.e(TAG, "Error creating ISO message", e)
            return null
        }
    }

    fun generateIsoLogonLogoff(mti: String, processingCode: String, officerTrack2data: String): Model8583Request? {
        try {
            val model8583Request: Model8583Request = generateBaseRequest(mti)
            Log.d(TAG, "Generating ISO message with Processing Code: $processingCode")

            model8583Request.bits_sending?.add(
                Model8583Bit(
                    3,
                    "Processing Code",
                    ISO8583.LEN_0,
                    processingCode
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    7,
                    "Transmission Date & Time",
                    ISO8583.LEN_0,
                    simpleDateFormat()
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    11,
                    "System Trace Audit Number",
                    ISO8583.LEN_0,
                    generateUniqueStan()
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    24,
                    "NII",
                    ISO8583.LEN_0,
                    NII
                )
            )
//            val officerTrack2data = "9911010026340396=30091201000044010000"
            val tk = "37" + officerTrack2data?.length?.let {
                if (it < 37) {
                    officerTrack2data!!.replace('=', 'D').padEnd(36, '0') + "F"
                } else {
                    officerTrack2data!!.replace('=', 'D') + "F"
                }
            }
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    35,
                    "Track 2 Data",
                    ISO8583.LEN_0,
                    tk
                ).setFunction("padStart")
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    41,
                    "Terminal ID",
                    ISO8583.LEN_0,
                    StringUtils.convertStringToHex(TID)
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    42,
                    "Merchant ID",
                    ISO8583.LEN_0,
                    StringUtils.convertStringToHex(MID)
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    52,
                    "PIN Data",
                    ISO8583.LEN_0,
                    pinBlockOwn
                )
            )

            model8583Request.bits_sending?.add(
                Model8583Bit(
                    60,
                    "Serial ID",
                    ISO8583.LEN_4HALF,
                    StringUtils.convertStringToHex(staticSN)
                )
            )

            model8583Request.setSpecs(getSpecs())
            return model8583Request
        } catch (e: Exception) {
            LogUtils.e(TAG, "Error creating ISO message", e)
            return null
        }
    }
    fun isoLogonLogoff(): ByteArray? {
        try {
            val proc = when(commandValue) {
                "logon" -> "810000"
                "logoff" -> "820000"
                else -> "000000"
            }

            val logonLogoff = mapOf(
                3 to proc,
                11 to generateUniqueStan().padStart(6, '0'),
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

    fun generateIsoCreateCustomerPIN(): Model8583Request? {
        try {
            val model8583Request: Model8583Request = generateBaseRequest("0600")
            Log.d(TAG, "Generating ISO message with Processing Code: 710000")

            model8583Request.bits_sending?.add(
                Model8583Bit(
                    3,
                    "Processing Code",
                    ISO8583.LEN_0,
                    "710000"
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    7,
                    "Transmission Date & Time",
                    ISO8583.LEN_0,
                    simpleDateFormat()
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    11,
                    "System Trace Audit Number",
                    ISO8583.LEN_0,
                    generateUniqueStan()
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    24,
                    "NII",
                    ISO8583.LEN_0,
                    NII
                )
            )

//        track2data = "9911019931030711=30091201000044010000"
//        track2data = "9911019916781791=30091201000044010000"
            val tk = "37" + track2data?.length?.let {
                if (it < 37) {
                    track2data!!.replace('=', 'D').padEnd(36, '0') + "F"
                } else {
                    track2data!!.replace('=', 'D') + "F"
                }
            }

            LogUtils.d(TAG, "tk2=$tk")
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    35,
                    "Track 2 Data",
                    ISO8583.LEN_0,
                    tk
                )
            )

            model8583Request.bits_sending?.add(
                Model8583Bit(
                    41,
                    "Terminal ID",
                    ISO8583.LEN_0,
                    StringUtils.convertStringToHex(TID)
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    42,
                    "Merchant ID",
                    ISO8583.LEN_0,
                    StringUtils.convertStringToHex(MID)
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    48,
                    "Private Data",
                    ISO8583.LEN_4HALF,
//                officerCardNum
//                StringUtils.convertStringToHex("9911010054404957")
                    StringUtils.convertStringToHex(officerCardNum)
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    52,
                    "PIN Data",
                    ISO8583.LEN_0,
                    pinBlockOwn
//                "688661FA3BC3E124"
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    53,
                    "Security Control Info",
                    ISO8583.LEN_0,
                    "0000000000000000"
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    60,
                    "Serial ID",
                    ISO8583.LEN_4HALF,
                    StringUtils.convertStringToHex(staticSN)
                )
            )

            model8583Request.setSpecs(getSpecs())
            return model8583Request
        } catch (e: Exception) {
            LogUtils.e(TAG, "Error creating ISO message", e)
            return null
        }
    }
    fun generateIsoCreateOfficerPIN(): Model8583Request? {
        try {
            val model8583Request: Model8583Request = generateBaseRequest("0100")
            Log.d(TAG, "Generating ISO message with Processing Code: 750000")

            model8583Request.bits_sending?.add(
                Model8583Bit(
                    3,
                    "Processing Code",
                    ISO8583.LEN_0,
                    "750000"
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    7,
                    "Transmission Date & Time",
                    ISO8583.LEN_0,
                    simpleDateFormat()
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    11,
                    "System Trace Audit Number",
                    ISO8583.LEN_0,
                    generateUniqueStan()
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    24,
                    "NII",
                    ISO8583.LEN_0,
                    NII
                )
            )

//        track2data = "9911019931030711=30091201000044010000"
//        track2data = "9911019916781791=30091201000044010000"
            val tk = "37" + track2data?.length?.let {
                if (it < 37) {
                    track2data!!.replace('=', 'D').padEnd(36, '0') + "F"
                } else {
                    track2data!!.replace('=', 'D') + "F"
                }
            }

            LogUtils.d(TAG, "tk2=$tk")
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    35,
                    "Track 2 Data",
                    ISO8583.LEN_0,
                    tk
                )
            )

            model8583Request.bits_sending?.add(
                Model8583Bit(
                    41,
                    "Terminal ID",
                    ISO8583.LEN_0,
                    StringUtils.convertStringToHex(TID)
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    42,
                    "Merchant ID",
                    ISO8583.LEN_0,
                    StringUtils.convertStringToHex(MID)
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    52,
                    "PIN Data",
                    ISO8583.LEN_0,
                    pinBlockOwn
//                "688661FA3BC3E124"
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    53,
                    "Security Control Info",
                    ISO8583.LEN_0,
                    ""
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    60,
                    "Serial ID",
                    ISO8583.LEN_4HALF,
                    StringUtils.convertStringToHex(staticSN)
                )
            )

            model8583Request.setSpecs(getSpecs())
            return model8583Request
        } catch (e: Exception) {
            LogUtils.e(TAG, "Error creating ISO message", e)
            return null
        }
    }

    fun generateIsoReissueOfficerPin(): Model8583Request? {
        try {
            val model8583Request = generateBaseRequest("0100")

            model8583Request.bits_sending?.add(
                Model8583Bit(
                    3,
                    "Processing Code",
                    ISO8583.LEN_0,
                    "750000"
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    7,
                    "Transmission Date & Time",
                    ISO8583.LEN_0,
                    simpleDateFormat()
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    11,
                    "System Trace Audit Number",
                    ISO8583.LEN_0,
                    generateUniqueStan()
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    24,
                    "NII",
                    ISO8583.LEN_0,
                    NII
                )
            )

            track2data = "9911010054404957=30091201000044010000"
//        track2data = "9911019916781791=30091201000044010000"
            val tk = "37" + track2data?.length?.let {
                if (it < 37) {
                    track2data!!.replace('=', 'D').padEnd(36, '0') + "F"
                } else {
                    track2data!!.replace('=', 'D') + "F"
                }
            }
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    35,
                    "Track 2 Data",
                    ISO8583.LEN_0,
                    tk
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    41,
                    "Terminal ID",
                    ISO8583.LEN_0,
                    StringUtils.convertStringToHex(TID)
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    42,
                    "Merchant ID",
                    ISO8583.LEN_0,
                    StringUtils.convertStringToHex(MID)
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    52,
                    "PIN Data",
                    ISO8583.LEN_0,
                    pinBlockOwn
//                    "688661FA3BC3E124"
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    60,
                    "Serial ID",
                    ISO8583.LEN_4HALF,
                    StringUtils.convertStringToHex(staticSN)
                )
            )

            model8583Request.setSpecs(getSpecs())
            return model8583Request
        } catch (e: Exception) {
            LogUtils.e(TAG, "Error creating ISO message", e)
            return null
        }
    }

    fun generateIsoReissueCustomerPIN(): Model8583Request? {
        try {
            val model8583Request = generateBaseRequest("0600")

            model8583Request.bits_sending.add(
                Model8583Bit(
                    3,
                    "Processing Code",
                    ISO8583.LEN_0,
                    "720000")
            )

            model8583Request.bits_sending?.add(
                Model8583Bit(
                    11,
                    "System Trace Audit Number",
                    ISO8583.LEN_0,
                    generateUniqueStan()
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    24,
                    "NII",
                    ISO8583.LEN_0,
                    NII
                )
            )
            val tk = "37" + track2data?.length?.let {
                if (it < 37) {
                    track2data!!.replace('=', 'D').padEnd(36, '0') + "F"
                } else {
                    track2data!!.replace('=', 'D') + "F"
                }
            }

            LogUtils.d(TAG, "tk2=$tk")
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    35,
                    "Track 2 Data",
                    ISO8583.LEN_0,
                    tk
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    41,
                    "Terminal ID",
                    ISO8583.LEN_0,
                    StringUtils.convertStringToHex(TID)
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    42,
                    "Merchant ID",
                    ISO8583.LEN_0,
                    StringUtils.convertStringToHex(MID)
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    48,
                    "Add. Data - Private",
                    ISO8583.LEN_4FULL,
                    field48data
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    52,
                    "PIN Data",
                    ISO8583.LEN_0,
                    pinBlockOwn
                )
            )

            model8583Request.setSpecs(getSpecs())
            return model8583Request
        } catch (e: Exception) {
            LogUtils.e(TAG, "Error creating ISO Create PIN message", e)
            return null
        }
    }

    fun isoCreatePIN(): ByteArray? {
        try {
            val createPin = mapOf(
                3 to "710000",
                11 to generateUniqueStan().padStart(6, '0'),
                24 to "831",
                35 to (track2data?.padStart(37, '0') ?: "".padStart(37, '0')),
                41 to "TERM0001",
                42 to "ATM00010",
                48 to field48data,
                52 to pinBlockOwn
            )
            LogUtils.i(TAG, "ISO Create PIN Data: $createPin")

            return IsoBuilder.createRequest("0100", createPin)
        } catch (e: Exception) {
            LogUtils.e(TAG, "Error creating ISO Create PIN message", e)
            return null
        }
    }

    fun isoReissuePIN(): ByteArray? {
        try {
            val reissuePin = mapOf(
                3 to "720000",
                11 to generateUniqueStan().padStart(6, '0'),
                24 to "831",
                35 to (track2data?.padStart(37, '0') ?: "".padStart(37, '0')),
                41 to "TERM0001",
                42 to "ATM00010",
                48 to field48data,
                52 to pinBlockOwn
            )
            LogUtils.i(TAG, "ISO Reissue PIN Data: $reissuePin")

            return IsoBuilder.createRequest("0100", reissuePin)
        } catch (e: Exception) {
            LogUtils.e(TAG, "Error creating ISO Reissue PIN message", e)
            return null
        }
    }

    fun generateIsoChangePIN(): Model8583Request? {
        try {
            val model8583Request = generateBaseRequest("0100")

            model8583Request.bits_sending.add(
                Model8583Bit(
                    3,
                    "Processing Code",
                    ISO8583.LEN_0,
                    "730000")
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    7,
                    "Transmission Date & Time",
                    ISO8583.LEN_0,
                    simpleDateFormat()
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    11,
                    "System Trace Audit Number",
                    ISO8583.LEN_0,
                    generateUniqueStan()
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    24,
                    "NII",
                    ISO8583.LEN_0,
                    NII
                )
            )
            val tk = "37" + track2data?.length?.let {
                if (it < 37) {
                    track2data!!.replace('=', 'D').padEnd(36, '0') + "F"
                } else {
                    track2data!!.replace('=', 'D') + "F"
                }
            }

            LogUtils.d(TAG, "tk2=$tk")
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    35,
                    "Track 2 Data",
                    ISO8583.LEN_0,
                    tk
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    41,
                    "Terminal ID",
                    ISO8583.LEN_0,
                    StringUtils.convertStringToHex(TID)
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    42,
                    "Merchant ID",
                    ISO8583.LEN_0,
                    StringUtils.convertStringToHex(MID)
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    48,
                    "Private Data",
                    ISO8583.LEN_4HALF,
                    StringUtils.convertStringToHex(pinBlockConfirm)
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    52,
                    "PIN Data",
                    ISO8583.LEN_0,
                    pinBlockOwn
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    53,
                    "Security Control Info",
                    ISO8583.LEN_0,
                    "0000000000000000"
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    60,
                    "Serial ID",
                    ISO8583.LEN_4HALF,
                    StringUtils.convertStringToHex(staticSN)
                )
            )


            model8583Request.setSpecs(getSpecs())
            return model8583Request
        } catch (e: Exception) {
            LogUtils.e(TAG, "Error creating ISO Create PIN message", e)
            return null
        }
    }

    fun isoChangePIN(): ByteArray? {
        try {
            val changePin = mapOf(
                3 to "730000",
                11 to generateUniqueStan().padStart(6, '0'),
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

    fun generateIsoVerificationPIN(): Model8583Request? {
        try {
            val model8583Request = generateBaseRequest("0100")

            model8583Request.bits_sending.add(
                Model8583Bit(
                    3,
                    "Processing Code",
                    ISO8583.LEN_0,
                    "740000")
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    7,
                    "Transmission Date & Time",
                    ISO8583.LEN_0,
                    simpleDateFormat()
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    11,
                    "System Trace Audit Number",
                    ISO8583.LEN_0,
                    generateUniqueStan()
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    24,
                    "NII",
                    ISO8583.LEN_0,
                    NII
                )
            )
            val tk = "37" + track2data?.length?.let {
                if (it < 37) {
                    track2data!!.replace('=', 'D').padEnd(36, '0') + "F"
                } else {
                    track2data!!.replace('=', 'D') + "F"
                }
            }

            LogUtils.d(TAG, "tk2=$tk")
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    35,
                    "Track 2 Data",
                    ISO8583.LEN_0,
                    tk
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    41,
                    "Terminal ID",
                    ISO8583.LEN_0,
                    StringUtils.convertStringToHex(TID)
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    42,
                    "Merchant ID",
                    ISO8583.LEN_0,
                    StringUtils.convertStringToHex(MID)
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    52,
                    "PIN Data",
                    ISO8583.LEN_0,
                pinBlockOwn
//                    "B34274FA6873D2CC"
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    53,
                    "Security Control Info",
                    ISO8583.LEN_0,
                    "0000000000000000"
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    60,
                    "Serial ID",
                    ISO8583.LEN_4HALF,
                    StringUtils.convertStringToHex(staticSN)
//                    StringUtils.convertStringToHex("AB80263820")
                )
            )

            model8583Request.setSpecs(getSpecs())
            return model8583Request
        } catch (e: Exception) {
            LogUtils.e(TAG, "Error creating ISO Create PIN message", e)
            return null
        }
    }

    fun isoVerificationPIN(): ByteArray? {
        try {
            val verificationPin = mapOf(
                3 to "740000",
                11 to generateUniqueStan().padStart(6, '0'),
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