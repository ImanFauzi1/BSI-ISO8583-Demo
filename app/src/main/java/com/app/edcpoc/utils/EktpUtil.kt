package com.app.edcpoc.utils

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.SystemClock
import android.util.Base64
import android.util.Log
import android.view.KeyEvent
import android.widget.EditText
import android.widget.LinearLayout
import cn.com.aratek.fp.FingerprintImage
import cn.com.aratek.fp.FingerprintScanner
import com.app.edcpoc.MyApp
import com.app.edcpoc.PreferenceManager
import com.app.edcpoc.R
import com.app.edcpoc.utils.Constants.READ_TIMEOUT
import com.app.edcpoc.utils.Constants.cardInfoEntity
import com.app.edcpoc.utils.Constants.indonesianIdentityCard
import com.app.edcpoc.utils.Constants.isTimeout
import com.app.edcpoc.utils.Constants.mCardReadManager
import com.app.edcpoc.utils.Constants.mCardType
import com.app.edcpoc.utils.Constants.mICCard
import com.app.edcpoc.utils.Constants.mRfCard
import com.app.edcpoc.utils.Constants.mRfCardType
import com.app.edcpoc.utils.Constants.mScanner
import com.app.edcpoc.utils.Utility.countDownTimer
import com.app.edcpoc.utils.fingerprint.FingerPrintTask
import com.simo.ektp.EktpSdkZ90
import com.simo.ektp.GlobalVars.VALUE_AGAMA
import com.simo.ektp.GlobalVars.VALUE_ALAMAT
import com.simo.ektp.GlobalVars.VALUE_BIOMETRIC_LEFT
import com.simo.ektp.GlobalVars.VALUE_BIOMETRIC_RIGHT
import com.simo.ektp.GlobalVars.VALUE_FOTO
import com.simo.ektp.GlobalVars.VALUE_GOL_DARAH
import com.simo.ektp.GlobalVars.VALUE_JNS_KELAMIN
import com.simo.ektp.GlobalVars.VALUE_KAB
import com.simo.ektp.GlobalVars.VALUE_KEC
import com.simo.ektp.GlobalVars.VALUE_KEL
import com.simo.ektp.GlobalVars.VALUE_NAMA
import com.simo.ektp.GlobalVars.VALUE_NATIONALITY
import com.simo.ektp.GlobalVars.VALUE_NIK
import com.simo.ektp.GlobalVars.VALUE_PEKERJAAN
import com.simo.ektp.GlobalVars.VALUE_PROV
import com.simo.ektp.GlobalVars.VALUE_RT
import com.simo.ektp.GlobalVars.VALUE_RW
import com.simo.ektp.GlobalVars.VALUE_SIGNATURE
import com.simo.ektp.GlobalVars.VALUE_STATUS
import com.simo.ektp.GlobalVars.VALUE_TGL_LAHIR
import com.simo.ektp.GlobalVars.VALUE_TMP_LAHIR
import com.simo.ektp.GlobalVars.VALUE_VALID_UNTIL
import com.app.edcpoc.utils.Constants.base64Finger
import com.app.edcpoc.utils.Constants.psamProfile
import com.app.edcpoc.utils.Utility.readPsamConfigSuccess
import com.app.edcpoc.utils.Utility.startCountDownTimer
import com.simo.ektp.GlobalVars.CONFIG_FILE
import com.simo.ektp.GlobalVars.PCID
import com.simo.ektp.GlobalVars.mHits
import com.simo.ektp.IndonesianIdentityCard
import com.zcs.sdk.DriverManager
import com.zcs.sdk.SdkData
import com.zcs.sdk.SdkResult
import com.zcs.sdk.Sys
import com.zcs.sdk.card.CardInfoEntity
import com.zcs.sdk.card.CardReaderTypeEnum
import com.zcs.sdk.listener.OnSearchCardListener
import java.io.ByteArrayOutputStream
import java.util.Arrays

object EktpUtil {

