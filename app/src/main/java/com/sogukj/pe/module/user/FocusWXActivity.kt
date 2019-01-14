package com.sogukj.pe.module.user

import android.graphics.BitmapFactory
import android.graphics.Paint
import android.os.Bundle
import android.os.Environment
import androidx.core.view.doOnLayout
import com.bumptech.glide.Glide
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.ExtrasDelegate
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.peUtils.ZxingUtils
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_focus_wx.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.toast
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class FocusWXActivity : ToolbarActivity() {
    private lateinit var outputPic: File
    private val qrUrl: String by ExtrasDelegate(Extras.URL, "")

    companion object {
        fun makeTempFile(saveDir: String, prefix: String, extension: String): File {
            val timeStamp = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            val dir = File(saveDir)
            dir.mkdirs()
            return File(dir, prefix + timeStamp + extension)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_focus_wx)
        setBack(true)
        title = "关注教程"
        step1.paint.flags = Paint.UNDERLINE_TEXT_FLAG


        outputPic = makeTempFile(Environment.getExternalStorageDirectory().path + "/Sogu/Saas/WX/", "wx_", ".jpg")
        parentLayout.doOnLayout {
            Observable.just(qrUrl)
                    .map { ZxingUtils.createQRImage(it, QRCodeImg.width, QRCodeImg.height, null, outputPic.absolutePath) }
                    .execute {
                        onNext { b ->
                            if (b) {
                                Glide.with(this@FocusWXActivity)
                                        .load(BitmapFactory.decodeFile(outputPic.absolutePath))
                                        .into(QRCodeImg)
                            }
                        }
                    }
        }
        step1.clickWithTrigger {
            Utils.saveImageToGallery(ctx,outputPic.absolutePath)
            toast("二维码已经保存至本地")
        }

    }

}
