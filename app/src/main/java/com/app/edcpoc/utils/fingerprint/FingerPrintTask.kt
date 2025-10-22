package com.idpay.victoriapoc.utils.fingerprint

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import cn.com.aratek.fp.Bione
import cn.com.aratek.fp.FingerprintImage
import cn.com.aratek.fp.FingerprintScanner
import cn.com.aratek.util.Result
import com.app.edcpoc.MyApp
import com.app.edcpoc.utils.Constants.fingerTimeout
import com.app.edcpoc.utils.Constants.isTimeout
import com.idpay.victoriapoc.utils.fingerprint.FingerprintError.getFingerprintErrorString
import com.simo.ektp.GlobalVars.FP_DB_PATH
import com.zcs.sdk.util.StringUtils
import kotlin.dec

class FingerPrintTask private constructor() {
    private val mScanner: FingerprintScanner = FingerprintScanner.getInstance(MyApp.getContext())
    private var mLastImage: FingerprintImage? = null

    companion object {
        val instance: FingerPrintTask by lazy { FingerPrintTask() }
        var fi: FingerprintImage? = null
        var featureEnroll: ByteArray? = null
        private const val TAG = "FingerPrintTask"
        private var fingerprint: Fingerprint? = null
    }

    fun openDevice() {
        Thread {
            synchronized(this) {
                var error: Int
                if (mScanner.open().also { error = it } != FingerprintScanner.RESULT_OK) {
                    Log.e(TAG, "fingerprint_device_open_failed ${getFingerprintErrorString(MyApp.getContext(), error)}")
                }
                if (Bione.initialize(MyApp.getContext(), FP_DB_PATH).also { error = it } != Bione.RESULT_OK) {
                    Log.e(TAG, "algorithm_initialization_failed ${getFingerprintErrorString(MyApp.getContext(), error)}")
                }
                Log.i(TAG, "Fingerprint algorithm version: ${Bione.getVersion()}")
            }
        }.start()
    }

    fun closeDevice() {
        Thread {
            synchronized(this) {
                var error: Int
                fingerprint?.takeIf { it.status != AsyncTask.Status.FINISHED }?.let {
                    it.cancel(false)
                    it.waitForDone()
                }
                if (mScanner.close().also { error = it } != FingerprintScanner.RESULT_OK) {
                    Log.e(TAG, "fingerprint_device_close_failed ${getFingerprintErrorString(MyApp.getContext(),error)}")
                }
                if (mScanner.powerOff().also { error = it } != FingerprintScanner.RESULT_OK) {
                    Log.e(TAG, "fingerprint_device_power_off_failed ${getFingerprintErrorString(MyApp.getContext(), error)}")
                }
                if (Bione.exit().also { error = it } != Bione.RESULT_OK) {
                    Log.e(TAG, "algorithm_cleanup_failed ${getFingerprintErrorString(MyApp.getContext(),error)}")
                }
            }
        }.start()
    }

//    fun captureFinger(): ByteArray? {
//        mScanner.setLedStatus(1, 1)
//        var error: Int
//        var isTimeout = false
//        var fpFeat: ByteArray? = null
//        var fpTemp: ByteArray? = null
//
//        do {
//            var capRetry = 0
//            mScanner.prepare()
//            var timeout = fingerTimeout
//
//            do {
//                val time = System.currentTimeMillis()
//                val res = mScanner.capture()
//                val endTime = System.currentTimeMillis()
//                Log.i("Sanny", "capture time: ${endTime - time}")
//                error = res.error
//
//                if (error == FingerprintScanner.TIMEOUT) {
//                    timeout--
//                    if (timeout == 0) {
//                        isTimeout = true
//                        break
//                    }
//                    Thread.sleep(10)
//                    continue
//                }
//
//                fi = res.data as FingerprintImage?
//                mLastImage = fi
//                val quality = fi?.let { Bione.getFingerprintQuality(it) } ?: 0
//
//                if (quality < 50 && capRetry < 3) {
//                    capRetry++
//                    continue
//                }
//
//            } while (error == FingerprintScanner.NO_FINGER)
//
//            mScanner.finish()
//            if (isTimeout) {
//                mScanner.setLedStatus(1, 0)
//                Handler(Looper.getMainLooper()).post {
//                    Toast.makeText(MyApp.getContext(), "Capture finger timeout", Toast.LENGTH_LONG).show()
//                }
//                break
//            }
//
//            fi?.let { fingerprintImage ->
//                val startTime = System.currentTimeMillis()
//                val res = Bione.extractFeature(fingerprintImage)
//                if (res.error != Bione.RESULT_OK) {
//                    // Alih-alih menggunakan break, gunakan return untuk keluar dari lambda
//                    return@let // atau gunakan mekanisme lain untuk menandai error dan keluar dari blok
//                }
//                fpFeat = res.data as ByteArray
//            }
//
//            if (fpFeat == null) break
//
//            val res = Bione.extractIsoFeature(fi!!)
//            if (res.error != Bione.RESULT_OK) break
//            fpTemp = res.data as ByteArray
//
//            featureEnroll = fpTemp
//            Log.e(TAG, "feature = ${StringUtils.convertBytesToHex(featureEnroll)}")
//            mScanner.setLedStatus(1, 0)
//
//        } while (false)
//
//        return featureEnroll
//    }

