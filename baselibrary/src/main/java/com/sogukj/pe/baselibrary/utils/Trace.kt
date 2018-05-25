package com.sogukj.pe.baselibrary.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import android.util.Log
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.*

/**
 * Log日志打印类
 *
 *
 * Trace.d和Trace.w不写入日志文件
 *
 *
 *
 *
 * Trace.i和Trace.e写入日志文件
 *
 *

 * @author qinfei
 */
@SuppressLint("SimpleDateFormat")
object Trace {
    val SDCARD = Environment
            .getExternalStorageDirectory().absolutePath
    var TAG = Trace::class.java.simpleName
    var df: DateFormat = SimpleDateFormat("yyyyMMdd")
    var LOG_SAVE_DIR = SDCARD + "/trace/"
    internal var log = Logger.getLogger(TAG)
    val isDebug = true

    fun init(ctx: Context) {
        try {
            var logDir = ctx.getExternalFilesDir(null)
            if (null == logDir) logDir = ctx.filesDir
            LOG_SAVE_DIR = logDir!!.absolutePath
            if (!logDir.exists()) {
                logDir.mkdirs()
            }
            val fHandler = FileHandler(LOG_SAVE_DIR + "/" + df.format(Date()) + ".log", true)
            fHandler.level = Level.ALL
            fHandler.formatter = SimpleFormatter()
            log.addHandler(fHandler)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        //        CrashHandler.init(ctx);
        i(TAG, "init logs")
        i(TAG, LOG_SAVE_DIR)
        try {
            val pm = ctx.packageManager
            val pi = pm.getPackageInfo(ctx.packageName,
                    PackageManager.GET_ACTIVITIES)
            val buf = StringBuffer()
            buf.append("time:")
                    .append(DateFormat.getDateTimeInstance().format(
                            System.currentTimeMillis())).append("\n")
            buf.append("package:").append(pi.packageName).append("\n")
            buf.append("suid:")
                    .append(pi.sharedUserLabel.toString() + "," + pi.sharedUserId)
                    .append("\n")
            buf.append("version:")
                    .append(pi.versionName + "," + pi.versionCode).append("\n")
            //            buf.append("rom:").append(SysProp.getSystemVersion()).append("\n");
            //            buf.append("mac:").append(Utils.getMacAddress(ctx)).append("\n");
            //            buf.append("ip:").append(Utils.getLocalIpAddress()).append("\n");
            i("init", "" + buf.toString())
        } catch (e: Exception) {
        }

    }

    //    public static void d(String tag, Object msg) {
    //        if (DEBUG) Log.d(TAG, format(tag, gson.toJson(msg)));
    //    }
    fun d(tag: String, msg: String?) {
        Log.i(TAG, format(tag, msg))
    }

    fun w(tag: String, msg: String?) {
        Log.e(TAG, format(tag, msg))
    }

    fun w(tag: String, msg: String, tr: Throwable) {
        if (isDebug) Log.e(TAG, format(tag, msg), tr)
    }

    fun i(tag: String, msg: String?) {
        log(Level.INFO, tag, msg, null)
    }

    fun e(tr: Throwable?) {
        log(Level.SEVERE, "", "", tr)
    }

    fun e(tag: String, msg: String?) {
        log(Level.SEVERE, tag, msg, null)
    }

    fun e(tag: String, msg: String, tr: Throwable) {
        log(Level.SEVERE, tag, msg, tr)
    }

    private fun log(level: Level, tag: String, msg: String?, tr: Throwable?) {
        val record = LogRecord(level, format(tag, msg))
        record.thrown = tr
        if (isDebug)
            log.log(record)
    }

    private fun format(tag: String, msg: String?): String {
        return "<$tag> $msg "
    }


    internal class CrashHandler private constructor() : Thread.UncaughtExceptionHandler {
        private val mDefaultHandler: Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler()

        init {
            Thread.setDefaultUncaughtExceptionHandler(this)
        }

        override fun uncaughtException(thread: Thread, ex: Throwable) {
            if (!handleException(ex) && mDefaultHandler != null) {
                mDefaultHandler.uncaughtException(thread, ex)
            }
            Thread(Runnable {
                e(TAG, "uncaughtException", ex)
                android.os.Process.killProcess(android.os.Process.myPid())
                // System.exit(0);
            }).start()
        }

        private fun handleException(ex: Throwable?): Boolean {
            if (ex == null) {
                return true
            }
            e(TAG, "handleException", ex)
            return true
        }

        companion object {
            val TAG = CrashHandler::class.java.simpleName
            private var sInst: CrashHandler? = CrashHandler()

            fun init() {
                if (sInst == null) sInst = CrashHandler()
            }
        }
    }
}
