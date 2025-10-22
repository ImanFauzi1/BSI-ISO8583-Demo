package com.app.edcpoc

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object PreferenceManager {
    private const val PREF_NAME = "svp_prefs"
    private const val KEY_ACTIVATED = "is_activated"
    private const val KEY_OFFICER_LOGGED_IN = "is_officer_logged_in"

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun setActivated(context: Context, activated: Boolean) {
        getPrefs(context).edit { putBoolean(KEY_ACTIVATED, activated) }
    }

    fun isActivated(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_ACTIVATED, false)

    fun setOfficerLoggedIn(context: Context, loggedIn: Boolean) {
        getPrefs(context).edit { putBoolean(KEY_OFFICER_LOGGED_IN, loggedIn) }
    }

    fun isOfficerLoggedIn(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_OFFICER_LOGGED_IN, false)

    fun clearAll(context: Context) {
        getPrefs(context).edit { clear() }
    }
}
