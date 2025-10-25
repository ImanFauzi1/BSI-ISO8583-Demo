package com.app.edcpoc

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.core.net.toUri

object PreferenceManager {
    private const val PREF_NAME = "svp_prefs"
    private const val SVP_CARD_NUM = "svp_card_num"
    private const val OFFICER_CARD_NUM = "officer_card_num"
    private const val SVP_USER_ID = "svp_user_id"
    private const val GATEWAY_URL = "gateway_url"
    private const val HOST = "host"
    private const val HOST_PORT = "host_port"
    private const val TMS = "tms"
    private const val TMS_PORT = "tms_port"

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun setPCID(context: Context, pcid: String){
        getPrefs(context).edit { putString("PCID", pcid) }
    }
    fun setSVPUserId(context: Context, userId: String){
        getPrefs(context).edit { putString(SVP_USER_ID, userId) }
    }

    fun setConfigFile(context: Context, configFile: String){
        getPrefs(context).edit { putString("CONFIG_FILE", configFile) }
    }

    fun setSvpCardNum(context: Context, svpCardNum: String?) {
        getPrefs(context).edit { putString(SVP_CARD_NUM, svpCardNum) }
    }

    fun getSvpCardNum(context: Context): String? =
        getPrefs(context).getString(SVP_CARD_NUM, null)

    fun getSvpUserId(context: Context): String? =
        getPrefs(context).getString(SVP_USER_ID, null)
    fun setOfficerLoggedIn(context: Context, track2data: String?) {
        getPrefs(context).edit { putString(OFFICER_CARD_NUM, track2data) }
    }

    fun getOfficerCardNum(context: Context): String? =
        getPrefs(context).getString(OFFICER_CARD_NUM, null)

    fun clearAll(context: Context) {
        getPrefs(context).edit { clear() }
    }

    fun getPCID(context: Context): String? {
        return getPrefs(context).getString("PCID", null)
    }

    fun getConfigFile(context: Context): String? {
        return getPrefs(context).getString("CONFIG_FILE", null)
    }

    fun setGatewayUrl(context: Context, url: String) {
        getPrefs(context).edit { putString(GATEWAY_URL, url) }
    }
    fun setGatewayPort(context: Context, port: String) {
        getPrefs(context).edit { putString(GATEWAY_URL, port) }
    }
    fun setHost(context: Context, host: String) {
        getPrefs(context).edit { putString(HOST, host) }
    }
    fun setHostPort(context: Context, port: String) {
        getPrefs(context).edit { putString(HOST_PORT, port) }
    }
    fun setTms(context: Context, tms: String) {
        getPrefs(context).edit { putString(TMS, tms) }
    }
    fun setTmsPort(context: Context, port: String) {
        getPrefs(context).edit { putString(TMS_PORT, port) }
    }
    fun getGatewayUrl(context: Context): String? {
        return getPrefs(context).getString(GATEWAY_URL, null)
    }
    fun getGatewayPort(context: Context): String? {
        return getPrefs(context).getString(GATEWAY_URL, "0")
    }
    fun getHost(context: Context): String? {
        return getPrefs(context).getString(HOST, null)
    }
    fun getHostPort(context: Context): String? {
        return getPrefs(context).getString(HOST_PORT, "0")
    }
    fun getTms(context: Context): String? {
        return getPrefs(context).getString(TMS, null)
    }
    fun getTmsPort(context: Context): String? {
        return getPrefs(context).getString(TMS_PORT, "0")
    }
}
