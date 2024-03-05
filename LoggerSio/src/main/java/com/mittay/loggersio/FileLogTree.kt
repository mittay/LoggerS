package com.mittay.loggersio

import android.text.format.DateFormat
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.*

object FileLogTree : Timber.DebugTree() {

    private val mainLogFlow =
        MutableSharedFlow<Pair<String?, String>>(replay = 64, extraBufferCapacity = 1024)
    private val scope = CoroutineScope(Dispatchers.IO)

    private const val DATE_FORMAT = "MM-dd HH:mm:ss.SSS"
    private var log: LoggerSio? = null

    init {
        mainLogFlow.onEach { log ->
            printToFile(log.first, log.second)
        }.launchIn(scope)

    }

    override fun createStackElementTag(element: StackTraceElement): String {
        return String.format(
            "(%s:%s)#%s",
            element.fileName,
            element.lineNumber,
            element.methodName
        )
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (log == null) {
            Log.e("LoggerSio", "Logger not set")
            return
        }
        if (message.length < log!!.maxLogRowLength) {
            mainLogFlow.tryEmit(Pair(first = tag, second = message))
            t?.let { mainLogFlow.tryEmit(Pair(first = tag, second = Log.getStackTraceString(it))) }
            return
        }

        // Split by line, then ensure each line can fit into Log's maximum length.
        var i = 0
        val length = message.length
        while (i < length) {
            var newline = message.indexOf('\n', i)
            newline = if (newline != -1) newline else length
            do {
                val end = Math.min(newline, i + log!!.maxLogRowLength as Int)
                val part = message.substring(i, end)

                mainLogFlow.tryEmit(Pair(first = tag, second = part))
                i = end
            } while (i < newline)
            i++
        }
    }

    private fun printToFile(tag: String?, message: String) {
        val dateTime = Calendar.getInstance().time
        val formatDateTime = SimpleDateFormat(DATE_FORMAT).format(dateTime)

        try {
            if(isFileLessThanMax(log!!.getFile(), log!!.maxLogSizeMb).not()){
                val pw = PrintWriter(log!!.path)
                pw.close()
            }
        } catch (e: Exception) {
            Log.println(
                Log.ERROR,
                "LoggerSio",
                "Error while checking file: $e"
            )
        }
        try {
            FileOutputStream(log?.getFile(), true).use {
                it.write("$formatDateTime[$tag] -> $message\n".toByteArray(Charsets.UTF_8))
            }
        } catch (e: IOException) {
            Log.println(
                Log.ERROR,
                "LoggerSio",
                "Error while logging into file: $e"
            )
        }
    }

    private fun isFileLessThanMax(file: File, fileMaxSizeMb: Int): Boolean {
        val maxFileSize = fileMaxSizeMb * 1024 * 1024
        val l = file.length()
        val fileSize = l.toString()
        val finalFileSize = fileSize.toInt()
        return finalFileSize <= maxFileSize
    }

    operator fun invoke(vararg loggerSio: LoggerSio): FileLogTree {
        log = loggerSio.first()
        return this
    }

}

class LoggerSio(
    val path: String?,
    val maxLogRowLength: Int,
    val maxLogSizeMb: Int,
) {

    fun getFile(): File {
        val filePath = path
            ?: throw IOException("Incorrect logging file directory")
        val file = File(filePath)
        if (!file.exists()) file.createNewFile()
        return file
    }

    data class Param(
        var path: String? = null,
        var maxLogRowLength: Int = 2000, // char
        var maxLogSize: Int = 4 * 1024 * 1024,// 4 Mb
    ) {

        fun path(path: String) = apply { this.path = path }
        fun maxLogRowLength(maxLogRowLengthMB: Int) =
            apply { this.maxLogRowLength = maxLogRowLength }

        fun maxLogSize(maxLogSize: Int) = apply { this.maxLogSize = maxLogSize }
        fun build() = LoggerSio(path, maxLogRowLength, maxLogSize)
    }
}
