package com.app.edcpoc.utils.IsoManager

import com.app.edcpoc.utils.IsoManager.IsoUtils.generateIsoChangePIN
import com.app.edcpoc.utils.IsoManager.IsoUtils.generateIsoConnection
import com.app.edcpoc.utils.IsoManager.IsoUtils.generateIsoCreateCustomerPIN
import com.app.edcpoc.utils.IsoManager.IsoUtils.generateIsoCreateOfficerPIN
import com.app.edcpoc.utils.IsoManager.IsoUtils.generateIsoEndDate
import com.app.edcpoc.utils.IsoManager.IsoUtils.generateIsoLogoff
import com.app.edcpoc.utils.IsoManager.IsoUtils.generateIsoLogonLogoff
import com.app.edcpoc.utils.IsoManager.IsoUtils.generateIsoReissueCustomerPIN
import com.app.edcpoc.utils.IsoManager.IsoUtils.generateIsoReissueOfficerPin
import com.app.edcpoc.utils.IsoManager.IsoUtils.generateIsoStartEndDate
import com.app.edcpoc.utils.IsoManager.IsoUtils.generateIsoVerificationPIN
import com.app.edcpoc.utils.LogUtils
import com.google.gson.Gson

object IsoHelper {
	private const val TAG = "IsoHelper"

	// Connection
	fun buildOpenConnection(): String? = tryPack(generateIsoConnection("0800", "800000"))
	fun buildOpenConnectionWithModel(): Pair<String, Model8583Request>? = tryPackWithModel(generateIsoConnection("0800", "800000"))

	// Start/End Date
	fun buildStartDate(): String? = tryPack(generateIsoStartEndDate("0600", "910000"))
	fun buildStartDateWithModel(): Pair<String, Model8583Request>? = tryPackWithModel(generateIsoStartEndDate("0600", "910000"))
	fun buildEndDateLegacy(): String? = tryPack(generateIsoEndDate())
	fun buildEndDateLegacyWithModel(): Pair<String, Model8583Request>? = tryPackWithModel(generateIsoEndDate())
	fun buildEndDate(): String? = tryPack(generateIsoStartEndDate("0600", "920000"))
	fun buildEndDateWithModel(): Pair<String, Model8583Request>? = tryPackWithModel(generateIsoStartEndDate("0600", "920000"))

	// Logon/Logoff
	fun buildLogon(): String? = tryPack(generateIsoLogonLogoff("0800", "810000", ""))
	fun buildLogonWithModel(): Pair<String, Model8583Request>? = tryPackWithModel(generateIsoLogonLogoff("0800", "810000", ""))
	fun buildLogoff(): String? = tryPack(generateIsoLogoff())
	fun buildLogoffWithModel(): Pair<String, Model8583Request>? = tryPackWithModel(generateIsoLogoff())

	// PIN flows
	fun buildCreateCustomerPin(): String? = tryPack(generateIsoCreateCustomerPIN())
	fun buildCreateCustomerPinWithModel(): Pair<String, Model8583Request>? = tryPackWithModel(generateIsoCreateCustomerPIN())
	fun buildCreateOfficerPin(): String? = tryPack(generateIsoCreateOfficerPIN())
	fun buildCreateOfficerPinWithModel(): Pair<String, Model8583Request>? = tryPackWithModel(generateIsoCreateOfficerPIN())
	fun buildReissueOfficerPin(): String? = tryPack(generateIsoReissueOfficerPin())
	fun buildReissueOfficerPinWithModel(): Pair<String, Model8583Request>? = tryPackWithModel(generateIsoReissueOfficerPin())
	fun buildReissueCustomerPin(): String? = tryPack(generateIsoReissueCustomerPIN())
	fun buildReissueCustomerPinWithModel(): Pair<String, Model8583Request>? = tryPackWithModel(generateIsoReissueCustomerPIN())
	fun buildChangePin(): String? = tryPack(generateIsoChangePIN())
	fun buildChangePinWithModel(): Pair<String, Model8583Request>? = tryPackWithModel(generateIsoChangePIN())
	fun buildVerifyPin(): String? = tryPack(generateIsoVerificationPIN())
	fun buildVerifyPinWithModel(): Pair<String, Model8583Request>? = tryPackWithModel(generateIsoVerificationPIN())

	private fun tryPack(model: Model8583Request?): String? {
		return tryPackWithModel(model)?.first
	}

	fun tryPackWithModel(model: Model8583Request?): Pair<String, Model8583Request>? {
		return try {
			if (model == null) return null
			val packed = ISO8583.packToHex(model)
			// Optional: quick sanity unpack for logging
			try {
				val unpack = ISO8583.unpackFromHex(packed, model)
				LogUtils.d(TAG, "Unpacked: ${Gson().toJson(unpack)}")
			} catch (_: Exception) { }
			packed to model
		} catch (e: Exception) {
			LogUtils.e(TAG, "Error packing ISO8583: ${e.message}", e)
			null
		}
	}
}