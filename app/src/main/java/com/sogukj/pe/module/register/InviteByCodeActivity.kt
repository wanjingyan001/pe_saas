package com.sogukj.pe.module.register

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import androidx.core.view.doOnLayout
import com.bumptech.glide.Glide
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.peUtils.ZxingUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_invite_by_code.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class InviteByCodeActivity : ToolbarActivity() {
    private lateinit var outputPic: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invite_by_code)
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar?.setBackgroundColor(resources.getColor(R.color.white))
        setBack(true)

        outputPic = makeTempFile(Environment.getExternalStorageDirectory().path + "/Sogu/Saas/", "qr_", ".jpg")
        parentLayout.doOnLayout {
            Observable.just("www.baidu.com")
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .map { ZxingUtils.createQRImage(it, QRCodeImg.width, QRCodeImg.height, null, outputPic.absolutePath) }
                    .subscribe { b ->
                        if (b) {
                            Glide.with(this)
                                    .load(BitmapFactory.decodeFile(outputPic.absolutePath))
                                    .into(QRCodeImg)
                        }
                    }
        }

        inviteCodeLayout.clickWithTrigger {
            val manager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            manager.primaryClip = ClipData.newPlainText(null, inviteCode.text)
            toast("已复制到剪贴板")
        }
    }


    companion object {
        fun makeTempFile(saveDir: String, prefix: String, extension: String): File {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val dir = File(saveDir)
            dir.mkdirs()
            return File(dir, prefix + timeStamp + extension)
        }
    }
}
