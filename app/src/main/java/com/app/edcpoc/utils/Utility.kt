package com.app.edcpoc.utils

import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Editable
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import com.app.edcpoc.PreferenceManager
import com.simo.ektp.EktpSdkZ90
import com.simo.ektp.GlobalVars.CONFIG_FILE
import com.simo.ektp.GlobalVars.DEBUG
import com.simo.ektp.GlobalVars.PCID
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
import com.zcs.sdk.DriverManager
import com.zcs.sdk.Sys
import java.io.ByteArrayOutputStream
import java.net.InetAddress
import java.net.Socket
import java.net.InetSocketAddress
import java.security.MessageDigest
import java.security.SecureRandom
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

object Utility {

    private const val TAG = "Utility"
    var mChangePinDialog: ProgressDialog? = null
    var countDownTimer: CountDownTimer? = null

    fun readPsamConfigSuccess(context: Context): Boolean {
        PCID = PreferenceManager.getPCID(context)
        CONFIG_FILE = PreferenceManager.getConfigFile(context)

        return try {
            if (DEBUG) Log.d(TAG, "PCID = $PCID")
            if (DEBUG) Log.d(TAG, "CONFIG_FILE = $CONFIG_FILE")

            if (PCID?.length != 32 || CONFIG_FILE?.length != 64) {
                return false
            }

            // Apply config
            Log.e(TAG, "pcid boolean = $PCID")
            EktpSdkZ90.instance().setPCID(PCID)
            EktpSdkZ90.instance().setConfigFile(CONFIG_FILE)
            true
        }catch (e: Exception){
            if (DEBUG) Log.d(TAG, "read psamProfile failed. ${e.message}")
            false
        }
    }

     fun generateHmac(userId: String, msgId: String, msgSecretKey: String): String {
        val data = userId + msgId + msgSecretKey
        return hashSHA256(data)
    }

     fun hashSHA256(data: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(data.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    fun startCountDownTimer(timeout: Int, onFinish: () -> Unit) {
        val h = Handler(Looper.getMainLooper())
        h.post {
            //here show dialog
           countDownTimer =
                object : CountDownTimer(timeout.toLong(), 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        val secondsRemaining = millisUntilFinished / 1000
                        Log.i(
                            TAG,
                            "timeout : $secondsRemaining seconds"
                        )
                    }

                    override fun onFinish() {
                        Log.i(TAG, "timeout : 0 seconds")
                        onFinish()
                    }
                }.start()
        }

    }

    fun getTransmissionDateTime(): String? {
        val sdf = SimpleDateFormat("MMddHHmmss", Locale.getDefault())
        return sdf.format(Date())
    }

    fun getLocalTransactionTime(): String? {
        val sdf = SimpleDateFormat("HHmmss", Locale.getDefault())
        return sdf.format(Date())
    }

    fun getLocalTransactionTime1(): String? {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }

    fun getLocalTransactionDate(): String? {
        val sdf = SimpleDateFormat("MMdd", Locale.getDefault())
        return sdf.format(Date())
    }

    fun formatDateWithDateTimeFormatter(): String {
        // Use SimpleDateFormat for API < 26
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
        val currentDate = Date()
        return sdf.format(currentDate).uppercase()
    }

    fun adjustTrack2Data(track2Hex: String): String? {
        val maxLength = 54

        // Jika panjang kurang dari 54 karakter, tambahkan spasi (0x20 dalam hex)
        if (track2Hex.length < maxLength) {
            val spacePaddingLength = maxLength - track2Hex.length
            val padding = StringBuilder()
            for (i in 0 until spacePaddingLength) {
                padding.append("20") // "20" adalah spasi dalam hex
            }
            return track2Hex + padding.toString()

            // Jika lebih dari 54 karakter, potong menjadi 54 karakter
        } else if (track2Hex.length > maxLength) {
            return track2Hex.substring(0, maxLength)
        }

        // Jika sudah tepat 54 karakter, tidak ada perubahan
        return track2Hex
    }

    fun formatToRupiah(amount: String): String {
        return try {
            // Konversi string ke Double
            val amountDouble = amount.toDoubleOrNull() ?: throw IllegalArgumentException("Invalid amount format")
            val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            formatRupiah.format(amountDouble)
        } catch (e: NumberFormatException) {
            "Error: Invalid number format"
        } catch (e: IllegalArgumentException) {
            e.message ?: "Unknown error"
        }
    }

    fun hexToAscii(hexStr: String): String {
        val output = StringBuilder("")

        // Memproses setiap dua karakter (sebagai pasangan hexadecimal)
        for (i in hexStr.indices step 2) {
            val str = hexStr.substring(i, i + 2)
            // Mengonversi nilai hex menjadi karakter ASCII
            output.append(str.toInt(16).toChar())
        }

        return output.toString()
    }

    fun convertBmpToBase64(bitmap: Bitmap, flags: Int = Base64.DEFAULT): String? {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)

        return Base64.encodeToString(outputStream.toByteArray(), flags)
    }

    fun hexToBase64(hex: String): String {
        // Convert hex to byte array
        val bytes = hex.chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()

        // Encode byte array to Base64
        return Base64.encodeToString(bytes, Base64.DEFAULT).trim()
    }

