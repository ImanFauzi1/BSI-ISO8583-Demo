package com.app.edcpoc.utils

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

object FileUtils {

    @Throws(IOException::class)
    fun doCopy(context: Context, assetsPath: String, desPath: String) {
        var assetsPath = assetsPath
        var desPath = desPath
        val srcFiles = context.assets.list(assetsPath) //for directory
        for (srcFileName in srcFiles!!) {
            if (!desPath.endsWith(File.separator)) desPath += File.separator
            if (!assetsPath.endsWith(File.separator)) assetsPath += File.separator
            val desDir = File(desPath)
            if (!desDir.exists()) desDir.mkdir()
            val outFileName = desPath + srcFileName
            var inFileName = assetsPath + srcFileName
            if (assetsPath == "") { // for first time
                inFileName = srcFileName
            }
            Log.e(
                "tag",
                "========= assets: $assetsPath  filename: $srcFileName infile: $inFileName outFile: $outFileName"
            )
            try {
                val inputStream = context.assets.open(inFileName)
                copyAndClose(inputStream, FileOutputStream(outFileName))
            } catch (e: IOException) { //if directory fails exception
                e.printStackTrace()
                File(outFileName).mkdir()
                doCopy(context, inFileName, outFileName)
            }
        }
    }

    private fun closeQuietly(out: OutputStream?) {
        try {
            out?.close()
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    private fun closeQuietly(`is`: InputStream?) {
        try {
            `is`?.close()
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    @Throws(IOException::class)
    private fun copyAndClose(`is`: InputStream, out: OutputStream) {
        copy(`is`, out)
        closeQuietly(`is`)
        closeQuietly(out)
    }

    @Throws(IOException::class)
    private fun copy(`is`: InputStream, out: OutputStream) {
        val buffer = ByteArray(1024)
        var n = 0
        while (-1 != `is`.read(buffer).also { n = it }) {
            out.write(buffer, 0, n)
        }
    }

    // Convert hex string to byte array
    fun hexStringToByteArray(s: String): ByteArray {
        val len = s.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((s[i].digitToIntOrNull(16) ?: -1 shl 4)
            + s[i + 1].digitToIntOrNull(16)!! ?: -1).toByte()
            i += 2
        }
        return data
    }
}