package com.app.edcpoc

import android.graphics.Bitmap
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding

class ManualEnrollmentActivity : AppCompatActivity() {
    private lateinit var nikEditText: EditText
    private lateinit var namaEditText: EditText
    private lateinit var fingerprintImageView: ImageView
    private lateinit var scanFingerprintButton: Button
    private lateinit var submitButton: Button
    private var fingerprintBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Root LinearLayout
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            gravity = android.view.Gravity.CENTER_HORIZONTAL
        }

        nikEditText = EditText(this).apply {
            hint = "NIK"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            filters = arrayOf(android.text.InputFilter.LengthFilter(16))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 16.dp }
        }
        rootLayout.addView(nikEditText)

        namaEditText = EditText(this).apply {
            hint = "Nama"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PERSON_NAME
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 16.dp }
        }
        rootLayout.addView(namaEditText)

        fingerprintImageView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(168.dp, 44.dp).apply { bottomMargin = 16.dp }
            setBackgroundColor(0xFFDDDDDD.toInt())
            contentDescription = "Fingerprint Preview"
        }
        rootLayout.addView(fingerprintImageView)

        scanFingerprintButton = Button(this).apply {
            text = "Scan Fingerprint"
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 12.dp }
        }
        rootLayout.addView(scanFingerprintButton)

        submitButton = Button(this).apply {
            text = "Submit"
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        rootLayout.addView(submitButton)

        setContentView(rootLayout)

        scanFingerprintButton.setOnClickListener {
            fingerprintBitmap = getPlaceholderFingerprintBitmap()
            fingerprintImageView.setImageBitmap(fingerprintBitmap)
        }

        submitButton.setOnClickListener {
            val nik = nikEditText.text.toString()
            val nama = namaEditText.text.toString()
            if (nik.isBlank() || nama.isBlank() || fingerprintBitmap == null) {
                Toast.makeText(this, "Lengkapi data dan scan fingerprint!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "NIK: $nik\nNama: $nama\nFingerprint ready", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getPlaceholderFingerprintBitmap(): Bitmap {
        val bmp = Bitmap.createBitmap(168, 44, Bitmap.Config.ARGB_8888)
        bmp.eraseColor(0xFFCCCCCC.toInt())
        return bmp
    }

    // Extension property for dp to px
    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()
}
