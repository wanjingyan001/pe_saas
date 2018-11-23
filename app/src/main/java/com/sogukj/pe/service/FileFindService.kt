package com.sogukj.pe.service

import android.app.IntentService
import android.app.Service
import android.content.Intent
import android.os.Environment
import android.os.IBinder
import android.preference.PreferenceManager
import androidx.core.content.edit
import com.sogukj.pe.baselibrary.Extended.jsonStr
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import java.io.File

class FileFindService : IntentService("FileFind") {
    override fun onHandleIntent(intent: Intent?) {
        recursionFile()
    }


    fun recursionFile(dir: File = Environment.getExternalStorageDirectory()) {
        val files = dir.listFiles() ?: return
        val list = files.filter { it.isDirectory && it.name.equals("DingTalk", true) }
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        AnkoLogger("WJY").info { list.jsonStr }
        sp.edit { putString("DingFileDir", list.jsonStr) }
    }
}
