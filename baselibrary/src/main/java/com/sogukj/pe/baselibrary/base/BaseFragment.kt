package com.sogukj.pe.baselibrary.base

import android.app.ProgressDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.support.annotation.DrawableRes
import android.support.v4.app.Fragment
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.gson.JsonSyntaxException
import com.sogukj.pe.baselibrary.R
import com.sogukj.pe.baselibrary.widgets.snackbar.Prompt
import com.sogukj.pe.baselibrary.widgets.snackbar.TSnackbar
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.find
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.support.v4.ctx
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Created by qinfei on 17/7/18.
 */

abstract class BaseFragment : Fragment(),AnkoLogger {

    abstract val containerViewId: Int
    val handler = Handler()
    var baseActivity: BaseActivity? = null
        private set
    val sp : SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(ctx) }

    open val titleId: Int
        get() = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        baseActivity = activity as BaseActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        inflateView = layoutInflater.inflate(R.layout.layout_custom_toast, null)
        iconImg = inflateView.find(R.id.toast_icon)
        tv = inflateView.find(R.id.toast_tv)
        return if (containerViewId != 0) {
            inflater.inflate(containerViewId, container, false)
        } else {
            super.onCreateView(inflater, container, savedInstanceState)
        }
    }

    private var toast: Toast? = null
    fun showToast(text: CharSequence?) {
        if (toast == null) {
            toast = Toast.makeText(baseActivity,
                    text?.toString(),
                    Toast.LENGTH_SHORT)
        } else {
            toast!!.setText(text)
        }
        toast!!.show()
    }

    fun ToastError(e: Throwable) {
        var str = when (e) {
            is JsonSyntaxException -> "后台数据出错"
            is UnknownHostException -> "网络连接出错，请联网"
            is SocketTimeoutException -> "连接超时"
            else -> "未知错误"
        }
        showCustomToast(R.drawable.icon_toast_fail, str)
    }

    lateinit var inflateView: View
    lateinit var iconImg: ImageView
    lateinit var tv: TextView
    private var toastView: Toast? = null

    fun showCustomToast(@DrawableRes resId: Int?, text: CharSequence?) {
        if (resId != null) {
            iconImg.imageResource = resId
        } else {
            iconImg.visibility = View.GONE
        }
        val width = baseActivity!!.windowManager.defaultDisplay.width
        tv.maxWidth = width / 3
        tv.text = text
        if (toastView == null) {
            toastView = Toast(baseActivity)
        }
        toastView?.let {
            it.setGravity(Gravity.CENTER_VERTICAL, 0, -50)
            it.duration = Toast.LENGTH_SHORT
            it.view = inflateView
            it.show()
        }
    }

    fun showErrorToast(text: CharSequence?) {
        showCustomToast(R.drawable.icon_toast_fail, text)
    }

    fun showSuccessToast(text: CharSequence?) {
        showCustomToast(R.drawable.icon_toast_success, text)
    }

    fun showCommonToast(text: CharSequence?) {
        showCustomToast(R.drawable.icon_toast_common, text)
    }

    fun showTopSnackBar(text: String) {
        val viewGroup = baseActivity?.findViewById<View>(android.R.id.content)?.rootView as? ViewGroup//注意getRootView()最为重要，直接关系到TSnackBar的位置
        if (viewGroup != null) {
            TSnackbar.make(viewGroup, text, TSnackbar.LENGTH_SHORT, TSnackbar.APPEAR_FROM_TOP_TO_DOWN)
                    .setTextColor(resources.getColor(R.color.white))
                    .setMessageTextSize(16)
                    .setPromptThemBackground(Prompt.WARNING)
                    .show()
        }
    }

    var progressDialog: ProgressDialog? = null
    fun showProgress(msg: String) {
        if (progressDialog == null) {
            progressDialog = ProgressDialog(activity)
        }
        progressDialog?.setMessage(msg)
        progressDialog?.show()
    }

    fun showProgress(msg: String, theme: Int) {
        if (progressDialog == null) {
            progressDialog = ProgressDialog(activity)
        }
        progressDialog?.setMessage(msg)
        progressDialog?.setProgressStyle(theme)
        progressDialog?.show()
    }

    fun hideProgress() {
        if (progressDialog != null) {
            progressDialog?.dismiss()
        }
    }
}
