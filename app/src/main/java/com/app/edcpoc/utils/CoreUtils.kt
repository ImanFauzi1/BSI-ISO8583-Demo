package com.app.edcpoc.utils

import android.content.Context
import com.app.edcpoc.interfaces.EmvUtilInterface


object CoreUtils {
    fun generateUniqueStan(): String {
        val rnd = java.security.SecureRandom()
        val number = rnd.nextInt(1_000_000)
        return String.format("%06d", number)
    }
    fun initializeEmvUtil(context: Context, emvUtilInterface: EmvUtilInterface): EmvUtil {
        val emvUtil = EmvUtil(context)
        emvUtil.initialize()
        emvUtil.emvOpen()
        emvUtil.setCallback(emvUtilInterface)
        return emvUtil
    }
}