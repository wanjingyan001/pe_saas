package com.sogukj.pe.baselibrary.utils

import android.graphics.Bitmap
import android.R.attr.bitmap
import android.content.Context
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.R.attr.bitmap
import android.renderscript.Allocation


/**
 * 高斯模糊处理
 * Created by admin on 2018/10/26.
 */
class RSBlurUtils {

    companion object {
        fun blurBitmap(bitmap: Bitmap, ctx: Context): Bitmap {
            val outBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            val script = RenderScript.create(ctx)
            val blur = ScriptIntrinsicBlur.create(script, Element.U8_4(script))
            val allIn = Allocation.createFromBitmap(script, bitmap)
            val allOut = Allocation.createFromBitmap(script, outBitmap)
            blur.setRadius(25f)
            blur.setInput(allIn)
            blur.forEach(allOut)
            allOut.copyTo(outBitmap)
            bitmap.recycle()
            blur.destroy()
            return outBitmap
        }
    }
}