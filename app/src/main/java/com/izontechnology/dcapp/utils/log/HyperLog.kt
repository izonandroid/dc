package com.izontechnology.dcapp.utils.log

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.izontechnology.dcapp.utils.API_RETURN_DATE_FORMAT
import com.izontechnology.dcapp.utils.DOWNLOAD_DIR_PATH
import com.izontechnology.dcapp.utils.executeAsyncTask
import com.izontechnology.dcapp.utils.todayDate
import kotlinx.coroutines.GlobalScope
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.io.LineNumberReader
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object HyperLog {
    private const val TAG = "LOG"
    const val TAG_ASSERT = "ASSERT"
    const val TAG_HYPERLOG = "LOG"
    private var logLevel = Log.WARN
    private var mDeviceLogList: DeviceLogList? = null
    private val URL: String? = null
    private const val EXPIRY_TIME = 7 * 24 * 60 * 60 // 7 Days
    private var mLogFormat: LogFormat? = null
    private var context: Context? = null
    private var executorService: ExecutorService? = null

    /**
     * Call this method to initialize HyperLog.
     * By default, seven days older logs will gets deleted automatically.
     *
     * @param context   The current context.
     * @param logFormat [LogFormat] to set custom log message format.
     * @see .initialize
     */
    fun initialize(context: Context, logFormat: LogFormat?) {
        initialize(context, EXPIRY_TIME, logFormat)
    }

    /**
     * Call this method to initialize HyperLog.
     * By default, seven days older logs will gets deleted automatically.
     *
     * @param context             The current context.
     * @param expiryTimeInSeconds Expiry time for logs in seconds.
     * @see .initialize
     */
    fun initialize(context: Context, expiryTimeInSeconds: Int?) {
        initialize(context, expiryTimeInSeconds, LogFormat(context))
    }
    /**
     * Call this method to initialize HyperLog.
     * By default, seven days older logs will get expire automatically. You can change the expiry
     * period of logs by defining expiryTimeInSeconds.
     *
     * @param context             The current context.
     * @param expiryTimeInSeconds Expiry time for logs in seconds.
     * @param logFormat           [LogFormat] to set custom log message format.
     * @see .initialize
     */
    /**
     * Call this method to initialize HyperLog.
     * By default, seven days older logs will gets deleted automatically.
     *
     * @param context The current context.
     * @see .initialize
     */
    fun initialize(
        context: Context, expiryTimeInSeconds: Int? = EXPIRY_TIME,
        logFormat: LogFormat? = LogFormat(context)
    ) {
        if (context == null) {
            Log.e(TAG, "HyperLog isn't initialized: Context couldn't be null")
            return
        }
        HyperLog.context = context.applicationContext
        synchronized(HyperLog::class.java) {
            if (logFormat != null) {
                mLogFormat = logFormat
                LogUtils.saveLogFormat(context, mLogFormat)
            } else {
                mLogFormat = LogUtils.getLogFormat(context)
            }
            if (mDeviceLogList == null) {
                val logDataSource: DeviceLogDataSource =
                    DeviceLogDatabaseHelper.getInstance(context)
                mDeviceLogList = DeviceLogList(logDataSource)
                mDeviceLogList?.clearOldLogs(expiryTimeInSeconds ?: EXPIRY_TIME)
            }
        }
    }

    /**
     * Call this method to define a custom log message format.
     *
     * @param logFormat LogFormat to set custom log message format.
     */
    @JvmStatic
    fun setLogFormat(logFormat: LogFormat) {
        if (mLogFormat != null) {
            mLogFormat = logFormat
            LogUtils.saveLogFormat(context, logFormat)
        }
    }

    private val isInitialize: Boolean
        private get() {
            if (mDeviceLogList == null || mLogFormat == null) {
                context?.let { initialize(it) }
                return false
            }
            return true
        }
    val expiryTime: Long
        /**
         * Call this method to get a expiry time of logs. Expiry Time is in seconds.
         */
        get() = EXPIRY_TIME.toLong()

    /**
     * Sets the level of logging to display, where each level includes all those below it.
     * The default level is LOG_LEVEL_NONE. Please ensure this is set to Log#ERROR
     * or LOG_LEVEL_NONE before deploying your app to ensure no sensitive information is
     * logged. The levels are:
     *
     *  * [Log.ASSERT]
     *  * [Log.VERBOSE]
     *  * [Log.DEBUG]
     *  * [Log.INFO]
     *  * [Log.WARN]
     *  * [Log.ERROR]
     *
     *
     * @param logLevel The level of logcat logging that Parse should do.
     */
    fun setLogLevel(logLevel: Int) {
        HyperLog.logLevel = logLevel
    }

    @JvmStatic
    fun v(tag: String?, message: String, tr: Throwable? = null) {
        if (Log.VERBOSE >= logLevel) {
            Log.v(
                tag, """
     $message
     ${Log.getStackTraceString(tr)}
     """.trimIndent()
            )
        }
        r(getFormattedLog(Log.VERBOSE, tag, message))
    }

    @JvmStatic
    fun d(tag: String?, message: String, tr: Throwable? = null) {
        if (Log.DEBUG >= logLevel) {
            Log.d(
                tag, """
     $message
     ${Log.getStackTraceString(tr)}
     """.trimIndent()
            )
        }
        r(getFormattedLog(Log.DEBUG, tag, message))
    }

    /**
     * Info log level that will store into DB.
     */
    @JvmStatic
    fun i(tag: String?, message: String, tr: Throwable? = null) {
        if (Log.INFO >= logLevel) {
            Log.i(
                tag, """
     $message
     ${Log.getStackTraceString(tr)}
     """.trimIndent()
            )
        }
        r(getFormattedLog(Log.INFO, tag, message))
    }

    @JvmStatic
    fun w(tag: String?, message: String, tr: Throwable? = null) {
        if (Log.WARN >= logLevel) {
            Log.w(
                tag, """
     $message
     ${Log.getStackTraceString(tr)}
     """.trimIndent()
            )
        }
        r(getFormattedLog(Log.WARN, tag, message))
    }

    @JvmStatic
    fun e(tag: String?, message: String, tr: Throwable? = null) {
        if (Log.ERROR >= logLevel) {
            Log.e(
                tag, """
     $message
     ${Log.getStackTraceString(tr)}
     """.trimIndent()
            )
        }
        r(getFormattedLog(Log.ERROR, tag, message))
    }

    @JvmStatic
    fun ze(tag: String?, message: String, tr: Throwable? = null) {
        if (Log.VERBOSE >= logLevel) {
            Log.e(
                tag, """
     $message
     ${Log.getStackTraceString(tr)}
     """.trimIndent()
            )
        }
        zr(getFormattedLog(Log.VERBOSE, tag, message))
    }


    @JvmStatic
    fun exception(tag: String?, message: String?, tr: Throwable? = null) {
        if (Log.ERROR >= logLevel) {
            Log.e(tag, "**********************************************")
            Log.e(
                tag, """
     EXCEPTION: $methodName, $message
     ${Log.getStackTraceString(tr)}
     """.trimIndent()
            )
            Log.e(tag, "**********************************************")
        }
        r(
            getFormattedLog(
                Log.ERROR, tag, "EXCEPTION: " + methodName + ", "
                        + message
            )
        )
    }

    fun exception(tag: String?, e: Exception?) {
        if (e == null) return
        exception(tag, e.message, null)
    }

    fun a(message: String) {
        r(getFormattedLog(Log.ASSERT, TAG_ASSERT, message))
    }

    private val methodName: String
        private get() {
            val stacktrace = Thread.currentThread().stackTrace
            val e = stacktrace[1] //coz 0th will be getStackTrace so 1st
            return e.methodName
        }

    private fun r(message: String?) {
        try {
//            appendLog(context, message);
            if (executorService == null) executorService = Executors.newSingleThreadExecutor()
            val runnable = Runnable {
                try {
                    if (!isInitialize || message == null || message.isEmpty()) return@Runnable

//                        mDeviceLogList.addDeviceLog(message);
//                        getDeviceLogsInFile(context);
                    appendLog(message)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
            executorService?.submit(runnable)
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun zr(message: String?) {
        try {
//            appendLog(context, message);
            if (executorService == null) executorService = Executors.newSingleThreadExecutor()
            val runnable = Runnable {
                try {
                    if (!isInitialize || message == null || message.isEmpty()) return@Runnable

//                        mDeviceLogList.addDeviceLog(message);
//                        getDeviceLogsInFile(context);
                    appendzeLog(message)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
            executorService?.submit(runnable)
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun appendLog(text: String?) {
        val logFileName = todayDate(API_RETURN_DATE_FORMAT) + "_log.txt"
        val path = File("$DOWNLOAD_DIR_PATH/LOGS/")
        val logFile = File(path, logFileName)
        try {
            if (!path.exists()) {
                path.mkdirs()
            }
            if (!logFile.exists()) {
                logFile.createNewFile()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        try {
            val buf = BufferedWriter(FileWriter(logFile, true))
            buf.append(text)
            buf.newLine()
            buf.close()
            var lineNumberReader = LineNumberReader(FileReader(logFile))
            lineNumberReader.skip(Long.MAX_VALUE)
            var lines = lineNumberReader.lineNumber
            while (lines >= 3000) {
                deleteContentAtIndex(filePath = logFile.absolutePath, 1)
                lineNumberReader = LineNumberReader(FileReader(logFile))
                lineNumberReader.skip(Long.MAX_VALUE)
                lines = lineNumberReader.lineNumber
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    fun appendzeLog(text: String?) {
        val logFileName = "extractionlog.txt"
        val path = File("$DOWNLOAD_DIR_PATH/LOGS/")
        val logFile = File(path, logFileName)
        try {
            if (!path.exists()) {
                path.mkdirs()
            }
            if (!logFile.exists()) {
                logFile.createNewFile()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        try {
            val buf = BufferedWriter(FileWriter(logFile, true))
            buf.append(text)
            buf.newLine()
            buf.close()
            var lineNumberReader = LineNumberReader(FileReader(logFile))
            lineNumberReader.skip(Long.MAX_VALUE)
            var lines = lineNumberReader.lineNumber
            while (lines >= 500) {
                deleteContentAtIndex(filePath = logFile.absolutePath, 1)
                lineNumberReader = LineNumberReader(FileReader(logFile))
                lineNumberReader.skip(Long.MAX_VALUE)
                lines = lineNumberReader.lineNumber
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun getLines(lineCount: Int, onresult: (data: String) -> Unit) {
        var text = ""
        GlobalScope.executeAsyncTask(onPreExecute = {}, doInBackground = {
            var logFileName = todayDate(API_RETURN_DATE_FORMAT) + "_log.txt"
            val path = File("$DOWNLOAD_DIR_PATH/LOGS/")
            if (path.exists()){
                path.listFiles { dir, name ->
                    if (name.contains(todayDate(API_RETURN_DATE_FORMAT))){
                        logFileName = name
                    }
                    true
                }
            }
            val logFile = File(path, logFileName)
            try {
                if (!path.exists()) {
                    path.mkdirs()
                }
                if (!logFile.exists()) {
                    text = ""
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            try {
                var lineNumberReader = LineNumberReader(FileReader(logFile))
                lineNumberReader.skip(Long.MAX_VALUE)
                var lines = lineNumberReader.lineNumber
                var startLines = lines - lineCount - 1
                for (i in startLines..lines - 1) {
                    var bf = BufferedReader(FileReader(logFile))
                    text =
                        text + "\n" + bf.lines().skip(i.toLong())
                            .findFirst().get()
                    bf.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            "Result"
        }, onPostExecute = {
            onresult.invoke(text)
        })
    }

    fun deleteContentAtIndex(filePath: String, indexPos: Int) {
        val bufferedWriter = File(filePath).bufferedWriter()
        File(filePath).readLines().filterIndexed { i, _ -> i != indexPos }.forEach {
            bufferedWriter.appendLine(it)
        }
        bufferedWriter.flush()
    }

    val deviceLogs: List<DeviceLogModel>
        /**
         * Call this method to get a list of stored Device Logs
         *
         * @return List of [DeviceLogModel]
         */
        get() = getDeviceLogs(true)

    /**
     * Call this method to get a list of stored Device Logs
     *
     * @param deleteLogs If true then logs will delete from the device.
     * @return List of [DeviceLogModel]
     */
    fun getDeviceLogs(deleteLogs: Boolean): List<DeviceLogModel> {
        return getDeviceLogs(deleteLogs, 1)
    }

    /**
     * Call this method to get a list of stored Device Logs.
     *
     * @param deleteLogs If true then logs will delete from the device.
     * @param batchNo    If there are more than one batch of device log then specify the batch number.
     * Batch number should be greater than or equal to 1.
     * @return List of [DeviceLogModel] or empty list if batch number is greater than the
     * [HyperLog.getDeviceLogBatchCount]
     */
    fun getDeviceLogs(deleteLogs: Boolean, batchNo: Int): List<DeviceLogModel> {
        var deviceLogs: List<DeviceLogModel> = ArrayList()
        if (!isInitialize) return deviceLogs
        deviceLogs = mDeviceLogList!!.getDeviceLogs(batchNo)
        if (deleteLogs) {
            mDeviceLogList!!.clearDeviceLogs(deviceLogs)
        }
        return deviceLogs
    }

    val deviceLogsAsStringList: List<String>
        /**
         * Call this method to get a list of stored Device Logs.
         * Device logs will gets deleted from device after fetching.
         *
         * @return List of [String]
         */
        get() = getDeviceLogsAsStringList(true)

    /**
     * Call this method to get a list of stored Device Logs
     *
     * @param deleteLogs If true then logs will delete from the device.
     * @return List of [String]
     */
    fun getDeviceLogsAsStringList(deleteLogs: Boolean): List<String> {
        return getDeviceLogsAsStringList(deleteLogs, 1)
    }

    /**
     * Call this method to get a list of stored Device Logs
     *
     * @param deleteLogs If true then logs will delete from the device.
     * @param batchNo    If there are more than one batch of device log then specify the batch number.
     * Batch number should be greater than or equal to 1.
     * @return List of [String] or if the given batchNo is greater than the
     * [HyperLog.getDeviceLogBatchCount] then returns empty list;
     */
    fun getDeviceLogsAsStringList(deleteLogs: Boolean, batchNo: Int): List<String> {
        val logsList: List<String> = ArrayList()
        if (!isInitialize) return logsList
        return if (!hasPendingDeviceLogs()) {
            logsList
        } else getDeviceLogsAsStringList(
            getDeviceLogs(
                deleteLogs,
                batchNo
            )
        )
    }

    /**
     * Method to get a list of stored Device Logs
     *
     * @param deviceLogList List of all device logs
     * @return List of [String]
     */
    private fun getDeviceLogsAsStringList(deviceLogList: List<DeviceLogModel>?): List<String> {
        val logsList: MutableList<String> = ArrayList()
        if (deviceLogList == null) {
            return logsList
        }
        for (deviceLog in deviceLogList) {
            logsList.add(deviceLog.deviceLog)
        }
        return logsList
    }

    /**
     * Call this method to get a stored Device Logs as a File object.
     * A text file will create in the app folder containing all logs with the current date time as
     * name of the file.
     *
     * @param mContext The current context.
     * @return [File] object or `null` if there is not any logs in device.
     */
    fun getDeviceLogsInFile(mContext: Context?): File? {
        return getDeviceLogsInFile(mContext, null)
    }

    /**
     * Call this method to get a stored Device Logs as a File object.
     * A text file will create in the app folder containing all logs with the current date time as
     * name of the file.
     *
     * @param mContext   The current context.
     * @param deleteLogs If true then logs will delete from the device.
     * @return [File] object or `null` if there is not any logs in device.
     */
    fun getDeviceLogsInFile(mContext: Context?, deleteLogs: Boolean): File? {
        return getDeviceLogsInFile(mContext, null, deleteLogs)
    }

    /**
     * Call this method to get a stored Device Logs as a File object.
     * A text file will create in the app folder containing all logs with the current date time as
     * name of the file.
     *
     * @param mContext The current context.
     * @param fileName Name of the file.
     * @return [File] object or `null` if there is not any logs in device.
     */
    fun getDeviceLogsInFile(mContext: Context?, fileName: String?): File? {
        return getDeviceLogsInFile(mContext, fileName, true)
    }

    /**
     * Call this method to get a stored Device Logs as a File object.
     * A text file will create in the app folder containing all logs.
     *
     * @param mContext   The current context.
     * @param fileName   Name of the file.
     * @param deleteLogs If true then logs will delete from the device.
     * @return [File] object, or `null` if there is not any logs in device.
     */
    fun getDeviceLogsInFile(mContext: Context?, fileName: String?, deleteLogs: Boolean): File? {
        var fileName = fileName
        if (!isInitialize) return null
        var file: File? = null
        if (TextUtils.isEmpty(fileName)) {
            fileName = HLDateTimeUtility.getCurrentTime() + ".txt"
            fileName = fileName.replace("[^a-zA-Z0-9_\\\\-\\\\.]".toRegex(), "_")
        }

        //Check how many batches of device logs are available to push
        var logsBatchCount = deviceLogBatchCount
        while (logsBatchCount != 0) {
            val deviceLogList = getDeviceLogs(deleteLogs)
            if (deviceLogList != null && !deviceLogList.isEmpty()) {
                file = LogUtils.writeStringsToFile(
                    mContext, getDeviceLogsAsStringList(deviceLogList),
                    fileName
                )
                if (file != null) {
                    if (deleteLogs) mDeviceLogList!!.clearDeviceLogs(deviceLogList)
                    i(
                        "LOG", "Log File has been created at " +
                                file.absolutePath
                    )
                }
            }
            logsBatchCount--
        }
        return file
    }

    /**
     * Call this method to check whether any device logs are available.
     *
     * @return true If device has some pending logs otherwise false.
     */
    fun hasPendingDeviceLogs(): Boolean {
        if (!isInitialize) return false
        val deviceLogsCount = mDeviceLogList!!.count()
        return deviceLogsCount > 0L
    }

    val deviceLogsCount: Long
        /**
         * Call this method to get the count of stored device logs.
         *
         * @return The number of device logs.
         */
        get() = if (!isInitialize) 0 else mDeviceLogList!!.count()
    val deviceLogBatchCount: Int
        /**
         * Call this method to get number of device logs batches. Each batch contains the 5000 device
         * logs.
         *
         * @return The number of device logs batches.
         */
        get() = if (!isInitialize) 0 else mDeviceLogList!!.deviceLogBatchCount

    /**
     * Call this method to delete all logs from device.
     */
    fun deleteLogs() {
        if (!isInitialize) return
        mDeviceLogList!!.clearSavedDeviceLogs()
    }

    private fun getFormattedLog(logLevel: Int, tag: String?, message: String): String? {
        return if (isInitialize) {
            mLogFormat!!.formatLogMessage(logLevel, tag, message)
        } else null
    }
}