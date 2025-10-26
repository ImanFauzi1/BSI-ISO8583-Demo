package com.app.edcpoc.utils

import android.app.ProgressDialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.app.edcpoc.MyApp
import com.app.edcpoc.interfaces.EmvUtilInterface
import com.app.edcpoc.utils.Constants.CHANGE_PIN
import com.app.edcpoc.utils.Constants.CREATE_PIN
import com.app.edcpoc.utils.Constants.END_DATE
import com.app.edcpoc.utils.Constants.LOGOFF
import com.app.edcpoc.utils.Constants.LOGON
import com.app.edcpoc.utils.Constants.READ_TIMEOUT
import com.app.edcpoc.utils.Constants.REISSUE_PIN
import com.app.edcpoc.utils.Constants.START_DATE
import com.app.edcpoc.utils.Constants.VERIFY_PIN
import com.app.edcpoc.utils.Constants.cardInfoEntity
import com.app.edcpoc.utils.Constants.cardNum
import com.app.edcpoc.utils.Constants.commandValue
import com.app.edcpoc.utils.Constants.emvHandler
import com.app.edcpoc.utils.Constants.iRet
import com.app.edcpoc.utils.Constants.mCardReadManager
import com.app.edcpoc.utils.Constants.mCardType
import com.app.edcpoc.utils.Constants.mDriverManager
import com.app.edcpoc.utils.Constants.mICCard
import com.app.edcpoc.utils.Constants.mPinPadManager
import com.app.edcpoc.utils.Constants.mPrinter
import com.app.edcpoc.utils.Constants.mRfCard
import com.app.edcpoc.utils.Constants.mRfCardType
import com.app.edcpoc.utils.Constants.mSys
import com.app.edcpoc.utils.Constants.pos_entrymode
import com.app.edcpoc.utils.Constants.realCardType
import com.app.edcpoc.utils.Constants.track2hex
import com.app.edcpoc.utils.Constants.aids
import com.app.edcpoc.utils.Constants.field48data
import com.app.edcpoc.utils.Constants.field55hex
import com.app.edcpoc.utils.Constants.inputPINResult
import com.app.edcpoc.utils.Constants.mLatch
import com.app.edcpoc.utils.Constants.mPinBlock
import com.app.edcpoc.utils.Constants.pinBlockConfirm
import com.app.edcpoc.utils.Constants.pinBlockNew
import com.app.edcpoc.utils.Constants.pinBlockOwn
import com.app.edcpoc.utils.Constants.spvCardNum
import com.app.edcpoc.utils.Constants.spvPinBlockOwn
import com.app.edcpoc.utils.Constants.step
import com.app.edcpoc.utils.Constants.tags
import com.app.edcpoc.utils.Constants.track2data
import com.app.edcpoc.utils.Constants.track2datacustomer
import com.google.gson.Gson
import com.simo.ektp.Utils
import com.zcs.sdk.SdkData
import com.zcs.sdk.SdkResult
import com.zcs.sdk.card.CardInfoEntity
import com.zcs.sdk.card.CardReaderTypeEnum
import com.zcs.sdk.card.CardSlotNoEnum
import com.zcs.sdk.emv.EmvApp
import com.zcs.sdk.emv.EmvCapk
import com.zcs.sdk.emv.EmvData
import com.zcs.sdk.emv.EmvHandler
import com.zcs.sdk.emv.EmvResult
import com.zcs.sdk.emv.EmvTermParam
import com.zcs.sdk.emv.EmvTransParam
import com.zcs.sdk.emv.OnEmvListener
import com.zcs.sdk.listener.OnSearchCardListener
import com.zcs.sdk.pin.PinAlgorithmMode
import com.zcs.sdk.pin.pinpad.PinPadManager.OnPinPadInputListener
import com.zcs.sdk.util.MessageDigestUtils
import com.zcs.sdk.util.StringUtils
import java.io.File
import java.io.IOException
import java.util.Locale
import kotlin.collections.set

/**
 * Utility class for EMV operations. Pass an Activity or Fragment context if you need to show dialogs or UI elements.
 * For non-UI operations, applicationContext will be used automatically.
 */
class EmvUtil(private val context: Context) {
    private val TAG = "EmvUtil"
    private val appContext = MyApp.getContext()
    private var dialog: ProgressDialog? = null
    private var callback: EmvUtilInterface? = null

    fun setCallback(callback: EmvUtilInterface) {
        this.callback = callback
    }

    fun initialize() {
        // Use appContext for system/device operations
        mSys = mDriverManager.baseSysDevice
        mPrinter = mDriverManager.printer
        mPinPadManager = mDriverManager.padManager
        mCardReadManager = mDriverManager.cardReadManager
        mICCard = mCardReadManager.icCard
        mRfCard = mCardReadManager.rfCard
        cardInfoEntity = CardInfoEntity()
        emvHandler = EmvHandler.getInstance()
    }

