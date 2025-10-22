package com.idpay.victoriapoc.utils.IsoManagement

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.app.edcpoc.utils.Constants.commandValue
import com.app.edcpoc.utils.Constants.field48data
import com.app.edcpoc.utils.Constants.pinBlockConfirm
import com.app.edcpoc.utils.Constants.pinBlockOwn
import com.app.edcpoc.utils.Constants.track2data
import com.app.edcpoc.utils.Constants.track2data
import com.app.edcpoc.utils.Constants.field48hex
import com.app.edcpoc.utils.CoreUtils.generateUniqueStan
import com.app.edcpoc.utils.IsoManager.ISO8583
import com.app.edcpoc.utils.IsoManager.Model8583Bit
import com.app.edcpoc.utils.IsoManager.Model8583Request
import com.app.edcpoc.utils.LogUtils
import com.google.gson.Gson
import com.zcs.sdk.util.StringUtils
import org.jpos.iso.ISOMsg
import org.jpos.iso.packager.GenericPackager
import org.jpos.iso.packager.ISO87BPackager

object IsoUtils {
    private val TAG = "IsoUtils"

    private fun generateBaseRequest(mti: String): Model8583Request {
        val model8583Request = Model8583Request()
        model8583Request.setMTI(mti)
        model8583Request.setTPDU("0000000001")
        return model8583Request
    }
    fun parseIsoResponse(iso: String): Map<String, String> {
        var idx = 0
        fun take(n: Int): String {
            val s = iso.substring(idx, (idx + n).coerceAtMost(iso.length))
            idx += n
            return s
        }
        return mapOf(
            "MTI" to take(4),
            "Bitmap" to take(16),
            "ProcessingCode" to take(6),
            "STI" to take(6),
            "F12" to take(6),
            "F13" to take(4),
            "F24" to take(3),
            "F37" to take(12),
            "F38" to take(6),
            "F39" to take(2),
            "F41" to take(8)
        )
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
                3
            )
        )
        specs.put(
            35, Model8583Bit(
                35,
                "Track 2 Data",
                ISO8583.LEN_2FULL,
                37
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
                "Add. Data - Private",
                ISO8583.LEN_4FULL,
                999
            )
        )
        specs.put(
            52, Model8583Bit(
                52,
                "PIN Data",
                ISO8583.LEN_0,
                32
            )
        )
        return specs
    }
    fun generateIsoStartEndDate(mti: String, processingCode: String): String? {
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
                    "831"
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    35,
                    "Track 2 Data",
                    ISO8583.LEN_2FULL,
                    track2data?.replace('=', 'D')
                ).setFunction("padStart")
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    41,
                    "Terminal ID",
                    ISO8583.LEN_0,
//                    StringUtils.convertStringToHex("TERM0001".padEnd(8, ' '))
//                    "12345678".padEnd(8, ' ')
                    StringUtils.convertStringToHex("1234".padEnd(8, ' '))
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    42,
                    "Merchant ID",
                    ISO8583.LEN_0,
                    StringUtils.convertStringToHex("123456".padEnd(15, ' '))
