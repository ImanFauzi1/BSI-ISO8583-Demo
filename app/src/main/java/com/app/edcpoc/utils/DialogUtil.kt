package com.app.edcpoc.utils

import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import com.app.edcpoc.utils.Constants.cardType

object DialogUtil {
    private const val TAG = "DialogUtil"

    fun createDialog(
        context: Context,
        title: String? = "Processing...",
        message: String?,
        showListener: (ProgressDialog, DialogInterface) -> Unit,
        cancelListener: (ProgressDialog, DialogInterface) -> Unit
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

    fun createEmvDialog(context: Context, emvUtil: EmvUtil, title: String? = "Insert Card", message: String? = "Insert or Swipe Card") {
        createDialog(
            context = context,
            title = title,
            message = message,
            showListener = { dialog, dialog1 ->
                LogUtils.d(TAG, "createEmvDialog: showListener")
                Thread {
                    emvUtil.searchBankCard(cardType, dialog)
                }.start()
            },
            cancelListener = { dialog, dialog1 ->
                emvUtil.cancelSearchCard()
            },
        ).show()
    }
}