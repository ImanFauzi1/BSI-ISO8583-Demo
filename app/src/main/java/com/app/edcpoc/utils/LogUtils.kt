package com.app.edcpoc.utils

import android.content.Context
import android.util.Log
import com.app.edcpoc.data.model.LogTransactionRequest
import com.app.edcpoc.ui.viewmodel.ApiViewModel
import com.app.edcpoc.utils.Constants.cardNum
import com.app.edcpoc.utils.Constants.pos_entrymode
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.getValue

object LogUtils {
	private const val DEFAULT_TAG = "VictoriaPOC"
	private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
	private val fileDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

	@Volatile
	private var fileLoggingEnabled: Boolean = false

	@Volatile
	private var retentionDays: Int = 30

	@Volatile
	private var initialized: Boolean = false

	private lateinit var appContext: Context
	private lateinit var logsDir: File
	private val writeLock = Any()
	@Volatile
	private var lastCleanupAtMs: Long = 0L
	@Volatile
	private var currentLogDate: String? = null
	@Volatile
	private var apiViewModel: ApiViewModel? = null

	fun setApiViewModel(viewModel: ApiViewModel) {
		apiViewModel = viewModel
	}

	fun init(context: Context, enableFileLogging: Boolean = true, directoryName: String = "logs", retentionDays: Int = 30) {
		appContext = context.applicationContext
		fileLoggingEnabled = enableFileLogging
		this.retentionDays = if (retentionDays > 0) retentionDays else 30
		logsDir = File(appContext.filesDir, directoryName)
		if (!logsDir.exists()) {
			logsDir.mkdirs()
		}
		initialized = true
		cleanupOldLogs()
	}

	fun enableFileLogging(enable: Boolean) {
		fileLoggingEnabled = enable
	}

	fun setRetentionDays(days: Int) {
		if (days > 0) retentionDays = days
	}

	fun d(tag: String = DEFAULT_TAG, message: String) {
		val formatted = formatMessage(message, tag, "D")
		Log.d(tag, formatted)
		writeToFile("D", tag, message, null)
	}

	fun i(tag: String = DEFAULT_TAG, message: String) {
		val formatted = formatMessage(message, tag, "I")
		Log.i(tag, formatted)
		writeToFile("I", tag, message, null)
	}

	fun w(tag: String = DEFAULT_TAG, message: String) {
		val formatted = formatMessage(message, tag, "W")
		Log.w(tag, formatted)
		writeToFile("W", tag, message, null)
	}

	fun e(tag: String = DEFAULT_TAG, message: String, throwable: Throwable? = null) {
		val formatted = formatMessage(message, tag, "E")
		Log.e(tag, formatted, throwable)
		writeToFile("E", tag, message, throwable)
	}

	private fun formatMessage(message: String, tag: String, level: String): String {
		val timestamp = dateTimeFormat.format(Date())
		val thread = Thread.currentThread().name
		return "[$timestamp][$level][$tag][$thread] $message"
	}

	private fun writeToFile(level: String, tag: String, message: String, throwable: Throwable?) {
		if (!fileLoggingEnabled || !initialized) return
		try {
			maybeCleanup()
			val datePart = fileDateFormat.format(Date())
			val logFile = File(logsDir, "$datePart.log")
			val line = buildString {
				append(formatMessage(message, tag, level))
				if (throwable != null) {
					append('\n')
					append(getStackTraceString(throwable))
				}
				append('\n')
			}
			synchronized(writeLock) {
				// If rolling over to a new day, run immediate cleanup and update state
				if (currentLogDate != datePart) {
					cleanupOldLogs()
					lastCleanupAtMs = System.currentTimeMillis()
					currentLogDate = datePart
				}
				FileWriter(logFile, true).use { writer ->
					writer.append(line)
				}
			}
		} catch (_: Throwable) {
			// Intentionally ignore file logging failures to avoid crashing
		}
	}

	private fun maybeCleanup() {
		val now = System.currentTimeMillis()
		// run at most once every 12 hours
		if (now - lastCleanupAtMs > 12 * 60 * 60 * 1000L) {
			cleanupOldLogs()
			lastCleanupAtMs = now
		}
	}

	private fun cleanupOldLogs() {
		try {
			if (!::logsDir.isInitialized) return
			val thresholdMs = System.currentTimeMillis() - retentionDays * 24L * 60L * 60L * 1000L
			val files = logsDir.listFiles() ?: return
			for (f in files) {
				if (f.isFile && f.name.endsWith(".log")) {
					val lm = f.lastModified()
					if (lm in 1..Long.MAX_VALUE && lm < thresholdMs) {
						try { f.delete() } catch (_: Throwable) { }
					}
				}
			}
		} catch (_: Throwable) {
			// ignore cleanup failures
		}
	}

	private fun getStackTraceString(throwable: Throwable): String {
		val sw = StringWriter()
		val pw = PrintWriter(sw)
		throwable.printStackTrace(pw)
		pw.flush()
		return sw.toString()
	}

	fun sendLogTransaction(status: String, transactionType: String, description: String, remarks: String = "") {
		try {
			val viewModel = apiViewModel ?: return
			val payload = LogTransactionRequest(
				card_number = cardNum ?: return,
				transaction_type = transactionType,
				pos_entrymode = pos_entrymode,
				status = status,
				response_code_description = description,
				remarks = remarks,
			)
			viewModel.sendLogTransaction(payload)
		} catch (e: Exception) {
			LogUtils.e(DEFAULT_TAG, "Log Transaction Error: ${e.message}")
		}
	}
}