    private fun showProgressDialog(message: String) {
        // Use passed context for UI elements
        dialog = ProgressDialog(context).apply {
            setMessage(message)
            setCancelable(false)
            show()
        }
    }

    fun emvOpen() {
        var status: Int = mSys.sdkInit()
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

    fun searchBankCard(cardType: CardReaderTypeEnum, progressDialog: ProgressDialog) {
        dialog = progressDialog
        mCardType = cardType
        mRfCardType = (SdkData.RF_TYPE_A.toInt() or SdkData.RF_TYPE_B.toInt()).toByte()
        mCardReadManager.cancelSearchCard()
        mCardReadManager.searchCard(cardType, READ_TIMEOUT, mListener)
    }

    private var mListener: OnSearchCardListener = object : OnSearchCardListener {
        override fun onCardInfo(cardInfoEntity: CardInfoEntity?) {
            Log.e(TAG, "Emv commandValue result = $commandValue")
            Log.e(TAG, "Deleted card ${realCardType?.name}")

            realCardType = cardInfoEntity?.cardExistslot
            Log.d(TAG, "onCardInfo: realCardType $realCardType")

            when (realCardType) {
                CardReaderTypeEnum.RF_CARD -> {
                    pos_entrymode = "071"
                    readRf()
                    dismissDialogsBasedOnCommand()
                }

                CardReaderTypeEnum.MAG_CARD -> {
                    pos_entrymode = "021"
                    getMagData()
                    dismissDialogsBasedOnCommand(includeRegistrasiPIN = true)
                }

                CardReaderTypeEnum.IC_CARD -> {
                    pos_entrymode = "011"
                    readIc()
                    dismissDialogsBasedOnCommand(includeRegistrasiPIN = true)
                }

                else -> {
                    // Handle other cases if needed
                }
            }
        }

        override fun onError(resultCode: Int) {
            cancelSearchCard()
        }

        override fun onNoCard(cardType: CardReaderTypeEnum?, isPresent: Boolean) {
            // Handle the no card scenario if needed
        }
    }

    private fun dismissDialogsBasedOnCommand(includeRegistrasiPIN: Boolean = false) {
        dialog?.dismiss()
    }
    fun cancelSearchCard() {
        mCardReadManager.cancelSearchCard()
        Log.d(TAG, "cancelSearchCard:  ")
//        if (commandValue.equals(CREATE_PIN) || commandValue.equals(CHANGE_PIN)) {
//            PinFragment.mReaderEmvProcessDialog.dismiss()
//        } else {
//            TransaksiFragment.mReaderTrxProcessDialog.dismiss()
//        }
    }
    fun readRf() {
        val resetData = ByteArray(EmvData.BUF_LEN)
        val datalength = IntArray(1)
        iRet = mRfCard.rfReset(resetData, datalength)

        if (iRet !== 0) {
            Log.e(TAG, "rf reset error")
            //            closeDialog();
            return
        }

        Log.d("Debug", "RF resetData: " + StringUtils.convertBytesToHex(resetData) + "iret=$iRet")
        realCardType?.let { emv(it) }
    }
    fun readIc() {
        iRet = mICCard.icCardReset(CardSlotNoEnum.SDK_ICC_USERCARD)
        if (iRet !== 0) {
            Log.e(TAG, "ic reset error")
            //            closeDialog();
            return
        }
        realCardType?.let { emv(it) }
    }

    private fun getMagData() {
        Log.d("Debug", "MAG_CARD")
        Log.d(TAG, "Mag card swipe")
//        Log.d(TAG, "Mag stateCard $stateCard")
        val magReadData = mCardReadManager.magCard.magReadData
        cardInfoEntity = magReadData
        if (magReadData.resultcode == SdkResult.SDK_OK) {
            val tk1 = magReadData.tk1
            val tk2 = magReadData.tk2
            val tk3 = magReadData.tk3
            val expiredDate = magReadData.expiredDate

            cardNum = magReadData.cardNo

            Log.d(TAG, "tk1:  $tk1")
            Log.d(TAG, "tk2:  $tk2")
            Log.d(TAG, "tk3:  $tk3")
            Log.d(TAG, "expiredDate:  $expiredDate")
            Log.d(TAG, "cardNo:  $cardNum")

            when(commandValue) {
                CREATE_PIN, REISSUE_PIN -> {
                    if (step == 2) {
                        track2data = tk2
                        Log.d("Debug", "step=$step;commandValue=$commandValue;track2datasvp=$track2data")
                        inputPIN()
                    } else {
                        inputNewPIN()
                    }
                }
                else -> {
                    track2data = tk2
                    track2hex = StringUtils.convertStringToHex(tk2)
                    val pinResult = inputPIN()
                    Log.d(TAG, "Input PIN result: $pinResult")
                }
            }
        } else {
            callback?.onError("Mag card read error: ${magReadData.resultcode}")
            Log.e(TAG, "Mag card read error:  " + magReadData.resultcode)
        }
    }

    private fun emv(cardType: CardReaderTypeEnum) {
        // 1. copy aid and capk to '/sdcard/emv/' as the default aid and capk
        Log.i("transaction", " copy aid and capk")
        try {
            if (!File(EmvTermParam.emvParamFilePath).exists()) {
                FileUtils.doCopy(appContext, "emv", EmvTermParam.emvParamFilePath)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // 2. set params
        Log.i("transaction", " set params")
        val emvTransParam = EmvTransParam()
        if (cardType == CardReaderTypeEnum.IC_CARD) {
            emvTransParam.transKernalType = EmvData.KERNAL_EMV_PBOC
        }
        if (cardType == CardReaderTypeEnum.RF_CARD) {
            emvTransParam.transKernalType = EmvData.KERNAL_CONTACTLESS_ENTRY_POINT
        }
        emvHandler?.transParamInit(emvTransParam)
        val emvTermParam = EmvTermParam()
        emvHandler?.kernelInit(emvTermParam)

        // 3. add aid or capk
        Log.i("transaction", " add aid or capk")
        emvHandler?.delAllApp()
        emvHandler?.delAllCapk()
        emvHandler?.let { loadAids(it) }
        emvHandler?.let { loadVisaAIDs(it) }
        emvHandler?.let { loadAnnexCapk(it) }
        emvHandler?.let { loadMasterCardCapks(it) }
        emvHandler?.let { loadMasterCardCapks1(it) }
        pinpadWork()

        // 4. transaction
        Log.i("transaction", "Debug" + "onSelApp")
        val pucIsEcTrans = ByteArray(1)
        val pucBalance = ByteArray(6)
        val pucTransResult = ByteArray(1)

        val onEmvListener: OnEmvListener = object : OnEmvListener {
            override fun onSelApp(appLabelList: Array<String>): Int {
                Log.d(TAG, "onSelApp")
                return iRet
            }

            override fun onConfirmCardNo(cardNo: String): Int {
                Log.d(TAG, "onConfirmCardNo")
                val track2 = arrayOfNulls<String>(1)
                val pan = arrayOfNulls<String>(1)
                emvHandler?.getTrack2AndPAN(track2, pan)
                var index = 0
                if (track2[0]!!.contains("D")) {
                    index = track2[0]!!.indexOf("D") + 1
                } else if (track2[0]!!.contains("=")) {
                    index = track2[0]!!.indexOf("=") + 1
                }
                val exp = track2[0]!!.substring(index, index + 4)
                Log.e(TAG, "cardNum:" + pan[0])
                Log.e(TAG, "exp:$exp")

                track2data = track2[0]
                Log.d("Debug", "track2data : $track2data")
                track2hex = StringUtils.convertStringToHex(track2data)

                Log.d("Debug", "track2hex : $track2hex")
                cardNum = pan[0]
                Log.e(TAG, "cardNum:$cardNum")
                //                cardExp = exp;
                return 0

//                363031393030373536333931363437383d3237303632323038343334323130323335303031
//                        6019007563916478=27062208434210235001
            }

            override fun onInputPIN(pinType: Byte): Int {
                // 1. open the secret pin pad to get pin block
                // 2. send the pinBlock to emv kernel
                if (emvTransParam.transKernalType == EmvData.KERNAL_CONTACTLESS_ENTRY_POINT) {
                    val track2 = arrayOfNulls<String>(1)
                    val pan = arrayOfNulls<String>(1)
                    emvHandler?.getTrack2AndPAN(track2, pan)
                    val index = track2[0]?.indexOfFirst { it == 'D' || it == '=' }?.plus(1) ?: 0
                    val exp = track2[0]?.substring(index, index + 4)
                    Log.e(TAG, "card: ${pan[0]}")
                    Log.e(TAG, "exp: $exp")
                    cardNum = pan[0]
                }

                Log.d(TAG, "onInputPIN")
                val iRet = when (commandValue) {
                    "cardActivation", "IBMBRegistration", "openAccount", "resetPIN" -> {
                        Log.d("Debug", "onInputPIN commandValue=$commandValue")
                        callback?.onDoSomething(appContext)
                        EmvResult.EMV_OK
                    }
                    "InquiryBalance", "transaction", CREATE_PIN, "registrasiPIN", "Transfer", CHANGE_PIN -> {
                        Log.d("Debug", "commandValue=$commandValue")
                        inputPIN()
                    }
                    START_DATE, END_DATE, LOGON, LOGOFF -> {
                        Log.d("Debug", "commandValue=$commandValue")
                        inputPIN()
                    }
                    else -> null
                }
//                var iRet: Int = inputPIN(pinType)

                //                iRet = inputPIN();
                Log.d("onInputPIN", "iRet=$iRet")
                Log.d("Debug", "iRet=$iRet")
                Log.d("Debug", "iRetPIN=$iRet")
                if (iRet == EmvResult.EMV_OK) {
                    emvHandler?.setPinBlock(mPinBlock)
                }
//                return iRet
                return iRet ?: 0
            }

            override fun onCertVerify(certType: Int, certNo: String): Int {
                Log.d(TAG, "onCertVerify")
                return 0
            }

            override fun onExchangeApdu(send: ByteArray): ByteArray? {
                Log.e(TAG, "apdu send = " + StringUtils.convertBytesToHex(send))
                Log.d("Debug", "onExchangeApdu=${Utils.bytesToHexString(send)}")
                Log.d("Debug", "realCardType=$realCardType")

                if (realCardType === CardReaderTypeEnum.IC_CARD) {
                    return mICCard.icExchangeAPDU(CardSlotNoEnum.SDK_ICC_USERCARD, send)
                }

                if (realCardType === CardReaderTypeEnum.RF_CARD) {
                    return mRfCard.rfExchangeAPDU(send)
                }

                return null
            }

            override fun onlineProc(): Int {
                // 1. assemble the authorisation request data and send to bank by using get 'emvHandler.getTlvData()'
                // 2. separateOnlineResp to emv kernel
                // 3. return the callback ret
                Log.d(TAG, "onOnlineProc")
                val authRespCode = ByteArray(3)
                val issuerResp = ByteArray(512)
                val issuerRespLen = IntArray(1)
                val iSendRet: Int = emvHandler!!.separateOnlineResp(
                    authRespCode, issuerResp,
                    issuerRespLen[0]
                )
                Log.d("Debug", "separateOnlineResp iSendRet= $iSendRet")
                return 0
            }
        }

        Log.e(TAG, "Emv Trans start... ${emvHandler == null}")
        // for the emv result, plz refer to emv doc.
        // for the emv result, plz refer to emv doc.
        // for the emv result, plz refer to emv doc.
        LogUtils.d(TAG, "mICCard=$mICCard, mRfCard=$mRfCard")
        val ret = emvHandler!!.emvTrans(
            emvTransParam,
            onEmvListener,
            pucIsEcTrans,
            pucBalance,
            pucTransResult
        )
        LogUtils.d(TAG, "emvTransParam: " + Gson().toJson(emvTransParam))
        LogUtils.d(TAG, "pucIsEcTrans before emvTrans: " + StringUtils.convertBytesToHex(pucIsEcTrans))
        LogUtils.d(TAG, "pucBalance before emvTrans: " + StringUtils.convertBytesToHex(pucBalance))
        LogUtils.d(TAG, "pucTransResult before emvTrans: " + StringUtils.convertBytesToHex(pucTransResult))
        Log.e(TAG, "Emv trans end, ret = $ret")

        val str = when (pucTransResult[0]) {
            EmvData.APPROVE_M -> "Approve"
            EmvData.ONLINE_M -> "Online"
            EmvData.DECLINE_M -> "Decline"
            else -> "Decline"
        }

        Log.e(TAG, "Emv trans result = " + pucTransResult[0] + ", " + str)
        if (ret == EmvResult.EMV_OK) {
            getEmvData()
        }
        mCardReadManager.closeCard()
    }

    // Method to determine if PIN input should be skipped
    fun shouldSkipPIN(): Boolean {
        val emvTransParam = EmvTransParam()
        // Example condition: skip PIN for contactless transactions
        return emvTransParam.transKernalType == EmvData.KERNAL_CONTACTLESS_ENTRY_POINT
    }

    private fun getEmvData() {
        Log.d(
            TAG,
            "getEmvdata: " + "berjalan "
        )

        val field55: ByteArray = emvHandler!!.packageTlvList(tags)
        Log.d(
            TAG,
            "Filed55: " + StringUtils.convertBytesToHex(field55).uppercase(Locale.getDefault())
        )

        val mainHandler = Handler(Looper.getMainLooper())
        when (commandValue) {
            "cardActivation" -> {
                mainHandler.post {
                    callback?.onDoSomething(appContext)
                }
            }
            CHANGE_PIN, "IBMBRegistration" -> {
                mainHandler.post {
                    // showDialogUtils.dialogBeforePIN(mainActivity)
                }
            }
            "openAccount" -> {
                Log.d("EmvUtil", "getEmvData: openAccount")
                mainHandler.post {
                    callback?.onDoSomething(appContext)
                }
            }
            START_DATE, END_DATE, LOGON, LOGOFF -> {
                Log.d("EmvUtil", "getEmvData: $commandValue")
                mainHandler.post {
                    callback?.onDoSomething(appContext)
                }
            }
            else -> {
                Log.d("EmvUtil", "getEmvData: else $commandValue")
            }
        }

        field55hex = StringUtils.convertBytesToHex(field55)
        val tlvData = StringUtils.convertBytesToHex(field55).uppercase(Locale.getDefault())
        val cardholderNameTag = "5F20"
        val index = tlvData.indexOf(cardholderNameTag)
        if (index != -1) {
            val length = tlvData.substring(
                index + cardholderNameTag.length,
                index + cardholderNameTag.length + 2
            ).toInt(16)
            val hexValue = tlvData.substring(
                index + cardholderNameTag.length + 2,
                index + cardholderNameTag.length + 2 + length * 2
            )
            val byteArray: ByteArray = FileUtils.hexStringToByteArray(hexValue)


//            System.out.println("stateCard Name: $stateCard")
//            if (commandValue.equals(CHANGE_PIN)) {
//                PinFragment.sendTrxLog("Change PIN")
//                nputNewPIN()
//            } else if (commandValue.equals(CREATE_PIN)) {
//                EmvUtil.inputConfirmPIN()
//            } else if (commandValue.equals("sendTrx")) {
//                try {
//                    if (stateCard === 1) {
//                        IsoUtil.isoVerificationPIN()
//                    } else {
////                        IsoUtil.isoVerificationPINswap();
//                    }
//                } catch (e: ISOException) {
//                    e.printStackTrace()
//                }
//            }
        } else {
            println("Cardholder Name not found in any tags.")
        }
    }
    fun inputPIN(): Int {

        mPinPadManager?.inputOnlinePin(
            context,
            6.toByte(),
            12.toByte(),
            60,
            true,
            cardNum,
            0.toByte(),
            PinAlgorithmMode.ANSI_X_9_8,
            object : OnPinPadInputListener {
                override fun onError(code: Int) {
                    step = 1
                    Log.e(TAG, "error pin : $code")
                }

                override fun onSuccess(bytes: ByteArray) {
                    val mainHandler = Handler(Looper.getMainLooper())
                    mainHandler.post {
//                        showDialogUtils.dialogBeforePIN(mainActivity)
//                    }
//                    mainActivity.runOnUiThread(Runnable {
                        Log.e(
                            TAG,
                            "PinBlock: " + StringUtils.convertBytesToHex(bytes)
                        )
                        val pinBlockOwnHex =
                            StringUtils.convertBytesToHex(bytes)
                        pinBlockOwn = StringUtils.convertBytesToHex(bytes)

                        Log.d("Debug", "pinBlockOwnHex=$pinBlockOwnHex")
                        Log.d("Debug", "pinBlockOwn=$pinBlockOwn")
                        Log.e(
                            "Debug",
                            "pinBlock=" + StringUtils.convertBytesToHex(bytes)
                        )
                        Log.d("Debug", "cardNumxx=$cardNum")
                        val encryptedPin: String = emvHandler!!.bytesToHexString(mPinBlock)
                        Log.d("Debug", "encryptedPin=$encryptedPin")

                        Log.d("Debug", "commandValue = $commandValue")

                        if (encryptedPin.isEmpty()) {
                            inputPINResult = EmvResult.EMV_NO_PASSWORD
                        } else { // pin length =0
                            inputPINResult = EmvResult.EMV_OK
                        }


                        when(commandValue) {
                            START_DATE, END_DATE, LOGON, LOGOFF, VERIFY_PIN, CREATE_PIN, REISSUE_PIN -> {
                                callback?.onDoSomething(appContext)
                            }
//                            CREATE_PIN, REISSUE_PIN -> {
//                                Log.d("Debug", "createPIN step=$step")
//                                if (step == 1) {
//                                    spvPinBlockOwn = pinBlockOwn
//                                    field48data = "$cardNum$spvPinBlockOwn"
//                                    Log.d("Debug", "field48data=$field48data")
//                                }
//                                callback?.onDoSomething(appContext)
//                            }
                            CHANGE_PIN -> inputNewPIN()
                        }
                    }
                }
            })
        Log.d("Debug", "inputPIN: selesai panggil inputPIN $commandValue")

        when(commandValue) {
            CHANGE_PIN -> mPinPadManager?.setInputPinTitle("Masukkan PIN anda")
            CREATE_PIN, REISSUE_PIN -> mPinPadManager?.setInputPinTitle("Masukkan PIN Supervisor")
            else -> mPinPadManager?.setInputPinTitle("Masukkan PIN anda")
        }
        return inputPINResult
    }

    private fun pinpadWork() {
        val mk_key = "484455B474A6C6115FF62236D8A09C74"
        val key_byte = StringUtils.convertHexToBytes(mk_key)
        var status = mPinPadManager!!.pinPadUpMastKey(
            0,
            key_byte,
            key_byte.size.toByte()
        ) //inject plain masterkey
        Log.d("Debug", "pinPadUpMastKey status$status")

//The working key ciphertext is regenerated from the plaintext key provided by the bank and the master key downloaded to the device
        val clearComponentHex = "02020202020202020404040404040404"
        val keycheckvalueHex: String =
            MessageDigestUtils.encodeTripleDES("0000000000000000", clearComponentHex)
                .substring(0, 8)
        val encryptedComponentHex: String =
            MessageDigestUtils.encodeTripleDES(clearComponentHex, mk_key) + keycheckvalueHex
        val pin_key_byte = StringUtils.convertHexToBytes(encryptedComponentHex)

        //inject pin chiper key
        status = mPinPadManager!!.pinPadUpWorkKey(
            0, pin_key_byte, pin_key_byte.size.toByte(),
            null, 0.toByte(), null, 0.toByte()
        )
    }
    fun inputNewPIN(): Boolean {
        Log.e(TAG, "start inputNewPIN")
        mPinPadManager?.inputOnlinePin(
            appContext,
            6.toByte(),
            12.toByte(),
            60,
            true,
            cardNum,
            0.toByte(),
            PinAlgorithmMode.ANSI_X_9_8,
            object : OnPinPadInputListener {
                override fun onError(code: Int) {
                    Log.d("Debug", "backCode=$code")
                    inputPINResult = when (code) {
                        SdkResult.SDK_PAD_ERR_NOPIN -> EmvResult.EMV_NO_PASSWORD
                        SdkResult.SDK_PAD_ERR_TIMEOUT -> EmvResult.EMV_TIME_OUT
                        SdkResult.SDK_PAD_ERR_CANCEL -> EmvResult.EMV_USER_CANCEL
                        else -> EmvResult.EMV_NO_PINPAD_OR_ERR
                    }
                    mLatch?.countDown()
                }

                override fun onSuccess(bytes: ByteArray) {
                    System.arraycopy(bytes, 0, mPinBlock, 0, bytes.size)
                    mPinBlock[bytes.size] = 0x00
                    Log.e(TAG, "PinBlock: " + StringUtils.convertBytesToHex(bytes))
                    val pinBlockOwnHex = StringUtils.convertBytesToHex(bytes)
                    pinBlockNew = StringUtils.convertBytesToHex(bytes)
                    Log.d("Debug", "pinBlockNewHex=$pinBlockOwnHex")
                    Log.d("Debug", "pinBlockNew=$pinBlockNew")

                    inputConfirmPIN()

                }
            })

        when(commandValue) {
            CHANGE_PIN -> mPinPadManager?.setInputPinTitle("Masukkan PIN baru anda")
            CREATE_PIN, "registrasiPIN" -> mPinPadManager?.setInputPinTitle("Masukkan PIN baru anda")
            else -> mPinPadManager?.setInputPinTitle("Masukkan PIN baru anda")
        }
//        if (commandValue == "IBMBRegistration") {
//            mPinPadManager?.setInputPinTitle("Masukkan PIN Internet Banking/Mobile Banking anda")
//        } else if (commandValue == CREATE_PIN || commandValue == "registrasiPIN") {
//            mPinPadManager?.setInputPinTitle("Masukkan PIN baru anda")
//        } else {
//            mPinPadManager?.setInputPinTitle("Masukkan PIN baru anda")
//        }
        return true
    }
    fun inputConfirmPIN(): Boolean {
        Log.e(TAG, "start inputNewPIN")
        mPinPadManager?.inputOnlinePin(
            appContext,
            6.toByte(),
            12.toByte(),
            60,
            true,
            cardNum,
            0.toByte(),
            PinAlgorithmMode.ANSI_X_9_8,
            object : OnPinPadInputListener {
                override fun onError(code: Int) {
                    step = 1
                    Log.d("Debug", "backCode=$code")
                    inputPINResult = when (code) {
                        SdkResult.SDK_PAD_ERR_NOPIN -> EmvResult.EMV_NO_PASSWORD
                        SdkResult.SDK_PAD_ERR_TIMEOUT -> EmvResult.EMV_TIME_OUT
                        SdkResult.SDK_PAD_ERR_CANCEL -> EmvResult.EMV_USER_CANCEL
                        else -> EmvResult.EMV_NO_PINPAD_OR_ERR
                    }
                    mLatch?.countDown()
                }

                override fun onSuccess(bytes: ByteArray) {
                    System.arraycopy(bytes, 0, mPinBlock, 0, bytes.size)
                    mPinBlock[bytes.size] = 0x00
                    pinBlockConfirm = StringUtils.convertBytesToHex(bytes)
                    Log.d("Debug", "pinBlockConfirm=$pinBlockConfirm")
                    Log.d("Debug", "pinBlockNew=$pinBlockNew")

                    if(pinBlockNew != pinBlockConfirm) {
                        Toast.makeText(appContext, "PIN tidak sesuai", Toast.LENGTH_SHORT).show()
                        Log.e(TAG, "PIN tidak sesuai")
                        return
                    }

                    when(commandValue) {
                        CREATE_PIN, REISSUE_PIN -> {
                            field48data = "$cardNum$pinBlockConfirm"
                            Log.d("Debug", "field48data=$field48data")
                        }
                    }

//                    when(commandValue) {
//                        REISSUE_PIN -> {
//                            if (step == 1) {
//                                spvPinBlockOwn = pinBlockOwn
//                                field48data = "$cardNum$spvPinBlockOwn"
//                                Log.d("Debug", "field48data=$field48data")
//                            }
//                        }
//                    }

                    callback?.onDoSomething(appContext)

//                    when (commandValue) {
//                        CHANGE_PIN -> {
//
//                        }
//                    }
//                    if (commandValue == CHANGE_PIN) {
//
//                        if (pinBlockNew == pinBlockConfirm) {
////                            mainActivity.changePIN()
//                            callback?.onDoSomething(appContext)
//                        }
//                    } else if (commandValue == CREATE_PIN) {
//                        if (pinBlockNew == pinBlockConfirm) {
////                            mainActivity.changePIN()
//                            callback?.onDoSomething(appContext)
//                        } else {
//                            Toast.makeText(MyApp.getContext(), "PIN tidak sesuai", Toast.LENGTH_SHORT).show()
//                            Log.e(TAG, "PIN tidak sesuai")
//                        }
//                    }else if(commandValue == "registrasiPIN"){
//                        if (pinBlockNew == pinBlockConfirm) {
////                            mainActivity.registrationPIN()
//                            Log.d("InputConfirmPIN", "onSuccess: panggil registrasiPIN")
//                            callback?.onDoSomething(appContext)
//                        } else {
//                            Log.e(TAG, "PIN tidak sesuai")
//                            Toast.makeText(MyApp.getContext(), "PIN tidak sesuai", Toast.LENGTH_SHORT).show()
//                        }
//                    }

//                    Log.d("Debug", "pinBlockConfirm=$pinBlockConfirm")
//                    Log.d("Debug", "backCode= $commandValue")
//                    if (commandValue == CREATE_PIN) {
//                        mainActivity.createPIN()
//                    } else if (commandValue == CHANGE_PIN) {+
//                        mainActivity.changePIN()
//                    }
                }
            })
        mPinPadManager?.setInputPinTitle("Masukkan PIN konfirmasi anda")
        return true
    }

    fun loadAids(emvHandler: EmvHandler) {
        for (aid in aids) {
            val ea = EmvApp()
            //ea.setAid("A00000000305076010");
            ea.aid = aid
            ea.selFlag = 0.toByte()
            ea.targetPer = 0x00.toByte()
            ea.maxTargetPer = 0.toByte()
            ea.floorLimit = 1000
            ea.onLinePINFlag = 1.toByte()
            ea.threshold = 0
            ea.tacDefault = "0000000000"
            ea.tacDenial = "0000000000"
            ea.tacOnline = "0000000000"
            ea.settDOL("0F9F02065F2A029A039C0195059F3704")
            ea.setdDOL("039F3704")
            ea.version = "008C"
            ea.clTransLimit = "000100000000"
            ea.clOfflineLimit = "000100008000"
            ea.clCVMLimit = "000100000000"
            ea.ecTTLVal = "000000100000"
            // paypass
            ea.payPassTermType = 0x22.toByte()
            ea.termCapNoCVMReq = "E0E9C8"
            ea.termCapCVMReq = "E0E9C8"
            ea.udol = "9F6A04"
            val ret = emvHandler.addApp(ea)
            Log.d("Debug", "addApp ret=$ret for AID: $aid")
        }
    }
    private fun loadMasterCardCapks1(emvHandle: EmvHandler) {
        val capk = EmvCapk()
        capk.keyID = 0x05.toByte()
        capk.rid = "A000000602"
        capk.modul = "B48CC63D71A486DFC920608A3E42D7C3" +
                "05472BF76B8E50C8C02FB8387E788F72" +
                "931A29DC15F913E7D69E43AD4C38A5C4" +
                "317E36D15DE5F49FA2327D9754799D24" +
                "84A6E156941ACA9632417E5C92931A85" +
                "E1BB5F2A2C1B847D5008C7B30591F1AC" +
                "BF3B98DFB0CF2849B6C7CDC7435AEA85" +
                "F3A58BAC3B8C990416A5E19EC4EA08DC" +
                "91CEF2FBE5940FA6622926D2AD0523D1" +
                "09A7024EB1035BBE37260B30F41AA52E" + "EB36E60DD37120B9401C3850920F0E03"
        capk.checkSum = "1CAB162A1BE81492BB952C2846617B756F833C07"
        capk.expDate = "20301231" // YYYYMMDD
        capk.exponent = "03"
        val ret = emvHandle.addCapk(capk)
        Log.d("Debug", "addCapk ret=$ret for CAPK RID: ${capk.rid} KeyID: ${capk.keyID}")
    }

    private fun loadMasterCardCapks(emvHandle: EmvHandler) {
        val capk = EmvCapk()
        capk.keyID = 0x05.toByte()
        capk.rid = "A000000602"
        capk.modul = "A517A338854E0856EE4AFDBF4BDA5DD3" +
                "F9EB3895CBD8971B1E58A8EB167BF993" +
                "5E0752DAEA7EAFB25E79D601EB201895" +
                "A93F8B0A16D95A230366C05FEC55858C" +
                "94D6097B2FB1EDDD2C6A3647DD0B71BC" +
                "1DCDDC68B4E9ECC919FB544070952443" +
                "159733471292993AB23E5B8C00E6A852" +
                "6DF04A0B6E65E0F9D0378F71497E12FA" +
                "83540B49FC05D0A86DC3D66FC4BB291A" +
                "69B2EBB98D057C8F1EE7CB8E942FD05E" +
                "9E4FAD0361BC184C13418C313C042C54" +
                "7DEF41310BA1850EF59CAF8CC7B14DAE" +
                "E72FA4689C1047434024D565A3FA46ED" +
                "CA3F53E236235268C893F268AA24AB2D" +
                "20EB7AE06FF3123318041CB23E30839C" + "58DFD4991D7C88CB"
        capk.checkSum = "E78686DB119C1CBFAD2149EF3CBE9CF54AC6321E"
        capk.expDate = "20301231" // YYYYMMDD
        capk.exponent = "03"
        val ret = emvHandle.addCapk(capk)
        Log.d("Debug", "addCapk ret=$ret for CAPK RID: ${capk.rid} KeyID: ${capk.keyID}")
    }

    private fun loadAnnexCapk(ep: EmvHandler) {
        val ea = EmvApp()
        ea.aid = "A0000006021010"
        ea.settDOL("9F02065F2A029A039C0195059F3704")
        ea.setdDOL("9F3704")
        ea.termCapNoCVMReq = "F000F0A001"
        ea.termCapCVMReq = "E0F8C8"
        ea.selFlag = 0.toByte()
        ea.targetPer = 0x00.toByte()
        ea.maxTargetPer = 0.toByte()
        ea.floorLimit = 0
        ea.onLinePINFlag = 1.toByte()
        ea.threshold = 0
        ea.tacDefault = "DC4000A800"
        ea.tacDenial = "0010000000"
        ea.tacOnline = "DC4004F800"
        ea.version = "008C"
        ea.clTransLimit = "000000015000"
        ea.clOfflineLimit = "000000008000"
        ea.clCVMLimit = "000000005000"
        ea.ecTTLVal = "000000100000"
        try {
            val ret = ep.addApp(ea)
            Log.d("Debug", "addApp ret=$ret for AID: ${ea.aid}")
        } catch (e: Exception) {
            e.printStackTrace()
            e.message
            Log.e(TAG, "error catch = " + e.message)
        }
    }

    private fun loadVisaAIDs(emvHandle: EmvHandler) {
// Visa Credit/Debit EmvApp
        val ea = EmvApp()
        ea.aid = "A0000000031010"
        ea.selFlag = 0.toByte()
        ea.targetPer = 0x00.toByte()
        ea.maxTargetPer = 0.toByte()
        ea.floorLimit = 1000
        ea.onLinePINFlag = 1.toByte()
        ea.threshold = 0
        ea.tacDefault = "0000000000"
        ea.tacDenial = "0000000000"
        ea.tacOnline = "0000000000"
        ea.settDOL("0F9F02065F2A029A039C0195059F3704")
        ea.setdDOL("039F3704")
        ea.version = "008C"
        ea.clTransLimit = "000000015000"
        ea.clOfflineLimit = "000000008000"
        ea.clCVMLimit = "000000005000"
        ea.ecTTLVal = "000000100000"
        val ret = emvHandle.addApp(ea)
        Log.d("Debug", "addApp ret=$ret for AID: ${ea.aid} ")
    }
}