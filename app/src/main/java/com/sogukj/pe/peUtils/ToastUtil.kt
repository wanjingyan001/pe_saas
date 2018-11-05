package com.sogukj.pe.peUtils

import android.content.Context
import android.support.annotation.DrawableRes
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.sogukj.pe.baselibrary.R
import com.sogukj.pe.baselibrary.utils.Utils

/**
 * Created by CH-ZH on 2018/8/18.
 */
class ToastUtil {
    companion object {
        private var toastView: Toast? = null
        fun showSuccessToast(text: CharSequence?,context: Context) {
            showCustomToast(R.drawable.icon_toast_success, text,context)
        }

        fun showErrorToast(text: CharSequence?,context: Context){
            showCustomToast(R.drawable.icon_toast_fail, text,context)
        }

        fun showWarnToast(text: CharSequence?,context: Context){
            showCustomToast(R.drawable.icon_toast_common, text,context)
        }
        fun showCustomToast(@DrawableRes resId: Int?, text: CharSequence?,context:Context) {
            var inflate = View.inflate(context,R.layout.layout_custom_toast, null)
            var iconImg = inflate.findViewById<ImageView>(R.id.toast_icon)
            var tv = inflate.findViewById<TextView>(R.id.toast_tv)
            if (resId != null) {
                iconImg.setImageResource(resId)
            } else {
                iconImg.visibility = View.GONE
            }
            tv.maxWidth = Utils.getScreenWidth(context) / 3
            tv.text = text
            if (toastView == null) {
                toastView = Toast(context)
            }
//            val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.bg_custom_toast).copy(Bitmap.Config.ARGB_8888, true)
//            inflate.background = BitmapDrawable(context.resources,
//                    RSBlurUtils.blurBitmap(bitmap,
//                            context.applicationContext))
            toastView?.let {
                it.setGravity(Gravity.CENTER_VERTICAL, 0, -50)
                it.duration = Toast.LENGTH_SHORT
                it.view = inflate
                it.show()
            }
        }
    }
}