//                    "123456".padEnd(15, '0')
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

            val specs = model8583Request.setSpecs(getSpecs())
            val packed = ISO8583.packToHex(specs)
            LogUtils.d(TAG, "ISO Data: ${Gson().toJson(packed)}")

            return packed
        } catch (e: Exception) {
            LogUtils.e(TAG, "Error creating ISO message", e)
            return null
        }
    }

    fun generateIsoStartEndDateJpos(context: Context, mti: String, data: Map<Int, String?>): ISOMsg {
        try {
            // force jPOS pakai parser Android (bukan Crimson)
            System.setProperty("org.xml.sax.driver", "org.xmlpull.v1.sax2.Driver")
            System.setProperty("javax.xml.parsers.SAXParserFactory", "org.xmlpull.v1.sax2.Driver")

            // pastikan file ada di assets/
            context.assets.open("iso8583-custom-packager.xml").use { inputStream ->
                val packager = GenericPackager(inputStream)

                val spec = IsoRepository.specs[mti]
                    ?: throw IllegalArgumentException("Spec untuk MTI $mti tidak ditemukan")

                return ISOMsg().apply {
                    this.packager = packager
                    setMTI(mti)

                    spec.requiredFields.forEach { field ->
                        data[field]?.let { set(field, it) }
                            ?: throw IllegalArgumentException("Field $field wajib diisi")
                    }
                    spec.optionalFields.forEach { field ->
                        data[field]?.let { set(field, it) }
                    }
                }
            }
        } catch (e: Exception) {
            LogUtils.e("ISO8583", "Error creating ISO message", e)
            throw e
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

    fun generateIsoLogonLogoff(mti: String, processingCode: String, officerTrack2data: String): String? {
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
                    "831"
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    35,
                    "Track 2 Data",
                    ISO8583.LEN_2FULL,
                    officerTrack2data.replace('=', 'D')
                ).setFunction("padStart")
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    41,
                    "Terminal ID",
                    ISO8583.LEN_0,
                    StringUtils.convertStringToHex("ATM00010".padEnd(15, ' '))
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    42,
                    "Merchant ID",
                    ISO8583.LEN_0,
                    StringUtils.convertStringToHex("ATM00010".padEnd(15, ' '))
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
            LogUtils.d(TAG, "ISO Data: ${Gson().toJson(ISO8583.packToHex(model8583Request))}")
            return ISO8583.packToHex(model8583Request)
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

    fun generateIsoCreatePIN(svpCardNumber: String): String? {
        try {
            val model8583Request = generateBaseRequest("0100")

            model8583Request.bits_sending.add(
                Model8583Bit(
                    3,
                    "Processing Code",
                    ISO8583.LEN_0,
                    "710000")
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
                    "831"
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    35,
                    "Track 2 Data",
                    ISO8583.LEN_2FULL,
                    svpCardNumber.replace('=', 'D')
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    41,
                    "Terminal ID",
                    ISO8583.LEN_0,
//                    StringUtils.convertStringToHex("TERM0001".padEnd(8, ' '))
//                    "12345678".padEnd(8, ' ')
                    StringUtils.convertStringToHex("1234".padEnd(8, ' '))
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    42,
                    "Merchant ID",
                    ISO8583.LEN_0,
                    StringUtils.convertStringToHex("123456".padEnd(15, ' '))
//                    "123456".padEnd(15, '0')
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
            LogUtils.d(TAG, "ISO Data: ${Gson().toJson(ISO8583.packToHex(model8583Request))}")
            return ISO8583.packToHex(model8583Request)
        } catch (e: Exception) {
            LogUtils.e(TAG, "Error creating ISO Create PIN message", e)
            return null
        }
    }

    fun generateIsoReissuePIN(svpCardNumber: String): String? {
        try {
            val model8583Request = generateBaseRequest("0100")

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
                    "831"
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    35,
                    "Track 2 Data",
                    ISO8583.LEN_2FULL,
                    svpCardNumber.replace('=', 'D')
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    41,
                    "Terminal ID",
                    ISO8583.LEN_0,
//                    StringUtils.convertStringToHex("TERM0001".padEnd(8, ' '))
//                    "12345678".padEnd(8, ' ')
                    StringUtils.convertStringToHex("1234".padEnd(8, ' '))
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    42,
                    "Merchant ID",
                    ISO8583.LEN_0,
                    StringUtils.convertStringToHex("123456".padEnd(15, ' '))
//                    "123456".padEnd(15, '0')
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
            LogUtils.d(TAG, "ISO Data: ${Gson().toJson(ISO8583.packToHex(model8583Request))}")
            return ISO8583.packToHex(model8583Request)
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

    fun generateIsoChangePIN(): String? {
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
                    "831"
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    35,
                    "Track 2 Data",
                    ISO8583.LEN_2FULL,
                    track2data?.replace('=', 'D')
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    41,
                    "Terminal ID",
                    ISO8583.LEN_0,
//                    StringUtils.convertStringToHex("TERM0001".padEnd(8, ' '))
//                    "12345678".padEnd(8, ' ')
                    StringUtils.convertStringToHex("1234".padEnd(8, ' '))
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    42,
                    "Merchant ID",
                    ISO8583.LEN_0,
                    StringUtils.convertStringToHex("123456".padEnd(15, ' '))
//                    "123456".padEnd(15, '0')
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    48,
                    "Add. Data - Private",
                    ISO8583.LEN_4HALF,
                    pinBlockConfirm
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
            LogUtils.d(TAG, "ISO Data: ${Gson().toJson(ISO8583.packToHex(model8583Request))}")
            return ISO8583.packToHex(model8583Request)
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

    fun generateIsoVerificationPIN(): String? {
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
                    "831"
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    35,
                    "Track 2 Data",
                    ISO8583.LEN_2FULL,
                    track2data?.replace('=', 'D')
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    41,
                    "Terminal ID",
                    ISO8583.LEN_0,
//                    StringUtils.convertStringToHex("TERM0001".padEnd(8, ' '))
//                    "12345678".padEnd(8, ' ')
                    StringUtils.convertStringToHex("1234".padEnd(8, ' '))
                )
            )
            model8583Request.bits_sending?.add(
                Model8583Bit(
                    42,
                    "Merchant ID",
                    ISO8583.LEN_0,
                    StringUtils.convertStringToHex("123456".padEnd(15, ' '))
//                    "123456".padEnd(15, '0')
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
            LogUtils.d(TAG, "ISO Data: ${Gson().toJson(ISO8583.packToHex(model8583Request))}")
            return ISO8583.packToHex(model8583Request)
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