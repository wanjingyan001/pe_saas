package com.sogukj.pe.peUtils

import android.graphics.Bitmap
import android.graphics.Canvas
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.io.FileOutputStream

/**
 * Created by admin on 2018/6/5.
 */
class ZxingUtils {
    companion object {
        fun addLogo(src: Bitmap, logo: Bitmap): Bitmap {
            val srcWidth = src.width
            val srcHeight = src.height
            val logoWidth = logo.width
            val logoHeight = logo.height
            if (logoWidth == 0 || logoHeight == 0) {
                return src
            }
            val scaleFactor = srcWidth * 1.0f / 5 / logoWidth
            val bitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawBitmap(src, 0f, 0f, null)
            canvas.scale(scaleFactor, scaleFactor, srcWidth / 2f, srcHeight / 2f)
            canvas.drawBitmap(logo, (srcWidth - logoWidth) / 2f, (srcHeight - logoHeight) / 2f, null)
            canvas.save()
            canvas.restore()
            return bitmap
        }

        fun createQRImage(content: String, widthPix: Int, heightPix: Int, logo: Bitmap?, filePath: String): Boolean {
            try {
                if (content.isEmpty()) {
                    return false
                }
                //配置参数
                val hints = HashMap<EncodeHintType, Any>()
                hints.put(EncodeHintType.CHARACTER_SET, "utf-8")
                //容错级别
                hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L)
                //设置空白边距的宽度
                hints.put(EncodeHintType.MARGIN, 0)
                val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, widthPix, heightPix, hints)
                val pixels = IntArray(widthPix * heightPix)
                // 下面这里按照二维码的算法，逐个生成二维码的图片，
                // 两个for循环是图片横列扫描的结果
                for (y in 0 until heightPix) {
                    for (x in 0 until widthPix) {
                        if (bitMatrix.get(x, y)) {
                            pixels[y * widthPix + x] = 0xff000000.toInt()
                        } else {
                            pixels[y * widthPix + x] = 0xffffffff.toInt()
                        }
                    }
                }
                // 生成二维码图片的格式，使用ARGB_8888
                var bitmap = Bitmap.createBitmap(widthPix, heightPix, Bitmap.Config.ARGB_8888)
                bitmap.setPixels(pixels, 0, widthPix, 0, 0, widthPix, heightPix)
                if (logo != null) {
                    bitmap = addLogo(bitmap, logo)
                }
                //必须使用compress方法将bitmap保存到文件中再进行读取。直接返回的bitmap是没有任何压缩的，内存消耗巨大！
                return bitmap != null && bitmap.compress(Bitmap.CompressFormat.JPEG, 100, FileOutputStream(filePath))
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return false
        }
    }
}