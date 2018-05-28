package com.sogukj.pe.baselibrary.Extended

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.support.v7.util.DiffUtil
import android.support.v7.util.DiffUtil.calculateDiff
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.ashokvarma.bottomnavigation.BottomNavigationItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sogukj.pe.baselibrary.R
import com.sogukj.pe.baselibrary.utils.DiffCallBack
import com.sogukj.pe.baselibrary.widgets.OnClickFastListener
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * kotlin扩展方法
 * Created by admin on 2018/3/30.
 */

inline fun <reified T> Gson.fromJson(json: String): T {
    return fromJson(json, T::class.java)
}

inline fun <reified T> Gson.arrayFromJson(json: String): List<T> {
    return Gson().fromJson(json, object : TypeToken<List<T>>() {}.type)
}

fun View.setOnClickFastListener(listener: OnClickFastListener.(v: View) -> Unit) {
    this.setOnClickListener {
        listener.invoke(object : OnClickFastListener() {
            override fun onFastClick(v: View) {

            }
        }, it)
    }
}

fun <T> List<T>?.isNullOrEmpty(): Boolean = this == null || this.isEmpty()
/**
 * 判断EditText的text是否不为空
 */
fun EditText.isNullOrEmpty(): Boolean =
        text?.trim().isNullOrEmpty()

/**
 * edittext扩展属性，获取其文本
 */
val EditText.textStr: String
    get() = text.trim().toString()

val EditText.noSpace: String
    get() = text.trimStart().trimEnd().toString()

/**
 * 扩展View是否可见，VISIBLE 与 GONE。
 */
fun View.setVisible(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}


fun <T> Observable<T>.execute(init: Ex_T0_Unit<SubscriberHelper<T>>) {
    val subscriberHelper = SubscriberHelper<T>()
    init(subscriberHelper)
    subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(subscriberHelper)
}


fun CharSequence?.checkEmpty(): CharSequence {
    return if (this == null || this.isEmpty() || this == "null")
        ""
    else
        this
}

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
fun Activity.fullScreen() {
    val sdkInt = Build.VERSION.SDK_INT
    if (sdkInt >= Build.VERSION_CODES.KITKAT) {
        if (sdkInt >= Build.VERSION_CODES.LOLLIPOP) {
            val decorView = window.decorView
            val option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN.or(View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
            decorView.systemUiVisibility = option
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
        }
    } else {
        val attributes = window.attributes
        attributes.flags = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
        window.attributes = attributes
    }
}

fun BottomNavigationItem.initNavTextColor(): BottomNavigationItem =
        setActiveColorResource(R.color.main_bottom_bar_color)
                .setInActiveColorResource(R.color.text_3)

fun BottomNavigationItem.initNavTextColor1(): BottomNavigationItem =
        setActiveColorResource(R.color.white)
                .setInActiveColorResource(R.color.text_3)


fun Context.showInput(view: View) {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(view, 0)
}

fun ViewGroup.childEdtGetFocus() {
    (0 until this.childCount)
            .map { getChildAt(it) }
            .filterIsInstance<EditText>()
            .forEach {
                this.setOnClickListener {
                    it.isFocusable = true
                    it.isFocusableInTouchMode = true
                    it.requestFocus()
                    this.context.showInput(it)
                }
            }
}


/***
 * 设置延迟时间的View扩展
 * @param delay Long 延迟时间，默认600毫秒
 * @return T
 */
fun <T : View> T.withTrigger(delay: Long = 600): T {
    triggerDelay = delay
    return this
}

/***
 * 点击事件的View扩展
 * @param block: (T) -> Unit 函数
 * @return Unit
 */
fun <T : View> T.click(block: (T) -> Unit) = setOnClickListener {

    if (clickEnable()) {
        block(it as T)
    }
}

/***
 * 带延迟过滤的点击事件View扩展
 * @param delay Long 延迟时间，默认600毫秒
 * @param block: (T) -> Unit 函数
 * @return Unit
 */
fun <T : View> T.clickWithTrigger(time: Long = 600, block: (T) -> Unit) {
    triggerDelay = time
    setOnClickListener {
        if (clickEnable()) {
            block(it as T)
        }
    }
}

private var <T : View> T.triggerLastTime: Long
    get() = if (getTag(1123460103) != null) getTag(1123460103) as Long else 0
    set(value) {
        setTag(1123460103, value)
    }

private var <T : View> T.triggerDelay: Long
    get() = if (getTag(1123461123) != null) getTag(1123461123) as Long else -1
    set(value) {
        setTag(1123461123, value)
    }

private fun <T : View> T.clickEnable(): Boolean {
    var flag = false
    val currentClickTime = System.currentTimeMillis()
    if (currentClickTime - triggerLastTime >= triggerDelay) {
        flag = true
    }
    triggerLastTime = currentClickTime
    return flag
}