package com.idpay.victoriapoc.utils.fingerprint

import android.content.Context
import cn.com.aratek.fp.Bione
import cn.com.aratek.fp.FingerprintScanner
import com.app.edcpoc.R

object FingerprintError {

    fun getFingerprintErrorString(context: Context, error: Int): String {
        val strid: Int
        strid = when (error) {
            FingerprintScanner.RESULT_OK -> R.string.operation_successful
            FingerprintScanner.RESULT_FAIL -> R.string.error_operation_failed
            FingerprintScanner.WRONG_CONNECTION -> R.string.error_wrong_connection
            FingerprintScanner.DEVICE_BUSY -> R.string.error_device_busy
            FingerprintScanner.DEVICE_NOT_OPEN -> R.string.error_device_not_open
            FingerprintScanner.TIMEOUT -> R.string.error_timeout
            FingerprintScanner.NO_PERMISSION -> R.string.error_no_permission
            FingerprintScanner.WRONG_PARAMETER -> R.string.error_wrong_parameter
            FingerprintScanner.DECODE_ERROR -> R.string.error_decode
            FingerprintScanner.INIT_FAIL -> R.string.error_initialization_failed
            FingerprintScanner.UNKNOWN_ERROR -> R.string.error_unknown
            FingerprintScanner.NOT_SUPPORT -> R.string.error_not_support
            FingerprintScanner.NOT_ENOUGH_MEMORY -> R.string.error_not_enough_memory
            FingerprintScanner.DEVICE_NOT_FOUND -> R.string.error_device_not_found
            FingerprintScanner.DEVICE_REOPEN -> R.string.error_device_reopen
            FingerprintScanner.NO_FINGER -> R.string.error_no_finger
            Bione.BAD_IMAGE -> R.string.error_bad_image
            Bione.NOT_MATCH -> R.string.error_not_match
            Bione.LOW_POINT -> R.string.error_low_point
            Bione.NO_RESULT -> R.string.error_no_result
            Bione.OUT_OF_BOUND -> R.string.error_out_of_bound
            Bione.DATABASE_FULL -> R.string.error_database_full
            Bione.LIBRARY_MISSING -> R.string.error_library_missing
            Bione.UNINITIALIZE -> R.string.error_algorithm_uninitialize
            Bione.REINITIALIZE -> R.string.error_algorithm_reinitialize
            Bione.REPEATED_ENROLL -> R.string.error_repeated_enroll
            Bione.NOT_ENROLLED -> R.string.error_not_enrolled
            else -> R.string.error_other
        }
        return if (strid != R.string.error_other) {
            context.getString(strid)
        } else context.getString(strid, error)
    }
}