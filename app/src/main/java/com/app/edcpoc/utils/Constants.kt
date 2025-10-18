package com.app.edcpoc.utils

import com.zcs.sdk.DriverManager
import com.zcs.sdk.Printer
import com.zcs.sdk.Sys
import com.zcs.sdk.card.CardInfoEntity
import com.zcs.sdk.card.CardReaderManager
import com.zcs.sdk.card.CardReaderTypeEnum
import com.zcs.sdk.card.ICCard
import com.zcs.sdk.card.RfCard
import com.zcs.sdk.emv.EmvHandler
import com.zcs.sdk.pin.pinpad.PinPadManager

object Constants {
    val mDriverManager: DriverManager = DriverManager.getInstance()

    public final var mPrinter: Printer? = null
    public final var mPinPadManager: PinPadManager? = null
    public final var emvHandler: EmvHandler? = null
    public final var realCardType: CardReaderTypeEnum? = null
    var mCardType: CardReaderTypeEnum = CardReaderTypeEnum.MAG_IC_RF_CARD

    public final lateinit var mSys: Sys
    public final lateinit var mCardReadManager: CardReaderManager
    public final lateinit var mICCard: ICCard
    public final lateinit var mRfCard: RfCard
    public final lateinit var cardInfoEntity: CardInfoEntity

    public final var commandValue: String = ""
    public final var pos_entrymode: String = ""
    var mRfCardType: Byte = 0
    const val READ_TIMEOUT = 30000
    var iRet = 0

    //PINPAD
    var inputPINResult: Int = 0x00
    var mPinBlock = ByteArray(12 + 1)
    var pinBlockOwn: String? = null
    var pinBlockNew: String? = null
    var pinBlockConfirm: String? = null

//    card
    public final var cardNum: String? = null
    public final var track2hex: String = ""
    var field55hex: String? = null
    var track2data: String? = null

    public final var aids = arrayOf(
        "A000000333010101",
        "A000000333010102",
        "A000000333010103",
        "A000000333010106",
        "A000000333010108",
        "A0000003330101",
        "A00000000305076010",
        "A0000000031010",
        "A000000003101001",
        "A000000003101002",
        "A0000000032010",
        "A0000000032020",
        "A0000000033010",
        "A0000000034010",
        "A0000000035010",
        "A0000000036010",
        "A0000000036020",
        "A0000000038002",
        "A0000000038010",
        "A0000000039010",
        "A000000003999910",
        "A00000000401",
        "A0000000041010",
        "A00000000410101213",
        "A00000000410101215",
        "A0000000042010",
        "A0000000043010",
        "A0000000043060",
        "A000000004306001",
        "A0000000044010",
        "A0000000045010",
        "A0000000046000",
        "A0000000048002",
        "A0000000049999",
        "A0000000050001",
        "A0000000050002",
        "A00000002401",
        "A000000025",
        "A0000000250000",
        "A00000002501",
        "A000000025010402",
        "A000000025010701",
        "A000000025010801",
        "A0000000291010",
        "A0000000421010",
        "A0000000422010",
        "A0000000423010",
        "A0000000424010",
        "A0000000425010",
        "A0000000426010",
        "A00000006510",
        "A0000000651010",
        "A00000006900",
        "A000000077010000021000000000003B",
        "A000000098",
        "A0000000980848",
        "A0000001211010",
        "A0000001410001",
        "A0000001523010",
        "A0000001524010",
        "A0000001544442",
        "A000000172950001",
        "A0000001850002",
        "A0000002281010",
        "A0000002282010",
        "A0000002771010",
        "A00000031510100528",
        "A0000003156020",
        "A0000003591010028001",
        "A0000003710001",
        "A0000004540010",
        "A0000004540011",
        "A0000004766C",
        "A0000005241010",
        "A0000006723010",
        "A0000006723020",
        "A0000007705850",
        "B012345678",
        "D27600002545500100",
        "D5280050218002",
        "D5780000021010",
        "F0000000030001",
        "A000000602",
        "A0000006021010",  //mastercard add
        "A0000000040000",
        "A0000000041010BB5449435301",
        "A0000000041010C0000301",
        "A0000000041010C0000302",
        "A0000000042203",
        "A0000000045000",
        "A0000000045555",
        "A0000000046010",
        "A0000001570020",
        "B012345678",
        "D0400000190003",
        "D0400000190004",
        "D7560000300101",
        "A000000004"
    )
    var tags = intArrayOf(
        0x9F26,
        0x9F27,
        0x9F10,
        0x9F37,
        0x9F36,
        0x95,
        0x9A,
        0x9C,
        0x9F02,
        0x5F2A,
        0x82,
        0x9F1A,
        0x9F03,
        0x9F33,
        0x9F34,
        0x9F35,
        0x9F1E,
        0x84,
        0x9F09,
        0x9F41,
        0x9F63,
        0x5F24,
        0x5F20
    )
}