    fun captureFinger(dialog: ProgressDialog?): ByteArray? {
        mScanner.setLedStatus(1, 1) // Hijau
        var startTime: Long
        var extractTime: Long = -1
        var generalizeTime: Long = -1
        var fpFeat: ByteArray? = null
        var fpTemp: ByteArray? = null
        var res: Result
        var error = 0
        isTimeout = false

        do {
            var capRetry = 0
            mScanner.prepare()

            var timeout = fingerTimeout
            do {
                val time = System.currentTimeMillis()
                res = mScanner.capture()
                val endTime = System.currentTimeMillis()
                Log.i("Sanny", "capture time: ${endTime - time}")
                error = res.error

                if (error == FingerprintScanner.TIMEOUT) {
                    Log.e(TAG, "Timeout occurred: $timeout")
                    timeout--

                    if (timeout == 0) {
                        isTimeout = true
                        Log.e(TAG, "Timeout happened: $isTimeout")
                        break
                    }
                    try {
                        Thread.sleep(10)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                    continue
                }

                fi = res.data as FingerprintImage?
                mLastImage = fi
                val quality: Int

                if (fi != null) {
                    quality = Bione.getFingerprintQuality(fi!!)
                    Log.i(TAG, "Fingerprint image quality is $quality")

                    if (quality < 50 && capRetry < 3) {
                        capRetry++
                        continue
                    }
                }
                if (error != FingerprintScanner.NO_FINGER) {
                    Log.i("Sanny", "Error code: $error")
                    break
                }

            } while (true)

            mScanner.finish()
            mLastImage = fi

            Log.e(TAG, "isTimeout: $isTimeout")

            if (isTimeout) {
                mScanner.setLedStatus(1, 0) // Hijau
                dialog?.dismiss()

//                Handler(Looper.getMainLooper()).post {
//                    Toast.makeText(MyApp.getContext(), "Capture finger timeout", Toast.LENGTH_LONG).show()
//                }
                break
            }

            if (fi != null) {
                startTime = System.currentTimeMillis()
                res = Bione.extractFeature(fi!!)
                extractTime = System.currentTimeMillis() - startTime

                if (res.error != Bione.RESULT_OK) {
                    break
                }

                fpFeat = res.data as ByteArray
            }

            if (fpFeat == null) break

            startTime = System.currentTimeMillis()
            res = fi?.let { Bione.extractIsoFeature(it) }!!
            generalizeTime = System.currentTimeMillis() - startTime

            if (res.error != Bione.RESULT_OK) {
                break
            }

            fpTemp = res.data as ByteArray
            featureEnroll = fpTemp

            Log.e(TAG, "Feature = ${StringUtils.convertBytesToHex(featureEnroll)}")

            mScanner.setLedStatus(1, 0) // Ijo
        } while (false)

        return featureEnroll
    }

    fun matchFingerprint(feature: ByteArray, iicFinger: ByteArray): Int {
        val res = Bione.verifyIsoFeature(feature, iicFinger)
        Log.e(TAG, "matchFingerprint data = ${res.data}")
        Log.e(TAG, "matchFingerprint arg1 = ${res.arg1}")
        Log.e(TAG, "matchFingerprint arg2 = ${res.arg2}")
        return res.arg1
    }

    private inner class Fingerprint : AsyncTask<String, Int, Void?>() {
        private var mIsDone = false

        override fun doInBackground(vararg params: String): Void? {
            mScanner.setLedStatus(1, 1)
            var error: Int
            var fpFeat: ByteArray? = null

            do {
                val capRetry = 0
                mScanner.prepare()
                var timeout = 100 * 30

                do {
                    val res = mScanner.capture()
                    error = res.error

                    if (error == FingerprintScanner.TIMEOUT) {
                        timeout--
                        if (timeout == 0) break
                        Thread.sleep(10)
                        continue
                    }

                    fi = res.data as FingerprintImage?

                    if (fi != null) {
                        val quality = Bione.getFingerprintQuality(fi!!)
                        if (quality < 50 && capRetry < 3 && !isCancelled) continue
                    }

                } while (error == FingerprintScanner.NO_FINGER || isCancelled)

                mScanner.finish()

                if (!isCancelled && error != FingerprintScanner.RESULT_OK) {
                    Log.e(TAG, "capture_image_failed")
                } else {
                    mLastImage = fi
                }

                if (params[0] == "enroll" || params[0] == "verify" || params[0] == "identify") {
                    fi?.let {
                        val res = Bione.extractFeature(it)
                        if (res.error != Bione.RESULT_OK) {
                            // Keluar dari let jika terjadi error
                            return@let
                        }
                        fpFeat = res.data as ByteArray
                    }
                }

            } while (false)

            mIsDone = true
            return null
        }

        fun waitForDone() {
            while (!mIsDone) {
                Thread.sleep(50)
            }
        }
    }
}

//object FingerPrintTask {
//
//    private var mScanner: FingerprintScanner? = null
//    private val mFingerprintTask: FingerPrintTask = FingerPrintTask()
//    var fi: FingerprintImage? = null
//    private val fingerprint: Fingerprint? = null
//    var mLastImage: FingerprintImage? = null
//    private val TAG = FingerPrintTask::class.java.name
//    var featureEnroll: ByteArray? = null
//
//    private fun FingerPrintTask(): FingerPrintTask {
//        mScanner = FingerprintScanner.getInstance(MyApp.getContext())
//        return mFingerprintTask
//    }
//
//    fun instance(): FingerPrintTask? {
//        return mFingerprintTask
//    }
//
//    fun openDevice() {
//        object : Thread() {
//            @SuppressLint("NewApi")
//            @RequiresApi(api = Build.VERSION_CODES.M)
//            override fun run() {
//                synchronized(this) {
//
////                    showProgressDialog(getString(R.string.loading), getString(R.string.preparing_device));
//                    var error: Int
//                    if (mScanner!!.open().also { error = it } != FingerprintScanner.RESULT_OK) {
//                        Log.e(
//                            TAG,
//                            "fingerprint_device_open_failed " + getFingerprintErrorString(
//                                MyApp.getContext().getApplicationContext(), error
//                            )
//                        )
//                    }
//                    if (Bione.initialize(MyApp.getContext().getApplicationContext(), FP_DB_PATH)
//                            .also {
//                                error = it
//                            } != Bione.RESULT_OK
//                    ) {
//                        Log.e(
//                            TAG,
//                            "string.algorithm_initialization_failed " + getFingerprintErrorString(
//                                MyApp.getContext().getApplicationContext(), error
//                            )
//                        )
//                    }
//                    Log.i(TAG, "Fingerprint algorithm version: " + Bione.getVersion())
//                }
//            }
//        }.start()
//    }
//
//    fun closeDevice() {
//        object : Thread() {
//            override fun run() {
//                synchronized(this) {
//                    var error: Int
//                    if (fingerprint != null && fingerprint.getStatus() != AsyncTask.Status.FINISHED) {
//                        fingerprint.cancel(false)
//                        fingerprint.waitForDone()
//                    }
//                    if (mScanner!!.close().also { error = it } != FingerprintScanner.RESULT_OK) {
////                        showError(getString(R.string.fingerprint_device_close_failed), getFingerprintErrorString(error));
////                        Log.e(TAG, "fingerprint_device_close_failed" + getFingerprintErrorString(error))
//                    } else {
////                        showInformation(getString(R.string.fingerprint_device_close_success), null);
//                    }
//                    if (mScanner!!.powerOff().also { error = it } != FingerprintScanner.RESULT_OK) {
////                        showError(getString(R.string.fingerprint_device_power_off_failed), getFingerprintErrorString(error));
//                    }
//                    if (Bione.exit().also { error = it } != Bione.RESULT_OK) {
////                        showError(getString(R.string.algorithm_cleanup_failed), getFingerprintErrorString(error));
//                    }
//                }
//            }
//        }.start()
//    }
//
//    fun captureFinger(): ByteArray? {
//        mScanner!!.setLedStatus(1, 1) // Green
//        var startTime = System.currentTimeMillis()
//        var extractTime: Long = -1
//        var generalizeTime: Long = -1
//        var fpFeat: ByteArray? = null
//        var fpTemp: ByteArray? = null
//        var res: Result
//        var error = 0
//        var mIsDone = false
//        val timeout: Int = fingerTimeout
//        var fi: FingerprintImage? = null
//        while (true) {
//            var capRetry = 0
//            mScanner!!.prepare()
//            while (true) {
//                res = mScanner!!.capture()
//                val currentTime = System.currentTimeMillis()
//                Log.i("Sanny", "capture time:" + (currentTime - startTime))
//                error = res.error
//                if (error == FingerprintScanner.TIMEOUT) {
//                    Log.i(
//                        "FingerprintTas",
//                        "Timeout occurred. fingerTimeout:$fingerTimeout"
//                    )
//                    if (currentTime - startTime >= timeout) {
//                        Log.i("FingerprintTas", "Capture process timed out.")
//                        break
//                    }
//                    try {
//                        Thread.sleep(10)
//                    } catch (e: InterruptedException) {
//                        e.printStackTrace()
//                    }
//                    continue
//                }
//                fi = res.data as FingerprintImage
//                mLastImage = fi
//                var quality: Int
//                if (fi != null) {
//                    quality = Bione.getFingerprintQuality(fi)
//                    Log.i(TAG, "Fingerprint image quality is $quality")
//                    if (quality < 50 && capRetry < 3) {
//                        capRetry++
//                        continue
//                    }
//                }
//                if (error != FingerprintScanner.NO_FINGER) {
//                    Log.i("Sanny", "Error occurred during capture: $error")
//                    break
//                }
//            }
//            mScanner!!.finish()
//            mLastImage = fi
//            if (fi == null || fpFeat == null && res.error != Bione.RESULT_OK) {
////                mScanner.setLedStatus(1,0);
//                Log.i(TAG, "Failed to capture fingerprint feature or no finger detected.")
//                break
//            }
//            if (fi != null) {
//                startTime = System.currentTimeMillis()
//                res = Bione.extractFeature(fi)
//                extractTime = System.currentTimeMillis() - startTime
//                if (res.error != Bione.RESULT_OK) {
//                    Log.i(TAG, "Failed to extract feature. Error: " + res.error)
//                    break
//                }
//                fpFeat = res.data as ByteArray
//            }
//            if (fpFeat == null) {
//                Log.i(TAG, "Failed to capture fingerprint feature.")
//                break
//            }
//            startTime = System.currentTimeMillis()
//            res = Bione.extractIsoFeature(fi)
//            generalizeTime = System.currentTimeMillis() - startTime
//            if (res.error != Bione.RESULT_OK) {
//                Log.i(TAG, "Failed to extract ISO feature. Error: " + res.error)
//                break
//            }
//            fpTemp = res.data as ByteArray
//            featureEnroll = fpTemp
//            Log.e(TAG, "Feature enrolled: " + StringUtils.convertBytesToHex(featureEnroll))
//            mScanner!!.setLedStatus(1, 0) // Green
//            break // Exit the outer loop if capture is successful
//        }
//        mIsDone = true
//        return featureEnroll
//    }
//
//    fun matchFingerprint(feature: ByteArray?, iicFinger: ByteArray?): Int {
//        val res: Result
//        res = Bione.verifyIsoFeature(feature!!, iicFinger!!)
//        matchingScore = res.arg1.toString()
//        Log.e(TAG, "matchFingerprint = " + res.data)
//        Log.e(TAG, "matchFingerprint = " + res.arg1)
//        Log.e(TAG, "matchFingerprint = " + res.arg2)
//        return res.arg1
//    }
//
//    private class Fingerprint : AsyncTask<String?, Int?, Void?>() {
//        private var mIsDone = false
//
//        override fun doInBackground(vararg params: String?): Void? {
//            mScanner!!.setLedStatus(1, 1) //Green
//            var startTime: Long
//            var extractTime: Long = -1
//            val generalizeTime: Long = -1
//            var fpFeat: ByteArray? = null
//            val fpTemp: ByteArray? = null
//            var res: Result
//            var error = 0
//            do {
//                if (params[0] == "show" || params[0] == "enroll" || params[0] == "verify" || params[0] == "identify") {
//                    val capRetry = 0
//                    mScanner!!.prepare()
//                    //  long time = System.currentTimeMillis();
////                    long timeout = 100 * 10;
////                    long timeout = 1000L * fingerTimeout;
//                    var timeout: Int = fingerTimeout
//                    do {
//                        val time = System.currentTimeMillis()
//                        res = mScanner!!.capture()
//                        val endTime = System.currentTimeMillis()
//                        Log.i("Sanny", "capture time:" + (endTime - time))
//                        error = res.error
//                        if (error == FingerprintScanner.TIMEOUT) {
//                            timeout--
//                            mScanner!!.setLedStatus(1, 0) //ijo
//                            if (timeout == 0) break
//                            try {
//                                Thread.sleep(10)
//                            } catch (e: InterruptedException) {
//                                e.printStackTrace()
//                            }
//                            //
//                            continue
//                        }
//                        fi = res.data as FingerprintImage
//                        var quality: Int
//                        if (fi != null) {
//                            quality = Bione.getFingerprintQuality(fi!!)
//                            Log.i(
//                                TAG,
//                                "Fingerprint image quality is $quality"
//                            )
//
////                            if (quality < 50 && capRetry < 3 && !isCancelled()) {
////                                capRetry++;
////                                continue;
////                            }
//                        }
//                        if (error != FingerprintScanner.NO_FINGER || isCancelled) {
//                            Log.i("Sanny", "error 2222:$error")
//                            break
//                        }
//                        if (isCancelled) {
//                            Log.i("Sanny", "error 3333333333:$error")
//                            break
//                        }
//                    } while (true)
//                    mScanner!!.finish()
//                    if (!isCancelled && error != FingerprintScanner.RESULT_OK) {
////                        showError(getString(R.string.capture_image_failed), getFingerprintErrorString(error));
//                    } else {
//                        mLastImage = fi
//                    }
//                }
//                if (params[0] == "enroll" || params[0] == "verify" || params[0] == "identify") {
//                    if (fi != null) {
//                        startTime = System.currentTimeMillis()
//                        res = Bione.extractFeature(fi!!)
//                        extractTime = System.currentTimeMillis() - startTime
//                        if (res.error != Bione.RESULT_OK) {
////                            showError(getString(R.string.enroll_failed_because_of_extract_feature), getFingerprintErrorString(res.error));
//                            break
//                        }
//                        fpFeat = res.data as ByteArray
//                    }
//                }
//            } while (false)
//            mIsDone = true
//            return null
//        }
//
//
//        fun waitForDone() {
//            while (!mIsDone) {
//                try {
//                    Thread.sleep(50)
//                } catch (e: InterruptedException) {
//                    Thread.currentThread().interrupt()
//                }
//            }
//        }
//    }
//
//}
