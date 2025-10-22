package com.app.edcpoc

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object PreferenceManager {
    private const val PREF_NAME = "svp_prefs"
    private const val SVP_CARD_NUM = "svp_card_num"
    private const val OFFICER_CARD_NUM = "officer_card_num"

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun setPCID(context: Context, pcid: String){
        getPrefs(context).edit { putString("PCID", pcid) }
    }

    fun setConfigFile(context: Context, configFile: String){
        getPrefs(context).edit { putString("CONFIG_FILE", configFile) }
    }

    fun setSvpCardNum(context: Context, svpCardNum: String?) {
        getPrefs(context).edit { putString(SVP_CARD_NUM, svpCardNum) }
    }

    fun getSvpCardNum(context: Context): String? =
        getPrefs(context).getString(SVP_CARD_NUM, null)

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
}
