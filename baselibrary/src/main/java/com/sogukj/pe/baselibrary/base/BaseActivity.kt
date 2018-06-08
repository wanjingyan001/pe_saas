package com.sogukj.pe.baselibrary.base

import android.app.Activity
import android.app.ProgressDialog
import android.os.Bundle
import android.os.Handler
import android.os.Trace
import android.support.annotation.DrawableRes
import android.support.constraint.ConstraintLayout
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.gson.JsonSyntaxException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import com.sogukj.pe.baselibrary.R
import com.umeng.message.PushAgent
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.find
import org.jetbrains.anko.imageResource
import android.support.v4.view.accessibility.AccessibilityEventCompat.setAction
import com.sogukj.pe.baselibrary.widgets.snackbar.TSnackbar
import android.view.ViewGroup
import com.sogukj.pe.baselibrary.widgets.snackbar.Prompt


/**
 * Created by qinfei on 17/7/17.
 */
abstract class BaseActivity : AppCompatActivity(), AnkoLogger {
    val context: BaseActivity
        get() = this
    val handler = Handler()
    lateinit var inflate: View
    lateinit var iconImg: ImageView
    lateinit var tv: TextView
    private var toastView: Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityHelper.add(this)
        PushAgent.getInstance(this).onAppStart()
        initEmptyView()
        inflate = layoutInflater.inflate(R.layout.layout_custom_toast, null)
        iconImg = inflate.find(R.id.toast_icon)
        tv = inflate.find(R.id.toast_tv)
    }

    override fun onStart() {
        super.onStart()
        activeCount++
    }

    override fun onStop() {
        super.onStop()
        activeCount--
        if (activeCount == 0) {
        }

    }

    val statusBarSize: Int
        get() {
            var result = 0
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                result = resources.getDimensionPixelSize(resourceId)
            }
            return result
        }


    val screenHeight: Int
        get() = windowManager.defaultDisplay.height

    val screenWidth: Int
        get() = windowManager.defaultDisplay.width

    var progressDialog: ProgressDialog? = null
    fun showProgress(msg: String) {
        if (progressDialog == null) {
            progressDialog = ProgressDialog(this)
        }
        progressDialog?.setMessage(msg)
        progressDialog?.show()
    }

    fun showProgress(msg: String, theme: Int) {
        if (progressDialog == null) {
            progressDialog = ProgressDialog(this)
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

    var layout: ConstraintLayout? = null
    private fun initEmptyView() {
        if (findViewById<ConstraintLayout>(R.id.networkErrorLayout) != null) {
            layout = find(R.id.networkErrorLayout)
            if (layout == null) {
                return
            } else {
                layout?.let {
                    it.visibility = View.GONE
                }
            }
        }
    }

    protected fun showEmptyView() {
        layout?.let {
            it.visibility = View.VISIBLE
        }
    }

    protected fun hideEmptyView() {
        layout?.let {
            it.visibility = View.GONE
        }
    }


    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        hideProgress()
        ActivityHelper.remove(this)
        super.onDestroy()
    }


    private var toast: Toast? = null
    fun showToast(text: CharSequence?) {
        //文件上传未完成，界面销毁
        if (context !is Activity) {
            return
        }
        if (toast == null) {
            toast = Toast.makeText(this,
                    text?.toString(),
                    Toast.LENGTH_SHORT)
        } else {
            toast!!.setText(text)
        }
        toast!!.show()
    }

    fun ToastError(e: Throwable) {
        val str = when (e) {
            is JsonSyntaxException -> "后台数据出错"
            is UnknownHostException -> "网络连接出错，请联网"
            is SocketTimeoutException -> "连接超时"
            else -> "未知错误"
        }
        showCustomToast(R.drawable.icon_toast_fail, str)
    }


    fun showCustomToast(@DrawableRes resId: Int?, text: CharSequence?) {
        if (resId != null) {
            iconImg.imageResource = resId
        } else {
            iconImg.visibility = View.GONE
        }
        tv.maxWidth = screenWidth / 3
        tv.text = text
        if (toastView == null) {
            toastView = Toast(this)
        }
        toastView?.let {
            it.setGravity(Gravity.CENTER_VERTICAL, 0, -50)
            it.duration = Toast.LENGTH_SHORT
            it.view = inflate
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

    fun showTopSnackBar(text: CharSequence) {
        val viewGroup = findViewById<View>(android.R.id.content).rootView as ViewGroup//注意getRootView()最为重要，直接关系到TSnackBar的位置
        TSnackbar.make(viewGroup, text, TSnackbar.LENGTH_SHORT, TSnackbar.APPEAR_FROM_TOP_TO_DOWN)
                .setPromptThemBackground(Prompt.WARNING)
                .setTextColor(resources.getColor(R.color.white))
                .setMessageTextSize(16)
                .show()
    }

    /**
     * 官方推荐使用CoordinatorLayout做为根布局
     */
    fun showTopSnackBar(text: CharSequence, container: CoordinatorLayout) {
        TSnackbar.make(container, text, TSnackbar.LENGTH_SHORT, TSnackbar.APPEAR_FROM_TOP_TO_DOWN)
                .setPromptThemBackground(Prompt.WARNING)
                .setTextColor(resources.getColor(R.color.white))
                .setMessageTextSize(16)
                .show()
    }


    fun showDesignSnack(text: CharSequence) {
        val viewGroup = findViewById<View>(android.R.id.content).rootView as ViewGroup
        Snackbar.make(viewGroup, text, Snackbar.LENGTH_SHORT).show()
    }

    companion object {
        val TAG = BaseActivity::class.java.simpleName
        var activeCount = 0
    }
}
