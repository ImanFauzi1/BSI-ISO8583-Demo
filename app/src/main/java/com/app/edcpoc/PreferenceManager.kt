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

    fun setSvpCardNum(context: Context, svpCardNum: String?) {
        getPrefs(context).edit { putString(SVP_CARD_NUM, svpCardNum) }
    }

    fun getSvpCardNum(context: Context): String? =
        getPrefs(context).getString(SVP_CARD_NUM, null)

    fun setOfficerLoggedIn(context: Context, cardNum: String?) {
        getPrefs(context).edit { putString(OFFICER_CARD_NUM, cardNum) }
    }

    fun getOfficerCardNum(context: Context): String? =
        getPrefs(context).getString(OFFICER_CARD_NUM, null)

    fun clearAll(context: Context) {
        getPrefs(context).edit { clear() }
    }
}
