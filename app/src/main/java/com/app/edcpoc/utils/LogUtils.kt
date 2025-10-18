package com.app.edcpoc.utils

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

object LogUtils {
    private const val DEFAULT_TAG = "VictoriaPOC"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

    fun d(tag: String = DEFAULT_TAG, message: String) {
        Log.d(tag, formatMessage(message))
    }

    fun i(tag: String = DEFAULT_TAG, message: String) {
        Log.i(tag, formatMessage(message))
    }

    fun w(tag: String = DEFAULT_TAG, message: String) {
        Log.w(tag, formatMessage(message))
    }

    fun e(tag: String = DEFAULT_TAG, message: String, throwable: Throwable? = null) {
        Log.e(tag, formatMessage(message), throwable)
    }

    private fun formatMessage(message: String): String {
        val timestamp = dateFormat.format(Date())
        val thread = Thread.currentThread().name
        return "[$timestamp][$thread] $message"
    }
}
