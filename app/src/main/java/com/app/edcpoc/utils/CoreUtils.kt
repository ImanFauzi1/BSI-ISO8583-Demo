package com.app.edcpoc.utils

import com.zcs.sdk.DriverManager
import com.zcs.sdk.SdkResult
import com.zcs.sdk.Sys

object CoreUtils {
    fun initSdk(mSys: Sys) {
        var status: Int = mSys.sdkInit()
        if (status != SdkResult.SDK_OK) {
            mSys.sysPowerOn()
            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            status = mSys.sdkInit()
        }
        if (status != SdkResult.SDK_OK) {
            //init failed.
        }
        mSys.showDetailLog(true)
    }
}