    fun base64ToHex(base64: String): String {
        // Decode Base64 to byte array
        val bytes = Base64.decode(base64, Base64.DEFAULT)

        // Convert byte array to hex string
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun clearVar() {
        VALUE_BIOMETRIC_LEFT = ""
        VALUE_BIOMETRIC_RIGHT = ""
        VALUE_NIK = ""
        VALUE_NAMA = ""
        VALUE_TMP_LAHIR = ""
        VALUE_TGL_LAHIR = ""
        VALUE_JNS_KELAMIN = ""
        VALUE_GOL_DARAH = ""
        VALUE_ALAMAT = ""
        VALUE_RT = ""
        VALUE_RW = ""
        VALUE_KEL = ""
        VALUE_KEC = ""
        VALUE_KAB = ""
        VALUE_PROV = ""
        VALUE_AGAMA = ""
        VALUE_STATUS = ""
        VALUE_PEKERJAAN = ""
        VALUE_NATIONALITY = ""
        VALUE_VALID_UNTIL = ""
        VALUE_FOTO = ""
        VALUE_SIGNATURE = ""
    }
    fun secureRandomString32(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        val rnd = SecureRandom()
        return (1..32)
            .map { chars[rnd.nextInt(chars.length)] }
            .joinToString("")
    }
    fun maskCardNumber(cardNumber: String, maskLength: Int = 4): String {
        if (cardNumber.length <= 6) return cardNumber
        val start = cardNumber.substring(0, maskLength)
        val end = cardNumber.substring(cardNumber.length - maskLength)
        val masked = "*".repeat(cardNumber.length - 8)
        return "$start$masked$end"
    }

    fun getAndroidId(context: Context): String =
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"

    fun getSn(): String {
        val sys: Sys = DriverManager.getInstance().baseSysDevice
        val pid = arrayOfNulls<String>(1)
        sys.getPid(pid)
        val sn = pid[0] ?: return ""
        return if (sn.length > 8) sn.substring(8) else sn
    }
//    @RequiresPermission("android.permission.READ_PHONE_STATE")
//    fun getDeviceSerial(context: Context): String {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) return getAndroidId(context) // Android 10+ -> fallback
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val perm = Manifest.permission.READ_PHONE_STATE
//            if (ContextCompat.checkSelfPermission(context, perm) != PackageManager.PERMISSION_GRANTED) {
//                return "permission_required" // request runtime permission in Activity/Fragment
//            }
//            return try {
//                Build.getSerial() ?: getAndroidId(context)
//            } catch (e: SecurityException) {
//                getAndroidId(context)
//            } catch (e: Exception) {
//                getAndroidId(context)
//            }
//        }
//
//        // older devices
//        return Build.SERIAL ?: getAndroidId(context)
//    }
    fun createDialog(
        context: Context,
        title: String = "Processing...",
        message: String?,
        showListener: (dialog: ProgressDialog, dialog1: DialogInterface) -> Unit,
        cancelListener: (dialog: ProgressDialog, dialog1: DialogInterface) -> Unit
    ): ProgressDialog {
        val dialog = ProgressDialog(context)
        dialog.setTitle(title)
        dialog.setMessage(message)
        dialog.setOnShowListener { dialog1 -> showListener(dialog, dialog1) }
        dialog.setOnCancelListener { dialog1 -> cancelListener(dialog, dialog1) }
        dialog.progress = 0
        dialog.isIndeterminate = true
        dialog.setCancelable(false)
        return dialog
    }

    fun formatNumber(s: Editable?): String {
        val cleanString = s.toString().replace(".", "")
        val parsed = cleanString.toLongOrNull() ?: 0L
        val formatted = "%,d".format(parsed).replace(",", ".")
        return formatted
    }

    fun generateUniqueStan(): String {
        // You can implement your own logic to generate a unique STAN
        // For simplicity, this example generates a random number as STAN
        return (Math.random() * 1000000).toInt().toString()
    }

    /**
     * Ping a host. Returns true if reachable, false otherwise. Logs the result and any exception.
     */
    suspend fun pingHost(host: String, timeoutMs: Int = 2000): Boolean = withContext(Dispatchers.IO) {
        try {
            val process = ProcessBuilder("ping", "-c", "1", host).start()
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readText()
            val exitCode = process.waitFor()
            Log.d(TAG, "Ping to $host result=$exitCode output=$output")
            exitCode == 0
        } catch (e: Exception) {
            Log.e(TAG, "Ping to $host failed: ${e.message}", e)
            false
        }
    }

    /**
     * Telnet (open socket) to a host:port. Returns true if successful, false otherwise. Logs the result and any exception.
     */
    suspend fun telnetHost(host: String, port: Int, timeoutMs: Int = 2000): Boolean = withContext(Dispatchers.IO) {
        try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(host, port), timeoutMs)
                true
            }
        } catch (e: Exception) {
            false
        }
    }

    fun generateSn(): String {
        val sn = getSn()
        val snSlice = sn.takeLast(8)
        return "ID$snSlice"
    }

    fun simpleDateFormat(): String {
        val sdf = SimpleDateFormat("yyMMddHHmm", Locale.getDefault())
        val formatted = sdf.format(Date())
        return formatted
    }
}