    private const val TAG = "EktpUtil"
    lateinit var mSys: Sys
    var mKtpDialog: ProgressDialog? = null

    fun initialize() {
        val mDriverManager = DriverManager.getInstance()
        mSys = mDriverManager.baseSysDevice

        indonesianIdentityCard = IndonesianIdentityCard()
        mScanner = FingerprintScanner.getInstance(MyApp.getContext())

        mCardReadManager = mDriverManager.cardReadManager
        mICCard = mCardReadManager.icCard
        mRfCard = mCardReadManager.rfCard
        cardInfoEntity = CardInfoEntity()
    }

    fun ektpOpen(){

        FingerPrintTask.instance.openDevice()

        if (!::mSys.isInitialized) {
            Log.e(TAG, "mSys is not initialized. Make sure to initialize it first.")
            return
        }

            var status = mSys.sdkInit()
            if (status != SdkResult.SDK_OK) {
                mSys.sysPowerOn()
                try {
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                status = mSys.sdkInit()

            } else {
                Log.e(TAG, "Open Device Success")
            }
            if (status != SdkResult.SDK_OK) {
                Log.e(TAG, "Init SDK Failed")
                Log.e(TAG, "Open Device Failed")
            }
    }

//    fun startCardSearch() {
//        EktpSdkZ90.instance().closePsamAndRfidReaders()
//        Executors.newSingleThreadExecutor().submit {
//            searchBankCard(
//                CardReaderTypeEnum.RF_CARD
//            )
//        }
//    }

    fun searchBankCard(cardType: CardReaderTypeEnum, dialog: ProgressDialog) {
        mKtpDialog = dialog
        mCardType = cardType
        mRfCardType = (SdkData.RF_TYPE_A.toInt() or SdkData.RF_TYPE_B.toInt()).toByte()
        mCardReadManager.cancelSearchCard()
        mCardReadManager.searchCard(cardType, READ_TIMEOUT, mListener)

        // Start the countdown timer
        isTimeout = false
        startCountDownTimer(READ_TIMEOUT) {
            // This block will be executed when the timer finishes
            Log.e(TAG, "Card read operation timed out.")
            mCardReadManager.cancelSearchCard()
            mKtpDialog?.cancel()
            mKtpDialog = null
            isTimeout = true
        }
    }

    private val mListener = object : OnSearchCardListener {
        override fun onCardInfo(cardInfoEntity: CardInfoEntity) {
//            mKtpDialog?.setMessage("KTP terdeteksi, mohon tunggu sebentar...")
            val cardType = cardInfoEntity.cardExistslot
            val rfCardType = cardInfoEntity.rfCardType

            when (cardType) {
                CardReaderTypeEnum.RF_CARD -> {
                    Log.e(TAG, "rfCardType: $rfCardType")

                    getEktpStrData()

                    // Cancel the countdown timer since the card was detected
                    countDownTimer?.cancel()

                    // Handle the card search result or any other logic here if needed
                    mKtpDialog?.cancel()
                }
                else -> {
                    Log.e(TAG, "Unsupported card type: $cardType")
                }
            }
//            mKtpDialog = null
        }

        override fun onError(errorCode: Int) {

        }

        override fun onNoCard(cardReaderTypeEnum: CardReaderTypeEnum?, p1: Boolean) {
            Log.e(TAG, "No card detected: $cardReaderTypeEnum")
        }
    }

    private fun getEktpStrData() {
        indonesianIdentityCard = EktpSdkZ90.instance().EktpReadCardStepOne()
        indonesianIdentityCard = EktpSdkZ90.instance().EktpReadCardStepTwo(indonesianIdentityCard)
    }

    fun setData(iic: IndonesianIdentityCard) {
        VALUE_NIK = iic.id
        VALUE_NAMA = iic.name
        VALUE_TMP_LAHIR = iic.placeOfBirth
        VALUE_TGL_LAHIR = iic.dateOfBirth
        VALUE_JNS_KELAMIN = iic.gender
        VALUE_GOL_DARAH = iic.bloodType
        VALUE_ALAMAT = iic.address
        VALUE_RT = iic.neighbourhood
        VALUE_RW = iic.communityAssociation
        VALUE_KEL = iic.village
        VALUE_KEC = iic.district
        VALUE_KAB = iic.city
        VALUE_PROV = iic.province
        VALUE_AGAMA = iic.religion
        VALUE_STATUS = iic.marriageStatus
        VALUE_PEKERJAAN = iic.occupation
        VALUE_NATIONALITY = iic.nationality
        VALUE_VALID_UNTIL = iic.dateOfExpiry
        VALUE_FOTO = iic.mPhotographData
        VALUE_SIGNATURE = iic.mSignature
        VALUE_BIOMETRIC_LEFT = iic.mLeftFinger
        VALUE_BIOMETRIC_RIGHT = iic.mRightFinger
    }

    fun updateFingerprintImage(fi: FingerprintImage?) {
        var fpBmp: ByteArray? = null
        var bitmap: Bitmap? = null
        if (fi == null || fi.convert2Bmp()
                .also { fpBmp = it } == null || BitmapFactory.decodeByteArray(
                fpBmp,
                0,
                fpBmp!!.size
            ).also {
                bitmap = it
            } == null
        ) {
            bitmap = BitmapFactory.decodeResource(MyApp.getContext().resources, R.drawable.nofinger)
        }
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        base64Finger = Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    fun checkPsamConfiguration(context: Context) {
        if (!readPsamConfigSuccess(context)) {
            showPsamConfigurationDialog(context)
        }
    }

    private fun showPsamConfigurationDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("Reading $psamProfile Failed")
            .setMessage("Reading \"$psamProfile\" failed. \n\n Please put $psamProfile in SD card root directory and import it.")
            .setIcon(R.drawable.ic_dialog_alert_yellow)
            .setOnKeyListener { _, keyCode, keyEvent ->
                handlePsamDialogKeyPress(keyCode, keyEvent)
            }
            .setPositiveButton("Import") { _, _ ->
                showManualPsamInputDialog(context)
            }
            .setNegativeButton("Exit") { dialog, _ ->
                if (context is android.app.Activity) {
                    context.finish()
                }
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun handlePsamDialogKeyPress(keyCode: Int, keyEvent: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_6 && keyEvent.action == KeyEvent.ACTION_DOWN) {
            System.arraycopy(mHits, 1, mHits, 0, mHits.size - 1)
            mHits[mHits.size - 1] = SystemClock.uptimeMillis()
            LogUtils.d(TAG, "System time: ${SystemClock.uptimeMillis()}")
            if (mHits[0] >= SystemClock.uptimeMillis() - 5000) {
                Arrays.fill(mHits, 0)
            }
        }
        return false
    }

    @SuppressLint("NewApi")
    private fun showManualPsamInputDialog(context: Context) {
        val inputLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 16, 32, 16)
        }
        val pcidEditText = EditText(context).apply {
            hint = "PCID"
            setText("2024BB12121100000000000000095067")
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 16 }
        }
        val configEditText = EditText(context).apply {
            hint = "Config"
            setText("E743E49AC5FD1A180D28AB938B1F3F6FC6F008D3BE955670BE598C9E084837F1")
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        inputLayout.addView(pcidEditText)
        inputLayout.addView(configEditText)

        AlertDialog.Builder(context)
            .setTitle("Manual PSAM Input")
            .setView(inputLayout)
            .setMessage("Masukkan PCID dan Config secara manual.")
            .setPositiveButton("OK") { dialog, _ ->
                PCID = pcidEditText.text.toString()
                CONFIG_FILE = configEditText.text.toString()
                PreferenceManager.setPCID(context, PCID)
                PreferenceManager.setConfigFile(context, CONFIG_FILE)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                if (context is android.app.Activity) {
                    context.finish()
                }
            }
            .show()
    }
}